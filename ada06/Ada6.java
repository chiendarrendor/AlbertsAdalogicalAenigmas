import java.util.*;
import java.awt.Point;

public class Ada6
{
	private static Point[] deltas = new Point[] { new Point(0,1), new Point(0,-1), new Point(1,0), new Point(-1,0) };

	private enum CellType 
	{ 	//		show	terminal	blocksLight		AffectsAdj		NumAdj		LightIsBad		Fillable	RequiresExternalLight	neverBulb
		BLOCK(  '#',  	true,     	true,       	false,     		 -1,     	false,      	false,		false,					false), 
		ZERO(   '0',  	true,		true,			true,			0,			false,			false,		false,					false),
		ONE(    '1',	true,		true,			true,			1,			false,			false,		false,					false),
		TWO(    '2',	true,		true,			true,			2,			false,			false,		false,					false),
		THREE(  '3',	true,		true,			true,			3,			false,			false,		false,					false),
		FOUR(   '4',	true,		true,			true,			4,			false,			false,		false,					false),
		BULB(   'O',	true,		true,			false,			-1,			true,			false,		false,					false),
		LIT(    '*',	true,		false,			false,			-1,			false,			false,		false,					true),
		EMPTY(  '.',	false,		false,			false,			-1,			false,			true,		false,					false), 
		NOBULB( 'x',	false,		false,			false,			-1,			false,			false,		true,					true);
	
		private final char show;
		public char getShow() { return show; } // what character does this show as?
		
		private boolean terminal;
		public boolean isTerminal() { return terminal; } // is this cell allowed on a complete board?
		public boolean needsLight() { return !terminal; }
		
		private boolean blocksLight;
		public boolean blocksLight() { return blocksLight; } // does the path of light stop here?
		
		private boolean affectsAdjacents;
		public boolean affectsAdjacents() { return affectsAdjacents; } // does this space force the adjacent spaces to something?
		
		private int numAdjacents;
		public int numAdjacents() { return numAdjacents; } // how many adjacents must be light bulbs?
		
		private boolean lightIsBad;
		public boolean lightIsBad() { return lightIsBad; } // true if hitting this with a light makes a contradiction
		
		private boolean fillable;
		public boolean isFillable() { return fillable; } // true if this space could have BULB or NOBULB put in it
		
		private boolean requiresLight;
		public boolean requiresExternalLight() { return requiresLight; }
		
		private boolean neverBulb;
		public boolean neverBulb() { return neverBulb; }
		
		CellType(char s,boolean t, boolean b, boolean aa, int na,boolean lib,boolean fil, boolean rel, boolean nb) 
		{ show=s;terminal=t;blocksLight=b;affectsAdjacents=aa;numAdjacents=na;
		  lightIsBad=lib; fillable = fil; requiresLight = rel; neverBulb = nb; }	
	}

	private static class AdaBoard
	{
		int width;
		int height;
		CellType cells[][];
		
		public AdaBoard(int width,int height)
		{
			this.width = width;
			this.height = height;
			cells = new CellType[width][height];
			
			for (int x = 0 ; x < width ; ++x) { for (int y = 0 ; y < height ; ++y) {
				cells[x][y] = CellType.EMPTY;
			}}
		}
		
		public AdaBoard(AdaBoard right)
		{
			this(right.width,right.height);
			for (int x = 0 ; x < width ; ++x) { for (int y = 0 ; y < height ; ++y) {
				cells[x][y] = right.cells[x][y];
			}}
		}	
	
		public boolean onBoard(int x, int y)
		{
			if (x < 0 || x >= width || y < 0 || y >= height) return false;
			return true;
		}
	
		public CellType getCell(int x,int y)
		{
			return cells[x][y];
		}
		
		public void setCell(int x,int y,CellType ct)
		{
			cells[x][y] = ct;
		}
		
