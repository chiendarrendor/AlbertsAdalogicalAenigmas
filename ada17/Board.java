import java.awt.Point;
import java.util.regex.*;

public class Board
{
	int width;
	int height;
	public enum CellColor { WHITE, GREY, BLACK };
	CellColor[][] colors = null;
	ClueSet clueset = new ClueSet();

	public boolean onBoard(int x,int y)
	{
		return x >= 0 && x < width && y >= 0 && y < height && colors[x][y] != CellColor.BLACK;
	}
	
	public boolean onBoard(Point p) { return onBoard(p.x,p.y); }
	
	
	
	public Board(String filename)
	{
		GridFileReader gfr = new GridFileReader(filename);
		width = gfr.getWidth();
		height = gfr.getHeight();
		colors = new CellColor[width][height];
		
		if (!gfr.hasBlock("COLORS")) throw new RuntimeException("File has no COLORS section");
		
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				String c = gfr.getBlock("COLORS")[x][y];
				if (c.equals("#")) colors[x][y] = CellColor.BLACK;
				else if (c.equals("@")) colors[x][y] = CellColor.GREY;
				else if (c.equals(".")) colors[x][y] = CellColor.WHITE;
				else throw new RuntimeException("Unknown color code '" + c + "'");
			}
		}
		
		int numsevens = gfr.toInt(gfr.getVar("NUM7BOXES"));
		for (int i = 0 ; i < numsevens ; ++i)
		{
			String sevname = "7BOX-" + i;
			String sevstr = gfr.getVar(sevname);
			String[] xy = sevstr.split(" ");
			Point ul = new Point(gfr.toInt(xy[0]),gfr.toInt(xy[1]));
			SevenBoxClue.MakeSevenBox(clueset,ul.x,ul.y);
		}
		
		Pattern deltaPattern = Pattern.compile("^([0-9])([NEWS])$");
		Pattern inequalityPattern = Pattern.compile("^([V<>^])([NEWS])$");
		
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				String c = gfr.getBlock("SIGILS")[x][y];
				if (c.equals("##") || c.equals("..")) continue;
				Matcher dm = deltaPattern.matcher(c);
				Matcher im = inequalityPattern.matcher(c);
				
				if (dm.find())
				{
					new DeltaClue(new Point(x,y),gfr.toInt(dm.group(1)),dm.group(2).charAt(0),this,clueset);
				}
				else if (im.find())
				{
					new InequalityClue(new Point(x,y),im.group(1).charAt(0),im.group(2).charAt(0),this,clueset);
				}
				else throw new RuntimeException("Unknown sigil " + c);
			}
		}
	}
}
