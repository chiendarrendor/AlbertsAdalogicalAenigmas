
import java.util.*;
import java.awt.Point;

public class Solver
{
	Vector<Position> solutions = new Vector<Position>();
	Vector<Position> queue = new Vector<Position>();
	
	private Point FindUnsolved(Position pos)
	{
		int smallCount = pos.getWidth() * pos.getHeight() + 1;
		Point smallPoint = null;
	
	
		for (int sum = 0 ; sum <= pos.getWidth() + pos.getHeight() ; ++sum)
		{
			for (int idx = 0 ; idx <= sum ; ++idx)
			{
				Point p = new Point(sum-idx,idx);
				if (!pos.isOnBoard(p)) continue;
				if (pos.GetCellNumber(p.x,p.y) != -1) continue;
				int mycount = pos.GetCellNumbers(p.x,p.y).size();
				if (mycount < smallCount)
				{
					smallCount = mycount;
					smallPoint = p;
				}
			}
		}
		return smallPoint; // null should never happen on an unsolved board
	}
	
	public int FindRegionUniques(Position curP,AdaBoard.Region region)
	{
		int result = 0;
		Map<Integer,Vector<Point>> intPoints = new HashMap<Integer,Vector<Point>>();
		
		for (Point p : region.cellV)
		{
			if (curP.GetCellNumber(p.x,p.y) != -1) continue;
			for (int i : curP.GetCellNumbers(p.x,p.y))
			{
				if (!intPoints.containsKey(i)) intPoints.put(i,new Vector<Point>());
				intPoints.get(i).add(p);
			}
		}
		for (Map.Entry<Integer,Vector<Point>> ent : intPoints.entrySet())
		{
			if (ent.getValue().size() > 1) continue;
			Point theP = ent.getValue().firstElement();
			if (!curP.SetOneCell(theP.x,theP.y,ent.getKey())) return -1;
			++result;
		}
		return result;
	}
	
	
	
	
	
	
	public int FindRegionsUniques(Position curP)
	{
		int result = 0;
		for (AdaBoard.Region region : curP.getRegions())
		{
			int lr = FindRegionUniques(curP,region);
			if (lr == -1) return -1;
			result += lr;
		}
		return result;
	}
	
	public Solver(Position first)
	{
		if (first.isSolved())
		{
			solutions.add(first);
			return;
		}
		queue.add(first);
	}
	
	public void Run()
	{
		while(queue.size() > 0)
		{
			System.out.println("Queue size: " + queue.size() + " Solution Size: " + solutions.size());
			Position curP = queue.remove(0);
			
			int status = FindRegionsUniques(curP);
			if (status == -1) continue; // board was found to contain a contradiction
			if (status > 0) { queue.add(curP); continue; } // returns # of changes made
			
			// This section here is the 'stymied case'
			Point action = FindUnsolved(curP);
			Collection<Integer> numbers = curP.GetCellNumbers(action.x,action.y);
			for (int num : numbers)
			{
				Position newP = new Position(curP);
				if (!newP.SetOneCell(action.x,action.y,num)) continue;
				if (newP.isSolved()) solutions.add(newP);
				else queue.add(newP);
			}
		}
	}
}
				
				