		public void ShowBoard()
		{
			for (int y = 0 ; y < height ; ++y)
			{
				for (int x = 0 ; x < width ; ++x)
				{
					System.out.print(cells[x][y].getShow());
				}
				System.out.println("");
			}
		}
		
		public boolean isSolution()
		{
			for (int x = 0 ; x < width ; ++x) { for (int y = 0 ; y < height ; ++y) {
				if (!cells[x][y].isTerminal()) return false;
			}}
			return true;
		}
		
		public void AddNoBulb(int x,int y)
		{
			cells[x][y] = CellType.NOBULB;
		}
		
		public boolean IsLightLegal(int x,int y)
		{
			if (cells[x][y] != CellType.EMPTY) return false;
			for (Point dir : deltas)
			{
				int idx = 1;
				while(true)
				{
					Point p = new Point(x+dir.x*idx,y+dir.y*idx);
					if (!onBoard(p.x,p.y)) break;
					if (cells[p.x][p.y].blocksLight()) break;
					if (cells[p.x][p.y] == CellType.BULB) return false;
					++idx;
				}
			}
			return true;
		}
		
		
		public boolean AddLight(int x,int y)
		{
			cells[x][y] = CellType.BULB;
			
			for (Point dir : deltas)
			{
				int idx = 1;
				while(true)
				{
					Point p = new Point(x+dir.x*idx,y+dir.y*idx);
					if (!onBoard(p.x,p.y)) break;
					if (cells[p.x][p.y].blocksLight()) break;
					if (cells[p.x][p.y].lightIsBad()) return false;
					cells[p.x][p.y] = CellType.LIT;
					++idx;
				}
			}
			return true;
		}
	}
	
	private enum LogicStatus { LOGIC,STYMIED,CONTRADICTION };

	private static void AddToLights(Map<Point,Vector<Point>> lights,Point light,Point target)
	{
		if (!lights.containsKey(target))
		{
			lights.put(target,new Vector<Point>());
		}
		lights.get(target).add(light);
	}
	
