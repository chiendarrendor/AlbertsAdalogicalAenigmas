
import java.io.*;
import java.util.*;

public class Board
{
	static Map<Integer,String> dirStrings = new HashMap<Integer,String>();
	static Map<String,Integer> stringDirs = new HashMap<String,Integer>();
	static
	{
		dirStrings.put(Circle.N,"N");		stringDirs.put("N",Circle.N);
		dirStrings.put(Circle.S,"S");		stringDirs.put("S",Circle.S);
		dirStrings.put(Circle.E,"E");		stringDirs.put("E",Circle.E);
		dirStrings.put(Circle.W,"W");		stringDirs.put("W",Circle.W);
		dirStrings.put(Circle.NE,"NE");		stringDirs.put("NE",Circle.NE);
		dirStrings.put(Circle.SE,"SE");		stringDirs.put("SE",Circle.SE);
		dirStrings.put(Circle.NW,"NW");		stringDirs.put("NW",Circle.NW);
		dirStrings.put(Circle.SW,"SW");		stringDirs.put("SW",Circle.SW);
		dirStrings.put(Circle.CENTER,"C");	stringDirs.put("C",Circle.CENTER);
		dirStrings.put(Circle.NONE,".");	stringDirs.put(".",Circle.NONE);
	}
		
	public boolean isEdge(int x,int y, int dir)
	{
		if (x == 0)
		{
			if (dir == Circle.NW || dir == Circle.W || dir == Circle.SW) return true;
		}
		if (y == 0)
		{
			if (dir == Circle.NW || dir == Circle.N || dir == Circle.NE) return true;
		}
		if (x == width - 1)
		{
			if (dir == Circle.NE || dir == Circle.E || dir == Circle.SE) return true;
		}
		if (y == height - 1)
		{
			if (dir == Circle.SW || dir == Circle.S || dir == Circle.SE) return true;
		}
		return false;
	}
		
	private int cellId = 0;
	public void setCellId(int id) { cellId = id; }
	public void setCellOn(int x,int y) { cellIds[x][y] = cellId; }
	public void setCellOn(int x,int y,int id) { cellIds[x][y] = id; }
	public boolean isCellOn(int x,int y) { return cellIds[x][y] != -1; }
	public boolean isCellEmpty(int x,int y) { return !isCellOn(x,y); }
		
	int width;
	int height;
	int [][] cellIds; // -1 means cell not on
	DotColor [][] cellColors;
	int [][] dotLoc; // uses the direction ints in Circle
	
	public Board(int w,int h)
	{
		width = w;
		height = h;
		cellIds = new int[w][h];
		cellColors = new DotColor[w][h];
		dotLoc = new int[w][h];
		
		for (int x = 0 ; x < w ; ++x)
		{
			for (int y = 0 ; y < h ; ++y)
			{
				cellIds[x][y] = -1;
				cellColors[x][y] = DotColor.NONE;
				dotLoc[x][y] = Circle.NONE;
			}
		}
	}
	
