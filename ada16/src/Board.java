
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;

import java.awt.Point;
import java.io.*;

public class Board implements StandardFlattenSolvable<Board>
{
	GridFileReader gfr;
	int width;
	int height;
	int depth;
	Point start = null;
	Point end = null;

//	TileLibertySet tls = null;
	
	char[][] letters;
	char[][] sigils;
	
	boolean hasLetter(int x,int y) { return letters[x][y] != '.'; }
	char getLetter(int x,int y) { return letters[x][y]; }
	boolean isStart(int x,int y) { return sigils[x][y] == 'S'; }
	boolean isEnd(int x,int y) {  return sigils[x][y] == 'E'; }
	public Point getStart() { return start; }
	public Point getEnd() { return end; }
	boolean isTile(int x,int y) {  return sigils[x][y] == '*' || sigils[x][y] == 'S' || sigils[x][y] == 'E'; }
	boolean isTree(int x,int y) {  return sigils[x][y] == 'O'; }
	boolean isEmpty(int x,int y) {  return sigils[x][y] == '.'; }
	boolean inBounds(Point p) { return gfr.inBounds(p); }
	
	void addTile(int x,int y) 
	{ 
		sigils[x][y] = '*';
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
		gfr = new GridFileReader(filename);
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
				if (isStart(x,y)) {
					if (start != null) throw new RuntimeException("Multiple Starts!");
					start = p;
				}
				if (isEnd(x,y)) {
					if (end != null) throw new RuntimeException("Multiple Ends!");
					end = p;
				}


//				if (isTile(x,y)) tls.AddTile(p);
//				if (isTree(x,y)) tls.AddTree(p);
			}
		}
	}
	
	public Board(Board right)
	{
		gfr = right.gfr;
		width = right.width;
		height = right.height;
		depth = right.depth + 1;
		start =  right.start;
		end = right.end;

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


	private static class MyMove {
		int x;
		int y;
		boolean isTree;
		public MyMove(int x,int y,boolean isTree) { this.x = x; this.y = y; this.isTree = isTree; }
		public boolean applyMove(Board b) {
			if (b.isTree(x,y)) return isTree;
			if (b.isTile(x,y)) return !isTree;
			if (isTree) b.addTree(x,y);
			else b.addTile(x,y);
			return true;
		}
	}

	@Override public boolean isComplete() { return isSolved(); }
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
	@Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

	@Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
		if (isTile(x,y)) return null;
		if (isTree(x,y)) return null;

		Board b1 = new Board(this);
		Board b2 = new Board(this);
		MyMove mm1 = new MyMove(x,y,true);
		MyMove mm2 = new MyMove(x,y,false);

		mm1.applyMove(b1);
		mm2.applyMove(b2);
		return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
	}


}