	public static LogicStatus ApplyLogic(AdaBoard theBoard)
	{
		int numChanges = 0;
		// are there any EMPTY or NOBULB spaces lightable by only one empty space?
		//    make the light that lights that empty space -> LOGIC
		
		// algorithm...for every space that could be a light, find every lightable but unlit space
		// that could be lit by that point, and record for each such space from where it is lit.
		Map<Point,Vector<Point>> lights = new HashMap<Point,Vector<Point>>();
		for (int x = 0 ; x < theBoard.width ; ++x)
		{
			for (int y = 0 ; y < theBoard.height ; ++y)
			{
				if (!theBoard.IsLightLegal(x,y)) continue;
				Point lightBringer = new Point(x,y);
				AddToLights(lights,lightBringer,lightBringer);
				for (Point delta : deltas)
				{
					int idx = 1;
					while(true)
					{
						Point target = new Point(lightBringer.x + idx * delta.x,lightBringer.y + idx * delta.y);
						if (!theBoard.onBoard(target.x,target.y)) break;
						if (theBoard.cells[target.x][target.y].blocksLight()) break;
						if (theBoard.cells[target.x][target.y].needsLight()) AddToLights(lights,lightBringer,target);
						++idx;
					}
				}
			}
		}
		
		for (Map.Entry<Point,Vector<Point>> ent : lights.entrySet())
		{
			if (ent.getValue().size() > 1) continue;
			Point p = ent.getValue().firstElement();
			if (!theBoard.AddLight(p.x,p.y)) return LogicStatus.CONTRADICTION;
			++numChanges;
		}
		

		// for all NOBULB spaces, they should have a presence in lights, or they can't be lit
		// (EMPTY spaces can self-light and so should be present)
		for (int x = 0 ; x < theBoard.width ; ++x)
		{
			for (int y = 0 ; y < theBoard.height ; ++y)
			{		
				if (!theBoard.cells[x][y].requiresExternalLight()) continue;
				if (!lights.containsKey(new Point(x,y))) return LogicStatus.CONTRADICTION;
			}
		}
		
		
		// for each affectsAdjacents space
		//    are there too many LIT or NOBULB adjacents? -> CONTRADICTION
		//    are there too many BULB adjacents? -> CONTRADICTION
		//    are there exactly the right number of LIT or NOBULB adjacents?  the rest must be BULBS -> LOGIC
		//    are there exactly the right number of BULBs?  the rest must be NOBULB -> LOGIC
		for (int x = 0 ; x < theBoard.width ; ++x)
		{
			for (int y = 0 ; y < theBoard.height ; ++y)
			{		
				if (!theBoard.cells[x][y].affectsAdjacents()) continue;
				int expectedNA = theBoard.cells[x][y].numAdjacents();
				Vector<Point> bulbs = new Vector<Point>();
				Vector<Point> nobulbs = new Vector<Point>();
				Vector<Point> empties = new Vector<Point>();
				for (Point delta : deltas)
				{
					Point nadj = new Point(x+delta.x,y+delta.y);
					if (!theBoard.onBoard(nadj.x,nadj.y)) continue;
					if (theBoard.cells[nadj.x][nadj.y] == CellType.BULB) bulbs.add(nadj);
					if (theBoard.cells[nadj.x][nadj.y].neverBulb()) nobulbs.add(nadj);
					if (theBoard.cells[nadj.x][nadj.y] == CellType.EMPTY) empties.add(nadj);
				}
		
				// exemplar cases:
				// 1 in corner
				//	0 bulbs, 0 empties -- do nothing
				//  1 bulbs -- all empties made NOBULB
				//  1 NOBULB 1 empty -- empty made bulbs
				// 2 bulbs or 2 NOBULB -- CONTRADICTION
		
				if (bulbs.size() > expectedNA) return LogicStatus.CONTRADICTION;
				if (bulbs.size() + empties.size() < expectedNA) return LogicStatus.CONTRADICTION;
				if (bulbs.size() == expectedNA)
				{
					for (Point empty : empties)
					{
						theBoard.AddNoBulb(empty.x,empty.y);
						++numChanges;
					}
				}
				if (bulbs.size() + empties.size() == expectedNA)
				{
					for (Point empty : empties)
					{
						if (!theBoard.AddLight(empty.x,empty.y)) return LogicStatus.CONTRADICTION;
						++numChanges;
					}
				}					
			}
		}
		return numChanges > 0 ? LogicStatus.LOGIC : LogicStatus.STYMIED;
	}
	
	public static void MakeGuess(AdaBoard tb,Vector<AdaBoard> queue)
	{
		for (int x = 0 ; x < tb.width ; ++x)
		{
			for (int y = 0 ; y < tb.height ; ++y)
			{
				if (!tb.cells[x][y].isFillable()) continue;
				AdaBoard lb = new AdaBoard(tb);
				AdaBoard db = new AdaBoard(tb);
				db.AddNoBulb(x,y);
				queue.add(db);
				if(tb.IsLightLegal(x,y))
				{
					lb.AddLight(x,y);
					queue.add(lb);
				}
				return;
			}
		}
	}
	
	
	
	
	
	public static Vector<AdaBoard> Solve(AdaBoard startBoard)
	{
		Vector<AdaBoard> solutions = new Vector<AdaBoard>();
		Vector<AdaBoard> queue = new Vector<AdaBoard>();
		
		queue.add(startBoard);
		
		while(queue.size() > 0)
		{
			System.out.println("Queue Size: " + queue.size() + " Solution Size: " + solutions.size());
			AdaBoard curb = queue.remove(0);
			if (curb.isSolution())
			{
				solutions.add(curb);
				continue;
			}
			LogicStatus lstat = ApplyLogic(curb);
			switch (lstat)
			{
			case LOGIC: 
				queue.add(curb);
				break;
			case STYMIED:
				MakeGuess(curb,queue);
				break;
			case CONTRADICTION:
				break;
			}
		}
		return solutions;
	}
	
	
	
