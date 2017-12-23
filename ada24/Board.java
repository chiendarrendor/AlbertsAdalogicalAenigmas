import java.awt.Point;
import java.util.*;
import java.util.regex.*;

public class Board
{
	public class Region
	{
		char id;
		Vector<Point> cells = new Vector<Point>();
		public Region(char id) { this.id = id;}
	}

	public class Path
	{
		char id;
		Point p1;
		Point p2;
		int numMirrors;
		public Path(char id, int numm,Point p1,Point p2) { this.id = id; numMirrors = numm; this.p1 = p1; this.p2 = p2; }
	}
	Map<Point,Path> pathsByPoint = new HashMap<Point,Path>();
	Map<Character,Path> pathsById = new HashMap<Character,Path>();

	int width;
	int height;
	char[][] letters;
	Region[][] regions;
	Map<Character,Region> regionsById = new HashMap<Character,Region>();
	Vector<Path> paths = new Vector<Path>();
	
	public boolean onBoard(int x,int y) { return x >= 0 && x < width && y >= 0 && y < height; }

	static Pattern ppPattern = Pattern.compile("^([TBLR])(\\d+)$");
	private Point ParsePathPoint(String s)
	{
		Matcher m = ppPattern.matcher(s);
		if (!m.find()) throw new RuntimeException("bad path point: " + s);
		int coord = Integer.parseInt(m.group(2));
		char dir = m.group(1).charAt(0);
		switch(dir)
		{
			case 'T': return new Point(coord,-1);
			case 'L': return new Point(-1,coord);
			case 'B': return new Point(coord,height);
			case 'R': return new Point(width,coord);
		}
		return null;
	}
	
	private void AddPathPoint(Point p,Path path)
	{
		if (pathsByPoint.containsKey(p)) throw new RuntimeException("Path point already exists: " + p);
		pathsByPoint.put(p,path);
	}
	
	public Board(GridFileReader gfr)
	{
		width = gfr.getWidth();
		height = gfr.getHeight();
		letters = new char[width][height];
		regions = new Region[width][height];
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				letters[x][y] = gfr.getBlock("LETTERS")[x][y].charAt(0);
				char rid = gfr.getBlock("REGIONS")[x][y].charAt(0);
				if (!regionsById.containsKey(rid)) regionsById.put(rid,new Region(rid));
				regionsById.get(rid).cells.add(new Point(x,y));
				regions[x][y] = regionsById.get(rid);
			}
		}

		
		
		
		
	
		String pathnames = gfr.getVar("LETTERS");
		for (int i = 0 ; i < pathnames.length() ; ++i)
		{
			char c = pathnames.charAt(i);
			String pstr = gfr.getVar(""+c);
			String[] parts = pstr.split("\\s+");
			int nm = Integer.parseInt(parts[0]);
			Point p1 = ParsePathPoint(parts[1]);
			Point p2 = ParsePathPoint(parts[2]);
			Path p = new Path(c,nm,p1,p2);
			AddPathPoint(p1,p);
			AddPathPoint(p2,p);
			paths.add(p);
			pathsById.put(c,p);
		}	
	}
}