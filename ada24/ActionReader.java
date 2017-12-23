
import java.util.*;
import java.util.regex.*;
import java.io.*;


public class ActionReader
{
	public static void ReadFile(String filename,State theState)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line = null;		
			
			while((line = reader.readLine()) != null)
			{	
				line = line.trim();
				System.out.println("Parsing line: " + line);
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;
				
				String[] parts = line.split("\\s+");
				if (parts.length == 0) throw new RuntimeException("Invalid parting!");
				
				if (parts[0].equals("EMPTY") && parts.length == 3)
				{
					int x = Integer.parseInt(parts[1]);
					int y = Integer.parseInt(parts[2]);
					theState.state[x][y] = CellState.EMPTY;
				}
				else if (parts[0].equals("SLASH") && parts.length == 3)
				{
					int x = Integer.parseInt(parts[1]);
					int y = Integer.parseInt(parts[2]);
					theState.state[x][y] = CellState.SLASH;
				}
				else if (parts[0].equals("BACKSLASH") && parts.length == 3)
				{
					int x = Integer.parseInt(parts[1]);
					int y = Integer.parseInt(parts[2]);
					theState.state[x][y] = CellState.BACKSLASH;
				}				
				else if (parts[0].equals("GENERICMIRROR") && parts.length == 3)
				{
					int x = Integer.parseInt(parts[1]);
					int y = Integer.parseInt(parts[2]);
					theState.state[x][y] = CellState.GENERICMIRROR;
				}					
				else if (parts[0].equals("FIRE") && parts.length == 2)
				{
					Board.Path path = theState.myBoard.pathsById.get(parts[1].charAt(0));
                                        FireField ff = new FireField(theState,path,path.p1,false);
                                        if (ff.paths.size() != 1) throw new RuntimeException("Fire failed");
                                        ff.paths.firstElement().ApplyPath(theState);
                                        theState.Fire(path);
				}
				else
				{
					throw new RuntimeException("LOLWUT?");
				}	
			}
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
}