import grid.file.GridFileReader;
import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;

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
		GridFileReader gfr = null;

		public char getLetter(int x,int y) {
			if (gfr == null) return '\0';
			return gfr.getBlock("LETTERS")[x][y].charAt(0);
		}

		public boolean hasLetter(int x,int y) {
			char let = getLetter(x,y);
			return let != '\0' && let != '.';
		}

		public AdaBoard(String fname) {
			gfr = new GridFileReader(fname);
			width = gfr.getWidth();
			height = gfr.getHeight();
			cells = new CellType[width][height];

			for (int y = 0 ; y < height ; ++y) {
				for (int x = 0; x < width ; ++x) {
					CellType ct = CellType.EMPTY;
					char clue = gfr.getBlock("CLUES")[x][y].charAt(0);
					switch(clue) {
						case '0': ct = CellType.ZERO; break;
						case '1': ct = CellType.ONE; break;
						case '2': ct = CellType.TWO; break;
						case '3': ct = CellType.THREE; break;
						case '4': ct = CellType.FOUR; break;
						case '%': ct = CellType.BLOCK; break;
						case '.': ct = CellType.EMPTY; break;
						default:
							throw new RuntimeException("Unknown character " + clue);
					}
					setCell(x,y,ct);
				}
			}

		}

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
			this.gfr = right.gfr;

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

			LogicStatus lstat = ApplyLogic(curb);
			if (lstat == LogicStatus.CONTRADICTION) continue;

			if (curb.isSolution())
			{
				solutions.add(curb);
				continue;
			}

			switch (lstat)
			{
			case LOGIC: 
				queue.add(curb);
				break;
			case STYMIED:
				MakeGuess(curb,queue);
				break;
			}
		}
		return solutions;
	}

	private static int numDirLit(AdaBoard board, int x, int y) {
		int result = 0;
		for (Direction d: Direction.orthogonals()) {
			Point np = d.delta(x,y,1);
			if (board.onBoard(np.x,np.y) && board.getCell(np.x,np.y) == CellType.LIT) ++result;
		}
		return result;
	}
	
	
	public static void main(String[] args)
	{
		if (args.length != 1) {
			System.out.println("Bad command line");
			System.exit(1);
		}

		AdaBoard newboard = new AdaBoard(args[0]);

		Vector<AdaBoard> solutions = Solve(newboard);

		/*
		AdaBoard commonboard = new AdaBoard(newboard.width,newboard.height);
		for (int y = 0 ; y < commonboard.height;  ++y) {
			for (int x = 0 ; x < commonboard.width ; ++x) {
				if ( x == 20 && y == 3) {
					System.out.println("Debug:");
					for (AdaBoard ab : solutions) { System.out.println(ab.getCell(x,y)); }
					System.out.println("---");
				}
				CellType ct = null;
				for (AdaBoard ab : solutions) {
					if (ct == null) ct = ab.getCell(x,y);
					else if (ab.getCell(x,y) != ct) {
						ct = null;
						break;
					}
				}
				if (ct == null) continue;
				commonboard.setCell(x,y,ct);
			}
		}

		System.out.println("intersection of all solutions");
		commonboard.ShowBoard();
		System.exit(1);
*/


		for (AdaBoard ab : solutions)
		{
			System.out.println("");
			ab.ShowBoard();

			System.out.print("QUOTE: ");
			for (int y = 0 ; y < ab.height ; ++y) {
				for (int x = 0 ; x < ab.width ; ++x) {
					if (ab.getCell(x,y) != CellType.BULB) continue;
					char c = ab.getLetter(x,y);
					if (c == '\0') continue;

					int numcount = 0;
					if (ab.gfr.getVar("CLUETYPE").equals("NUMDIRLIT")) {
						for (Direction d: Direction.orthogonals()) {
							Point np = d.delta(x,y,1);
							if (ab.onBoard(np.x,np.y) && ab.getCell(np.x,np.y) == CellType.LIT) ++numcount;
						}
					}



					System.out.print(LetterRotate.Rotate(c,numcount));
				}
			}
			System.out.println("");
		}
	}
}
	