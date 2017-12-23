

import java.util.*;
import java.awt.Point;

public class Solver
{
	Vector<Numbers> solutions = new Vector<Numbers>();

	private LogicStatus ApplyLogic(ClueSet cs,Numbers nums)
	{
		LogicStatus result = LogicStatus.STYMIED;
		
		for (ClueSet.Clue clue : cs.clues)
		{
			LogicStatus stat = clue.Apply(nums);
			if (stat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
			if (stat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
		}
		return result;
	}
	
	private void MakeGuess(PriorityQueue<Numbers> q, ClueSet cs,Numbers nums)
	{
		Point smallest = null;
		int count = 8;
		
		for (int x = 0 ; x < nums.width ; ++x)
		{
			for (int y = 0 ; y < nums.height ; ++y)
			{
				if (nums.numbers[x][y] == null) continue;
				if (nums.numbers[x][y].size() == 1) continue;
				if (nums.numbers[x][y].size() < count)
				{
					count = nums.numbers[x][y].size();
					smallest = new Point(x,y);
				}
			}
		}
		
		Numbers.IntSet is = nums.numbers[smallest.x][smallest.y];
		for (int i : is)
		{
			Numbers newnum = new Numbers(nums);
			newnum.numbers[smallest.x][smallest.y] = new Numbers.IntSet();
			newnum.numbers[smallest.x][smallest.y].add(i);
			q.add(newnum);
		}
	}
			
	private class UnsolvedComparator implements Comparator<Numbers>
	{
		@Override
		public int compare(Numbers x, Numbers y)
		{
			return y.guessdepth - x.guessdepth;
		}
	}	
	
	
	
	
	
	public Solver(ClueSet cs, Numbers nums)
	{
//		Vector<Numbers> queue = new Vector<Numbers>();
		PriorityQueue<Numbers> queue = new PriorityQueue<Numbers>(new UnsolvedComparator());
		queue.add(nums);
		
		while(queue.size() > 0)
		{
			System.out.print("Queue Size: " + queue.size() + " solution size: " + solutions.size());
//			Numbers curn = queue.remove(0);
			Numbers curn = queue.poll();
			
			System.out.println(" depth = " + curn.guessdepth);
						
			LogicStatus lstat = ApplyLogic(cs,curn);
//			System.out.println("Apply: " + lstat);
			switch (lstat)
			{
				case CONTRADICTION:
					break;
				case LOGICED:
					queue.add(curn);
					break;
				case STYMIED:
					if (curn.isSolution())
					{
						System.out.println("Stymied but is solution");
						solutions.add(curn);
						return;
					}
					else
					{
						MakeGuess(queue,cs,curn);
					}
					break;
			}
		}
	}
}
			