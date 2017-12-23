
import java.util.*;
import java.awt.Point;

public class GalaxyAI
{
	Vector<GalaxyBoard> queue = new Vector<GalaxyBoard>();
	Vector<GalaxyBoard> solutions = new Vector<GalaxyBoard>();
	GalaxyBoardAreaOverlays gbao = null;

	enum LogicStatus { SOLVED, CONTRADICTION, LOGICED, STYMIED };
	
	public GalaxyAI(GalaxyBoard theBoard)
	{
		queue.add(new GalaxyBoard(theBoard));
	}
	
	public void Run()
	{
		while(RunOne());
	}
	
	public boolean RunOne()
	{
		if (queue.size() == 0) return false;
		System.out.println("RunOne start: " + queue.size() );
		GalaxyBoard theBoard = queue.remove(0);
				
		LogicStatus lstat = ApplyLogic(theBoard);
		System.out.println("Logic Status: " + lstat);
		if (lstat == LogicStatus.SOLVED)
		{
			solutions.add(theBoard);
		}
		else if (lstat == LogicStatus.CONTRADICTION)
		{
			// do nothing
		}
		else if (lstat == LogicStatus.LOGICED)
		{
			queue.add(theBoard);
		}
		else // STYMIED
		{
			Guess(theBoard);
		}
		
		return true;
	}
	
	// class variables for communicating with anonymous inner classes
	boolean solved;
	boolean isGlobalContradiction;
	boolean isContradiction;
	boolean isStymied;

	private LogicStatus ApplyLogic(GalaxyBoard theBoard)
	{
		// step 1) detect if we're solved.
		solved = true;
		ForEachCell.op(theBoard.getWidth(),theBoard.getHeight(),new ForEachCell.CellOp() {
			public boolean op(int x,int y)
			{
				if (theBoard.getBoardCell(x,y) == null)
				{
					solved = false;
					return false;
				}
				return true;
			}
		});
		if (solved) return LogicStatus.SOLVED;
		
		// we're not solved.  calculate overlay state.
		gbao = new GalaxyBoardAreaOverlays(theBoard);

		// if the overlays fail to cover the whole board, we're in trouble.
		isGlobalContradiction = false;
		ForEachCell.op(theBoard.getWidth(),theBoard.getHeight(),new ForEachCell.CellOp() {
			public boolean op(int x,int y)
			{
				if (gbao.cellstates[x][y] == null)
				{
					isGlobalContradiction = true;
					return false;
				}
				return true;
			}
		});
		if (isGlobalContradiction) return LogicStatus.CONTRADICTION;
		
		isStymied = true;
		// if any of the areas are disconnected, we're in trouble.
		// if not, we can start mapping stuff back onto our incoming board.
		// start with the required connectors of each area.
		for (Map.Entry<Integer,GalaxyBoard> ent : gbao.overlays.entrySet())
		{
			if (!AISupport.isAreaConnected(ent.getValue(),ent.getKey())) return LogicStatus.CONTRADICTION;
			Vector<Point> connectors = AISupport.getConnectors(gbao,ent.getKey());
			GalaxyBoard.Area area = theBoard.getAreaById(ent.getKey());
			for (Point p : connectors)
			{
				boolean status = area.addCell(p.x,p.y);
				// the only way this is okay is if we did this to ourselves already...
				if (status == false)
				{
					GalaxyBoard.Area.Pair pair = theBoard.getBoardCell(p.x,p.y);
					if (pair == null || pair.getAreaId() != ent.getKey()) return LogicStatus.CONTRADICTION;
					continue;
				}
				GalaxyBoard.Area.Pair newpair = theBoard.getBoardCell(p.x,p.y);
				System.out.println("Required Connector of Area: " + newpair.getAreaId() + ": " + newpair.p1 + " - " + newpair.p2);
				isStymied = false;
			}	
		}
		
		// also, any cell in the overlay compilation that is not isSource but only has 1 
		// possible area can be included...have to watch for the same thing, that the prior
		// addition of cells does not make any of these single-area cells impossible.
		isContradiction = false;
		ForEachCell.op(theBoard.getWidth(),theBoard.getHeight(),new ForEachCell.CellOp() {
			public boolean op(int x, int y)
			{
				GalaxyBoardAreaOverlays.OverlayState os = gbao.cellstates[x][y]; // we know this is non-null
				if (os.isSource) return true;
				if (os.areaIds.size() > 1) return true;
				int areaid = (Integer)(os.areaIds.toArray()[0]);
				GalaxyBoard.Area area = theBoard.getAreaById(areaid);
				boolean status = area.addCell(x,y);
				if (status == false)
				{
					GalaxyBoard.Area.Pair pair = theBoard.getBoardCell(x,y);
					if (pair == null || pair.getAreaId() != areaid) 
					{
						isContradiction  = true;
						return false;
					}
					return true;
				}
				
				GalaxyBoard.Area.Pair newpair = theBoard.getBoardCell(x,y);
				System.out.println("Singular Area: " + newpair.getAreaId() + ": " + newpair.p1 + " - " + newpair.p2);				
				
				isStymied = false;
				return true;
			}
		});
			
		if (isContradiction) return LogicStatus.CONTRADICTION;
		if (isStymied) return LogicStatus.STYMIED;
		return LogicStatus.LOGICED;
	}
	
	// class variables for communicating with anonymous inner class
	int gx;
	int gy;
	int bestGuess;
	
	
	// invariants:
	// applyLogic returned STYMIED, which means that it did not return CONTRADICTION or SOLVED
	// STYMIED means that applyLogic added no cells to theBoard
	// also, gbao is a valid GalaxyBoardAreaOverlays
	public void Guess(GalaxyBoard theBoard)
	{
		gx = -1;
		gy = -1;
		bestGuess = theBoard.numAreas() + 1; // this is obviously too large.
		
		ForEachCell.op(theBoard.getWidth(),theBoard.getHeight(),new ForEachCell.CellOp() {
			public boolean op(int x,int y)
			{
				GalaxyBoardAreaOverlays.OverlayState os = gbao.cellstates[x][y]; // we know this is non-null
				if (os.isSource) return true;
				
				if (os.areaIds.size() < bestGuess)
				{
					gx = x;
					gy = y;
					bestGuess = os.areaIds.size();
				}
				return true;
			}
		});
	
		System.out.println("Guess Executed: " + gx + "," + gy + ": " + bestGuess);
	
		// we know that this cell has at least 2 entries.
		GalaxyBoardAreaOverlays.OverlayState os = gbao.cellstates[gx][gy];
		for(int areaid : os.areaIds)
		{
			GalaxyBoard newBoard = new GalaxyBoard(theBoard);
			GalaxyBoard.Area area = newBoard.getAreaById(areaid);
			area.addCell(gx,gy);
			queue.add(newBoard);
		}
	}
			
	
}





