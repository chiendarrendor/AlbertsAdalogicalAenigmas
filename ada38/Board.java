
import java.util.*;
import java.awt.Point;


public class Board
{
	int width;
	int height;
	
	Map<Character,Region> rmap = new HashMap<Character,Region>();
	
	public class Region
	{
		char regionid;
		Set<Point> cells = new HashSet<Point>();	

		public Region(char rid) { regionid = rid; }
	}
	
	Region[][] regions;
	
	static final int NUMBER_SENTINEL = -99;
	char [][] letters;
	
	public char GetLetter(int x,int y) { return letters[x][y]; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	private void AddSpaceToRegion(int x,int y, char id)
	{
		if (!rmap.containsKey(id))
		{
			Region r = new Region(id);
			rmap.put(id,r);
		}
		Region curr = rmap.get(id);
		Point p = new Point(x,y);
		curr.cells.add(p);
		regions[x][y] = curr;
	}
	
	public boolean isOnBoard(int x,int y) { return x >= 0 && y >= 0 && x < getWidth() && y < getHeight(); }
	
	
	
	public Board(int width,int height,String[][] rstrings, String[][] lets)
	{
		this.width = width;
		this.height = height;
		regions = new Region[width][height];
		letters = new char[width][height];
		
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				if (rstrings[x][y].length() != 1) throw new RuntimeException("Bad Region symbol at " + x + "," + y);
				AddSpaceToRegion(x,y,rstrings[x][y].charAt(0));
				letters[x][y] = lets[x][y].charAt(0);
			}
		}	
	}
}