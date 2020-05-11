import java.io.*;


public class BoardReader
{
	AdaBoard theBoard = null;
	Position numbers = null;
	String cluetype = null;
	String solution = null;
	char rawshades[][];

	public BoardReader(String filename)
	{
		ReadBoard(filename);
	}
	
	
	private void ReadBoard(String filename)
	{
		try
		{
			FileReader reader = new FileReader(filename);
			BufferedReader bufread = new BufferedReader(reader);
			
			String line = null;
			String processingWhat = "";
			int regionsCurline = 0;
			int shadesCurline = 0;
			int numbersCurline = 0;
			
			while((line = bufread.readLine()) != null)
			{
				if (theBoard == null)
				{
					String[] dims = line.split(" ");
					if (dims.length != 2) throw new RuntimeException("First line does not contain two records");
					int width = Integer.parseInt(dims[0]);
					int height = Integer.parseInt(dims[1]);
					theBoard = new AdaBoard(width,height);
					rawshades = new char[width][height];
				}
				else if (line.equals("regions:"))
				{
					processingWhat = "regions";
				}
				else if (line.equals("shades:"))
				{
					processingWhat = "shades";
				}
				else if (line.startsWith("solution")) {
					String[] stuff = line.split(":");
					solution = stuff[1].trim();
				}
				else if (line.startsWith("cluetype")) {
					String[] stuff = line.split(":");
					cluetype = stuff[1].trim();
				}
				else if (line.equals("numbers:"))
				{
					if (regionsCurline != theBoard.height) throw new RuntimeException("must specify all regions before starting numbers");
					processingWhat = "numbers";
					if (numbers == null) numbers = new Position(theBoard);
				}
				else if (processingWhat.equals("numbers"))
				{
					if (numbersCurline >= theBoard.height) throw new RuntimeException("Numbers specification has too many lines");
					if (line.length() != theBoard.width) throw new RuntimeException("Numbers line length does not equal board width");
					for (int i = 0 ; i < theBoard.width ; ++i )
					{
						if (line.charAt(i) == '.') continue;
						int num = Character.getNumericValue(line.charAt(i));
						numbers.SetOneCell(i,numbersCurline,num);
					}
					++numbersCurline;
				}
				else if (processingWhat.equals("shades"))
				{
					if (shadesCurline >= theBoard.height) throw new RuntimeException("Shades specification has too many lines");
					if (line.length() != theBoard.width) throw new RuntimeException("Shades line length does not equal board width");
					for (int i = 0 ; i < theBoard.width ; ++i )
					{
						if (line.charAt(i) == '#')
						{
							theBoard.isSpecial[i][shadesCurline] = true;
						}
						rawshades[i][shadesCurline] = line.charAt(i);
					}
					++shadesCurline;
				}
				else if (processingWhat.equals("regions"))
				{
					if (regionsCurline >= theBoard.height) throw new RuntimeException("Region specification has too many lines");
					if (line.length() != theBoard.width) throw new RuntimeException("Region line length does not equal board width");
					for (int i = 0 ; i < theBoard.width ; ++i )
					{
						theBoard.addCellToRegion(line.charAt(i),i,regionsCurline);
					}
					++regionsCurline;
				}
				else
				{
					throw new RuntimeException("Unknown line behavior");
				}
			}
			bufread.close();
		}
		catch(Exception ex)
		{
			theBoard = null;
			throw new RuntimeException(ex);
		}
	}
}