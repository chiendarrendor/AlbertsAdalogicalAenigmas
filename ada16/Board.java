
import java.awt.Point;
import java.io.*;

public class Board
{
	int width;
	int height;
	int depth;
//	TileLibertySet tls = null;
	
	char[][] letters;
	char[][] sigils;
	
	boolean hasLetter(int x,int y) { return letters[x][y] != '.'; }
	char getLetter(int x,int y) { return letters[x][y]; }
	boolean isStart(int x,int y) { return sigils[x][y] == 'S'; }
	boolean isEnd(int x,int y) {  return sigils[x][y] == 'E'; }	
	boolean isTile(int x,int y) {  return sigils[x][y] == '#' || sigils[x][y] == 'S' || sigils[x][y] == 'E'; }
	boolean isTree(int x,int y) {  return sigils[x][y] == 'O'; }
	boolean isEmpty(int x,int y) {  return sigils[x][y] == '.'; }
	
	void addTile(int x,int y) 
	{ 
		sigils[x][y] = '#'; 
//		tls.AddTile(new Point(x,y));
	}
	
	void addTree(int x,int y) 
	{ 
		sigils[x][y] = 'O';
//		tls.AddTree(new Point(x,y));
	}
	
	public void printBoard(String title)
	{
		System.out.println(title + ":");
		for (int y = 0 ; y < height ; ++y)
		{
			System.out.print("  ");
			for (int x = 0 ; x < width ; ++x)
			{
				if (isTile(x,y)) System.out.print("#");
				if (isTree(x,y)) System.out.print("O");
				if (isEmpty(x,y)) System.out.print(".");
			}
			System.out.println("");
		}
	}
			
	
	
	
	public boolean isSolved()
	{
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				if (isEmpty(x,y)) return false;
			}
		}
		return true;
	}
	
	public int numEmpty()
	{
		int result = 0;
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{	
				if (isEmpty(x,y)) ++result;
			}
		}
		return result;
	}

/*	
	public Point findEmpty()
	{
		Point aGuess = null;
	
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				if (!isEmpty(x,y)) continue;
				aGuess = new Point (x,y);
				
				Board tileb = new Board(this);
				tileb.addTile(x,y);
				if (AntiTedium.ApplyAntiTedium(tileb) == LogicStatus.CONTRADICTION) return aGuess;
				
				Board treeb = new Board(this);
				treeb.addTree(x,y);
				if (TileConnectivity.UpdateTileConnectivity(treeb) == LogicStatus.CONTRADICTION) return aGuess;
			}
		}
		
		return aGuess;
	}
*/				
	
	static BufferedReader br = null;
	static String readLineFromSTDIN()
	{
		try
		{
			if (br == null) br = new BufferedReader(new InputStreamReader(System.in));
			return br.readLine();
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
/*
	public Point findEmpty()
	{
		do
		{
//			BoardShower bs = new BoardShower(this);
			System.out.print("x y: ");
			String in = readLineFromSTDIN();
//			bs.dispose();
			String[] split = in.split(" ");
			if (split.length != 2) 
			{
				System.out.println("Bad input");
				continue;
			}
			int x;
			int y;
			try
			{
				x = Integer.parseInt(split[0]);
				y = Integer.parseInt(split[1]);
			}
			catch(Exception ex)
			{
				System.out.println("bad input: " + ex);
				continue;
			}
			if (!isEmpty(x,y))
			{
				System.out.println("not good.");
				continue;
			}
			
			return new Point(x,y);
		} while(true);
	}
*/

	public Point findEmpty()
	{
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				if (!isEmpty(x,y)) continue;
				return new Point (x,y);				
			}
		}
		return null;
	}

	
	
	
	
	public Board(String filename)
	{
		GridFileReader gfr = new GridFileReader(filename);
		width = gfr.getWidth();
		height = gfr.getHeight();
		depth = 0;
//		tls = new TileLibertySet(width,height);
		
		String[][] rawletters = gfr.getBlock("LETTERS");
		String[][] rawsigils = gfr.getBlock("FEATURES");
		
		letters = new char[width][height];
		sigils = new char[width][height];
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				letters[x][y] = rawletters[x][y].charAt(0);
				sigils[x][y] = rawsigils[x][y].charAt(0);
				Point p = new Point(x,y);
//				if (isTile(x,y)) tls.AddTile(p);
//				if (isTree(x,y)) tls.AddTree(p);
			}
		}
	}
	
	public Board(Board right)
	{
		width = right.width;
		height = right.height;
		depth = right.depth + 1;
//		tls = new TileLibertySet(right.tls);
		letters = right.letters;
		sigils = new char[width][height];
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				sigils[x][y] = right.sigils[x][y];
			}
		}
	}
}