	public static void main(String[] args)
	{
		AdaBoard theBoard = new AdaBoard(21,12);
		theBoard.setCell(0,1,CellType.TWO);
		theBoard.setCell(0,9,CellType.BLOCK);
		theBoard.setCell(1,2,CellType.ONE);
		theBoard.setCell(1,9,CellType.TWO);
		theBoard.setCell(2,7,CellType.BLOCK);
		theBoard.setCell(3,4,CellType.TWO);
		theBoard.setCell(3,8,CellType.ONE);
		theBoard.setCell(3,10,CellType.BLOCK);
		theBoard.setCell(4,0,CellType.BLOCK);
		theBoard.setCell(4,1,CellType.ZERO);
		theBoard.setCell(4,5,CellType.TWO);
		theBoard.setCell(4,8,CellType.BLOCK);
		theBoard.setCell(4,10,CellType.BLOCK);
		theBoard.setCell(5,2,CellType.BLOCK);
		theBoard.setCell(5,3,CellType.TWO);
		theBoard.setCell(5,10,CellType.ONE);
		theBoard.setCell(6,8,CellType.ZERO);
		theBoard.setCell(7,2,CellType.BLOCK);
		theBoard.setCell(7,6,CellType.BLOCK);
		theBoard.setCell(7,9,CellType.BLOCK);
		theBoard.setCell(8,3,CellType.ONE);
		theBoard.setCell(8,4,CellType.BLOCK);
		theBoard.setCell(9,6,CellType.BLOCK);
		theBoard.setCell(9,7,CellType.ONE);
		theBoard.setCell(9,11,CellType.ONE);
		theBoard.setCell(10,1,CellType.ONE);
		theBoard.setCell(10,2,CellType.BLOCK);
		theBoard.setCell(10,3,CellType.TWO);
		theBoard.setCell(10,10,CellType.BLOCK);
		theBoard.setCell(10,11,CellType.BLOCK);
		theBoard.setCell(11,6,CellType.TWO);
		theBoard.setCell(11,7,CellType.BLOCK);
		theBoard.setCell(11,11,CellType.ONE);
		theBoard.setCell(12,3,CellType.BLOCK);
		theBoard.setCell(12,4,CellType.BLOCK);
		theBoard.setCell(13,2,CellType.ONE);
		theBoard.setCell(13,6,CellType.BLOCK);
		theBoard.setCell(13,9,CellType.BLOCK);
		theBoard.setCell(14,8,CellType.TWO);
		theBoard.setCell(15,2,CellType.BLOCK);
		theBoard.setCell(15,3,CellType.BLOCK);
		theBoard.setCell(15,10,CellType.ONE);
		theBoard.setCell(16,0,CellType.BLOCK);
		theBoard.setCell(16,1,CellType.ONE);
		theBoard.setCell(16,5,CellType.BLOCK);
		theBoard.setCell(16,8,CellType.ZERO);
		theBoard.setCell(16,10,CellType.BLOCK);
		theBoard.setCell(17,4,CellType.THREE);
		theBoard.setCell(17,8,CellType.BLOCK);
		theBoard.setCell(17,10,CellType.ONE);
		theBoard.setCell(18,7,CellType.THREE);
		theBoard.setCell(19,2,CellType.BLOCK);
		theBoard.setCell(19,9,CellType.BLOCK);
		theBoard.setCell(20,1,CellType.ZERO);
		theBoard.setCell(20,9,CellType.ZERO);
	
		theBoard.ShowBoard();
		
		Vector<AdaBoard> solutions = Solve(theBoard);
		for (AdaBoard ab : solutions)
		{
			System.out.println("");
			ab.ShowBoard();
		}
	}
}
	