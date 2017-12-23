import java.util.*;

public class PuzzleBoard
{
	Board theBoard;
	Vector<Board> unusedBoards = new Vector<Board>();
	
	public PuzzleBoard(Board b,List<Board> unused) 
	{ 
		theBoard = new Board(b);
		for (Board ub: unused) { unusedBoards.add(ub); }
	}
	
	public PuzzleBoard(PuzzleBoard right)
	{
		theBoard = new Board(right.theBoard);
		for (Board ub : right.unusedBoards) { unusedBoards.add(ub); }
	}


	public boolean isAddable(Board source, int dx,int dy)
	{
		Board dest = theBoard;
		for (int x = 0 ; x < source.width ; ++x)
		{
			int ix = x + dx;
			if (ix < 0 || ix >= dest.width) return false;
			
			for (int y = 0 ; y < source.height ; ++y)
			{
				int iy = y + dy;
				if (iy < 0 || iy >= dest.height) return false;
				
				if (!source.isCellOn(x,y)) continue;
				if (dest.isCellOn(ix,iy)) return false;
			}
		}
		return true;
	}
	
	public boolean add(Board source, int dx,int dy)
	{
		Board dest = theBoard;	
		if (!isAddable(source,dx,dy)) return false;
		for (int x = 0 ; x < source.width ; ++x)
		{
			int ix = x + dx;
			
			for (int y = 0 ; y < source.height ; ++y)
			{
				int iy = y + dy;
				if (!source.isCellOn(x,y)) continue;	

				dest.cellIds[ix][iy] = source.cellIds[x][y];
				dest.cellColors[ix][iy] = source.cellColors[x][y];
				dest.dotLoc[ix][iy] = source.dotLoc[x][y];
			}
		}
		
		return true;
	}
	
	private class InvalidCellException extends RuntimeException 
	{
		InvalidCellException(String s) { super(s); }
	}
	
	
	// returns true if this is a dotted cell where the dot is not all on this cell.
	private boolean isMatchable(int x,int y)
	{
		if (!theBoard.isCellOn(x,y)) return false;
		DotColor myColor = theBoard.cellColors[x][y];
		if (myColor == DotColor.NONE) return false;
		int curloc = theBoard.dotLoc[x][y];
		return curloc != Circle.NONE && curloc != Circle.CENTER;
	}
	
	// assume that x,y isMatchable.
	// for the given off-cell dot location, how many of the corresponding
	// spaces on the board empty?
	private int numUnmatched(int x, int y)
	{
		int curloc = theBoard.dotLoc[x][y];
		int unmatchedcount = 0;
		switch(curloc)
		{
			case Circle.N: if (theBoard.isCellEmpty(x,y-1)) ++unmatchedcount; break;
			case Circle.S: if (theBoard.isCellEmpty(x,y+1)) ++unmatchedcount; break;
			case Circle.W: if (theBoard.isCellEmpty(x-1,y)) ++unmatchedcount; break;
			case Circle.E: if (theBoard.isCellEmpty(x+1,y)) ++unmatchedcount; break;
			case Circle.NW:
				if (theBoard.isCellEmpty(x-1,y)) ++unmatchedcount;
				if (theBoard.isCellEmpty(x,y-1)) ++unmatchedcount;
				if (theBoard.isCellEmpty(x-1,y-1)) ++unmatchedcount;
				break;
			case Circle.NE:
				if (theBoard.isCellEmpty(x+1,y-1)) ++unmatchedcount;
				if (theBoard.isCellEmpty(x+1,y)) ++unmatchedcount;
				if (theBoard.isCellEmpty(x,y-1)) ++unmatchedcount;
				break;				
			case Circle.SW:
				if (theBoard.isCellEmpty(x-1,y+1)) ++unmatchedcount;
				if (theBoard.isCellEmpty(x-1,y)) ++unmatchedcount;
				if (theBoard.isCellEmpty(x,y+1)) ++unmatchedcount;
				break;
			case Circle.SE:
				if (theBoard.isCellEmpty(x+1,y+1)) ++unmatchedcount;
				if (theBoard.isCellEmpty(x,y+1)) ++unmatchedcount;
				if (theBoard.isCellEmpty(x+1,y)) ++unmatchedcount;
				break;	
			default: throw new RuntimeException("Unknown Circle type!");
		}
		return unmatchedcount;
	}
	
	
	
	
	
	
	private void ValidateCell(int x,int y,DotColor color, int loc)
	{
//		System.out.println("Validating " + x + "," + y + " " + color + " " + loc);
		// due to the outer ring of floor, we can assume that we're never off the board.
		if (!theBoard.isCellOn(x,y)) return; // not invalid if the space is empty
		// if the space is on, it _must_ have the given criteria.
		if (theBoard.cellColors[x][y] != color) throw new InvalidCellException("cell " + x + "," + y + " is not colored " + color);
		if (theBoard.dotLoc[x][y] != loc) throw new InvalidCellException("cell " + x + "," + y + " does not have loc " + loc);
		return;
	}
	
	
	
