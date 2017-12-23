
import java.util.*;
import java.awt.Point;

public class Position
{
	private class PositionCellInfo
	{
		Set<Integer> numbers = new HashSet<Integer>();
		int lockdownNumber = -1;
		
		public PositionCellInfo() {}
		public PositionCellInfo(PositionCellInfo right)
		{
			lockdownNumber = right.lockdownNumber;
			for (int x  : right.numbers) { numbers.add(x); }
		}
	}


	PositionCellInfo[][] cellinfo;
	AdaBoard theBoard;
	private Point[] deltas = new Point[] { new Point(1,0),new Point(0,1), new Point(-1,0), new Point(0,-1)};
	
	public Position(Position right)
	{
		theBoard = right.theBoard;
		cellinfo = new PositionCellInfo[theBoard.width][theBoard.height];

		for (int x = 0 ; x < theBoard.width ; ++x)
		{
			for (int y = 0 ; y < theBoard.height ; ++y)
			{
				cellinfo[x][y] = new PositionCellInfo(right.cellinfo[x][y]);
			}
		}
	}
	
	public Position(AdaBoard theBoard)
	{
		cellinfo = new PositionCellInfo[theBoard.width][theBoard.height];
		this.theBoard = theBoard;
		
		for (int x = 0 ; x < theBoard.width ; ++x)
		{
			for (int y = 0 ; y < theBoard.height ; ++y)
			{
				cellinfo[x][y] = new PositionCellInfo();
				AdaBoard.Region region = theBoard.cells[x][y];
				for (int i = 1 ; i <= region.cellV.size() ; ++i) { cellinfo[x][y].numbers.add(i); }
			}
		}
		if (!ProcessLockdowns()) throw new RuntimeException("Board Cannot be Solved!");
	}
	
	private boolean ProcessOneCell(Vector<Point> queue,int lockdownNumber,Point p)
	{
		PositionCellInfo psi = cellinfo[p.x][p.y];
		int preNum = psi.numbers.size();
		psi.numbers.remove(lockdownNumber);
		if (preNum == psi.numbers.size()) return true;
		if (psi.numbers.size() == 0) return false;
		if (psi.numbers.size() == 1 && psi.lockdownNumber == -1) queue.add(p);
		return true;
	}
	
	
	
	public boolean ProcessLockdowns()
	{
		// this queue contains cells that 
		// a) have exactly 1 number in numbers
		// b) lockdownNumber is -1
		// we can assume that the board is legal prior to starting this work
		Vector<Point> queue = new Vector<Point>();
		for (int x = 0 ; x < theBoard.width ; ++x)
		{
			for (int y = 0 ; y < theBoard.height; ++y)
			{
				if (cellinfo[x][y].numbers.size() == 1 && cellinfo[x][y].lockdownNumber == -1) queue.add(new Point(x,y)); 
			}
		}
		
		// now the real work begins.
		while(queue.size() > 0)
		{
			Point curP = queue.remove(0);
			PositionCellInfo psi = cellinfo[curP.x][curP.y];
			AdaBoard.Region region = theBoard.cells[curP.x][curP.y];
			if (psi.numbers.size() == 0) return false;
			if (psi.lockdownNumber != -1) continue;
			psi.lockdownNumber = psi.numbers.iterator().next();
			for (Point regionP  : region.cellV)
			{
				if (regionP.equals(curP)) continue;
				if (!ProcessOneCell(queue,psi.lockdownNumber,regionP)) return false;
			}
			
			for (Point delta : deltas)
			{
				for (int idx = 1 ; idx <= psi.lockdownNumber ; ++idx)
				{
					Point nearbyP = new Point(curP.x + delta.x * idx , curP.y + delta.y * idx);
					if (!theBoard.isOnBoard(nearbyP)) break;
					if (!ProcessOneCell(queue,psi.lockdownNumber,nearbyP)) return false;
				}
			}
		}
		return true;
	}
	
	public Collection<AdaBoard.Region> getRegions() { return theBoard.regionsById.values(); }
	public int getWidth() { return theBoard.width; }
	public int getHeight() { return theBoard.height; }
	public boolean isOnBoard(Point p) { return theBoard.isOnBoard(p); }
	
	public Collection<Integer> GetCellNumbers(int x,int y) { return cellinfo[x][y].numbers; }
	public int GetCellNumber(int x,int y) { return cellinfo[x][y].lockdownNumber; }
	
	public boolean SetOneCell(int x,int y,int number)
	{
		PositionCellInfo psi = cellinfo[x][y];
		if (!psi.numbers.contains(number)) return false;
		psi.numbers = new HashSet<Integer>();
		psi.numbers.add(number);
		return ProcessLockdowns();
	}
	
	public boolean isSolved()
	{
		for (int x = 0 ; x < getWidth() ; ++x)
		{
			for (int y = 0 ; y < getHeight() ; ++y)
			{
				if (GetCellNumber(x,y) == -1) return false;
			}
		}
		return true;
	}
	
	
}
		
					
			
				
			
			
	