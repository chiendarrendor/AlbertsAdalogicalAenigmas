
import java.util.*;
import java.awt.Point;

public class AdaBoard
{
	public class Region
	{
		char id;
		Vector<Point> cellV = new Vector<Point>();
		
		public Region(char id) { this.id = id; }
		
		public void addPoint(Point p)
		{
			cellV.add(p);
			cells[p.x][p.y] = this;
		}
	}

	Map<Character,Region> regionsById = new HashMap<Character,Region>();
	int width;
	int height;
	Region[][] cells;
	boolean[][] isSpecial;
	
	public AdaBoard(int width,int height)
	{
		cells = new Region[width][height];
		isSpecial = new boolean[width][height];
		this.width = width;
		this.height = height;
		
		for (int x = 0 ; x < width ; ++ x) { for (int y = 0 ; y < height ; ++y) { isSpecial[x][y] = false; } }
	}
	
	public boolean isOnBoard(Point p)
	{
		return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
	}
	
	public void makeCellSpecial(int x,int y) { isSpecial[x][y] = true; }
	
	public void addCellToRegion(char rid,int x,int y)
	{
		if (!regionsById.containsKey(rid))
		{
			regionsById.put(rid,new Region(rid));
		}
		regionsById.get(rid).addPoint(new Point(x,y));
	}
}
	
	