	public boolean isValid()
	{
		try
		{
			for (int x = 0 ; x < theBoard.width ; ++x)
			{
				for (int y = 0 ; y < theBoard.height ; ++y)
				{
					if (!isMatchable(x,y)) continue;
					DotColor myColor = theBoard.cellColors[x][y];
					int curloc = theBoard.dotLoc[x][y];
					
					// if we get here, x,y is an on cell with a dot that touches an edge
					switch(curloc)
					{
						case Circle.N: ValidateCell(x,y-1,myColor,Circle.S); break;
						case Circle.S: ValidateCell(x,y+1,myColor,Circle.N); break;
						case Circle.W: ValidateCell(x-1,y,myColor,Circle.E); break;
						case Circle.E: ValidateCell(x+1,y,myColor,Circle.W); break;
						case Circle.NW:
							ValidateCell(x-1,y,myColor,Circle.NE);
							ValidateCell(x,y-1,myColor,Circle.SW);
							ValidateCell(x-1,y-1,myColor,Circle.SE);
							break;
						case Circle.NE:
							ValidateCell(x+1,y-1,myColor,Circle.SW);
							ValidateCell(x+1,y,myColor,Circle.NW);
							ValidateCell(x,y-1,myColor,Circle.SE);
							break;
						case Circle.SW:
							ValidateCell(x-1,y+1,myColor,Circle.NE);
							ValidateCell(x,y+1,myColor,Circle.NW);
							ValidateCell(x-1,y,myColor,Circle.SE);
							break;
						case Circle.SE:
							ValidateCell(x+1,y+1,myColor,Circle.NW);
							ValidateCell(x+1,y,myColor,Circle.SW);
							ValidateCell(x,y+1,myColor,Circle.NE);
							break;
						default: throw new RuntimeException("Unknown Circle type!");
					}
				}
			}
		}
		catch(InvalidCellException ice) 
		{
//			System.out.println(ice);
			return false; 
		}
		return true;
	}
	
	
	
	
	
	
	List<PuzzleBoard> Successors()
	{
		if (unusedBoards.size() == 0) return null;
		Vector<PuzzleBoard> successors = new Vector<PuzzleBoard>();
		
		// 1) find the first space on the board that is not fully matched.
		for (int tx = 0 ; tx < theBoard.width ; ++tx)
		{
			for (int ty = 0 ; ty < theBoard.height ; ++ty)
			{
				if (!isMatchable(tx,ty)) continue;
				int numunmatched = numUnmatched(tx,ty);
				if (numunmatched == 0) continue; 
	
				// okay, we have one.
				// 
				for (Board unused : unusedBoards)
				{
					for (Board img : unused.getImages())
					{
						for (int x = 1 ; x < theBoard.width - 1 ; x += 3)
						{
							for (int y = 1 ; y < theBoard.height - 1 ; y += 3)
							{
								if (!isAddable(img,x,y)) continue;
								PuzzleBoard successor = new PuzzleBoard(this);
								successor.unusedBoards.remove(unused);
								successor.add(img,x,y);
								if (!successor.isValid()) continue;	
								if (successor.numUnmatched(tx,ty) == numunmatched) continue;
								successors.add(successor);
							}
						}
					}
				}
				return successors;
			}
		}
		return null;
	}
}