	public Board(Board right)
	{
		width = right.width;
		height = right.height;
		cellIds = new int[width][height];
		cellColors = new DotColor[width][height];
		dotLoc = new int[width][height];
		
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				cellIds[x][y] = right.cellIds[x][y];
				cellColors[x][y] = right.cellColors[x][y];
				dotLoc[x][y] = right.dotLoc[x][y];
			}
		}
	}
	
	
	
	public Board(GridFileReader gfr)
	{
		width = gfr.getWidth();
		height = gfr.getHeight();
		cellIds = new int[width][height];
		cellColors = new DotColor[width][height];
		dotLoc = new int[width][height];
		
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				cellIds[x][y] = gfr.toInt(gfr.getBlock("CELLS")[x][y]);
				if (!isCellOn(x,y)) continue;
				switch (gfr.getBlock("COLORS")[x][y].charAt(0))
				{
					case '.': cellColors[x][y] = DotColor.NONE; break;
					case 'W': cellColors[x][y] = DotColor.WHITE; break;
					case 'B': cellColors[x][y] = DotColor.BLACK; break;
					default: throw new RuntimeException("Dot Color What? " + gfr.getBlock("COLORS")[x][y].charAt(0));
				}
				dotLoc[x][y] = stringDirs.get(gfr.getBlock("DIRS")[x][y]);
			}
		}
	}
	
	public Board Reflect()
	{
		Board result = new Board(width,height);
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; y++)
			{
				int newx = width-1-x;
				int newy = y;
				result.cellIds[newx][newy] = cellIds[x][y];
				result.cellColors[newx][newy] = cellColors[x][y];
				int loc = dotLoc[x][y];
				switch(loc)
				{
					case Circle.NONE:
					case Circle.CENTER:
					case Circle.N:
					case Circle.S:
						break;
					case Circle.W: loc = Circle.E; break;
					case Circle.E: loc = Circle.W; break;
					case Circle.NE: loc = Circle.NW; break;
					case Circle.NW: loc = Circle.NE; break;
					case Circle.SE: loc = Circle.SW; break;
					case Circle.SW: loc = Circle.SE; break;
				}
				result.dotLoc[newx][newy] = loc;
			}
		}
		return result;
	}
	
	public Board Rotate()
	{
		Board result = new Board(height,width);
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; y++)
			{
				int newx = height-1-y;
				int newy = x;
				result.cellIds[newx][newy] = cellIds[x][y];
				result.cellColors[newx][newy] = cellColors[x][y];
				int loc = dotLoc[x][y];
				switch(loc)
				{
					case Circle.NONE:
					case Circle.CENTER:
						break;
					case Circle.N: loc = Circle.E; break;
					case Circle.S: loc = Circle.W; break;
					case Circle.W: loc = Circle.N; break;
					case Circle.E: loc = Circle.S; break;
					case Circle.NE: loc = Circle.SE; break;
					case Circle.NW: loc = Circle.NE; break;
					case Circle.SE: loc = Circle.SW; break;
					case Circle.SW: loc = Circle.NW; break;
				}
				result.dotLoc[newx][newy] = loc;
			}
		}
		return result;	
	
	}
	
	Vector<Board> images = new Vector<Board>();
	public List<Board> getImages()
	{
		if (images.size() == 0)
		{
			Board t0 = this;
			for (int i = 0 ; i < 4 ; ++i)
			{
				images.add(t0);
				images.add(t0.Reflect());
				t0 = t0.Rotate();
			}
		}
		return images;
	}
	
	public void AddDot(int x,int y, DotColor color, int loc)
	{
		if (color == DotColor.NONE) throw new RuntimeException("Why make a blank dot?");
		if (!isCellOn(x,y)) throw new RuntimeException("That cell is inactive!");
		if (cellColors[x][y] != DotColor.NONE) throw new RuntimeException("That cell's already been dotted!");
		cellColors[x][y] = color;
		dotLoc[x][y] = loc;
	}
	
	public void Write(String filename)
	{
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(filename);
		}
		catch(Exception ex)
		{
			throw new RuntimeException("some problem with board write!");
		}
		
		writer.println(width + " " + height);
		writer.println("CODE:CELLS");
		for (int y = 0 ; y < height ; ++y)
		{
			for (int x = 0 ; x < width ; ++x)
			{
				if (x != 0) writer.print(" ");
				writer.print(cellIds[x][y]);
			}
			writer.println("");
		}
		writer.println("CODE:COLORS");
		for (int y = 0 ; y < height ; ++y)
		{
			for (int x = 0 ; x < width ; ++x)
			{
				if (x != 0) writer.print(" ");
				switch(cellColors[x][y])
				{
					case NONE: writer.print("."); break;
					case WHITE: writer.print("W"); break;
					case BLACK: writer.print("B"); break;
				}
			}
			writer.println("");
		}
		writer.println("CODE:DIRS");
		for (int y = 0 ; y < height ; ++y)
		{
			for (int x = 0 ; x < width ; ++x)
			{
				if (x != 0) writer.print(" ");
				
				if (!isCellOn(x,y)) writer.print(".");
				else if (cellColors[x][y] == DotColor.NONE) writer.print(".");
				else writer.print(dirStrings.get(dotLoc[x][y]));
			}
			writer.println("");
		}
		writer.close();
	}
		
	
	
	
	
	
	public void Show()
	{
		for (int y = 0 ; y < height; ++y)
		{
			for (int x = 0 ; x < width ; ++x)
			{
				System.out.print(isCellOn(x,y) ? "#" : " ");
			}
			System.out.println("");
		}
	}
}