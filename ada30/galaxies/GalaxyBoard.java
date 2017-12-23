

import java.util.*;
import java.awt.Color;
import java.awt.Point;

public class GalaxyBoard
{
	int width;
	int height;
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public class Area
	{
		int id;
		Color c;
		double x;
		double y;
		Set<Point> points = new HashSet<Point>();
		Vector<Pair> pairs = new Vector<Pair>();
		
		public int getId() { return id; }
		public Color getColor() { return c; }
		public double getX() { return x; }
		public double getY() { return y; }
		
		public class Pair
		{
			boolean isDegenerate;
			boolean isInitial;
			
			public int getAreaId() { return id; }
			
			Point p1;
			Point p2;
			
			public Pair(boolean isInitial, int x1,int y1, int x2, int y2)
			{
				this.isInitial = isInitial;
				isDegenerate = x1 == x2 && y1 == y2;
				p1 = new Point(x1,y1);
				p2 = new Point(x2,y2);
				theboard[x1][y1] = this;
				theboard[x2][y2] = this;
			}
			
			public Point getMirrorPoint(Point p)
			{
				if (p.equals(p1)) return p2;
				return p1;
			}
			
			
		}		
		
		
		public Area(int id,Color c,double x,double y)
		{
			this.id = id;
			this.c = c;
			this.x = x;
			this.y = y;
		}
		
		public boolean addCell(int x,int y)
		{
			return addCell(false,x,y);
		}
		
		boolean addCell(boolean isInitial, int x, int y)
		{
			if (!isLegal(x,y)) return false;
			Point p = new Point(x,y);
			Point mp = mirrorOf(x,y);
			if (!isLegal(mp)) return false;
			points.add(p);
			points.add(mp);
			pairs.add(new Pair(isInitial,x,y,mp.x,mp.y));
			
			AssertValidBoard();
			return true;
		}
		
		public Point mirrorOf(int ix,int iy)
		{
			return new Point((int)(2.0*x - ix),(int)(2.0*y-iy));
		}
		public Point mirrorOf(Point in)
		{
			return mirrorOf(in.x,in.y);
		}
	}
		
	private Map<Integer,Area> areas = new HashMap<Integer,Area>();	
	private Area.Pair[][] theboard;
		
	public int numAreas() { return areas.size(); }
	public Iterable<Area> getAreas() { return areas.values(); }
	public Area getAreaById(int id) { return areas.get(id); }
	public Area.Pair getBoardCell(int x,int y) { return theboard[x][y]; }
		
	public GalaxyBoard(int width,int height)
	{
		this.width = width;
		this.height = height;
		theboard = new Area.Pair[width][height];
	}
	
	public GalaxyBoard(GalaxyBoard right)
	{
		this(right.getWidth(),right.getHeight());
		for (Area a : right.areas.values())
		{
			AddArea(a.getId(),a.getColor(),a.getX(),a.getY()); // this adds all items in area.pairs where isInitial is true
			Area myarea = getAreaById(a.getId());
			for (Area.Pair p : a.pairs)
			{
				if (p.isInitial) continue;
				myarea.addCell(p.p1.x,p.p1.y);
			}
		}
	}
	
	private int arid = 0;
	public boolean AddNewBlackArea(double x,double y)
	{
		return AddArea(++arid,Color.black,x,y);
	}
	public boolean AddNewWhiteArea(double x,double y)
	{
		return AddArea(++arid,Color.white,x,y);
	}	
	
	
	
	// x and/or y may be x.5, representing the start being on an intersection or an edge
	// if both x and y are .5, then the area starts with 4 cells
	// if either x and y are .5 then the area starts with 2 cells
	// if neither x nor y are .5, then the area starts with 1 cell, which is mirrored to itself.
	public boolean AddArea(int id, Color c, double x, double y)
	{
		Area nar = new Area(id,c,x,y);
		
		boolean xIsOffset = ((int)(2.0*x)) % 2 == 1;
		boolean yIsOffset = ((int)(2.0*y)) % 2 == 1;
		int ulx = (int)Math.floor(x);
		int uly = (int)Math.floor(y);
		
		if (xIsOffset && yIsOffset)
		{
			if (!isLegal(ulx,uly)) return false;
			Point mp1 = nar.mirrorOf(ulx,uly);
			if (!isLegal(mp1)) return false;
			Point cross = new Point(ulx+1,uly);
			if (!isLegal(cross)) return false;
			Point mp2 = nar.mirrorOf(cross);
			if (!isLegal(mp2)) return false;
			
			nar.addCell(true,ulx,uly);
			nar.addCell(true,cross.x,cross.y);
		}
		else if (xIsOffset || yIsOffset)
		{
			if (!isLegal(ulx,uly)) return false;
			Point mp1 = nar.mirrorOf(ulx,uly);
			if (!isLegal(mp1)) return false;
			
			nar.addCell(true,ulx,uly);
		}
		else 
		{
			if (!isLegal(ulx,uly)) return false;
			nar.addCell(true,ulx,uly);
		}
			
		areas.put(id,nar);
		return true;
	}
	
	public boolean isLegal(Point p)
	{
		return isLegal(p.x,p.y);
	}
	
	public boolean isLegal(int x,int y)
	{
		return isInBounds(x,y) && isEmpty(x,y);
	}
	
	public boolean isEmpty(int x,int y)
	{
		return theboard[x][y] == null;
	}
	
	public boolean isInBounds(int x, int y)
	{
		if (x < 0) return false;
		if (x >= width) return false;
		if (y < 0) return false;
		if (y >= height) return false;
		return true;
	}
	
	public void AssertValidBoard()
	{
		for (Area a : areas.values())
		{
			for (Area.Pair p : a.pairs)
			{
				assert getBoardCell(p.p1.x,p.p1.y) == p;
				assert getBoardCell(p.p2.x,p.p2.y) == p;
			}
		}
		ForEachCell.op(getWidth(),getHeight(),new ForEachCell.CellOp() {
			public boolean op(int x,int y)
			{
				Area.Pair p = getBoardCell(x,y);
				if (p == null) return true;
				if ((p.p1.x != x || p.p1.y != y) &&
					(p.p2.x != x || p.p2.y != y)) assert 0 == 1;
				return true;
			}
		});
	}
				
	
}

	