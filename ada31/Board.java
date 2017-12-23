
import java.util.*;
import java.awt.Point;


public class Board
{
	int width;
	int height;
	
	int nextregionid = 0;
	Map<Character,Region> rmap = new HashMap<Character,Region>();
	
	public class Region
	{
		int regionid;
		Set<Point> cells = new HashSet<Point>();
		Point exemplarCell = null;
		
		public String toString()
		{
			StringBuffer sb = new StringBuffer();
			sb.append("(").append(exemplarCell.x).append(",").append(exemplarCell.y).append(")");
			return sb.toString();
		}
	
		
	}
	
	Region[][] regions;
	
	static final int NUMBER_SENTINEL = -99;
	int [][] numbers;
	
	private void AddSpaceToRegion(int x,int y, char id)
	{
		if (!rmap.containsKey(id))
		{
			Region r = new Region();
			r.regionid = nextregionid++;
			rmap.put(id,r);
		}
		Region curr = rmap.get(id);
		Point p = new Point(x,y);
		curr.cells.add(p);
		regions[x][y] = curr;
		if (curr.exemplarCell == null) curr.exemplarCell = p;
	}
	
	
	
	
	
	public Board(int width,int height,String[][] rstrings, String[][] numstrings)
	{
		this.width = width;
		this.height = height;
		regions = new Region[width][height];
		numbers = new int[width][height];
		
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				if (rstrings[x][y].length() != 1) throw new RuntimeException("Bad Region symbol at " + x + "," + y);
				AddSpaceToRegion(x,y,rstrings[x][y].charAt(0));
				
				if (numstrings[x][y].equals("."))
				{
					numbers[x][y] = NUMBER_SENTINEL;
				}
				else
				{
					numbers[x][y] = GridFileReader.toInt(numstrings[x][y]);
				}
			}
		}
				
	}
}