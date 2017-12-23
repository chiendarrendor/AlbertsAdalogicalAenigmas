
import java.util.*;
import java.awt.Point;

public class Solver
{
	Vector<Board>  solutions = new Vector<Board>();
	Vector<Board> rejections = new Vector<Board>();
	
	private class UnsolvedComparator implements Comparator<Board>
	{
		@Override
		public int compare(Board x, Board y)
		{
			return y.depth - x.depth;
		}
	}
	
	public Solver(Board b)
	{
		PriorityQueue<Board> queue = new PriorityQueue<Board>(new UnsolvedComparator());
		//Vector<Board> queue = new Vector<Board>();
		queue.add(b);
		
		while(queue.size() > 0)
		{
			System.out.print("Queue Size: " + queue.size() + " solution size: " + solutions.size() + " rejections size " + rejections.size() );
			Board curBoard = queue.poll();
			System.out.println("  depth: " + curBoard.depth);
//			Board tBoard = new Board(curBoard);
			
			
			LogicStatus atstatus = AntiTedium.ApplyAntiTedium(curBoard);
			if (atstatus == LogicStatus.CONTRADICTION) 
			{
				continue;
			}
						
			LogicStatus tcstatus = TileConnectivity.UpdateTileConnectivity(curBoard);
			if (tcstatus == LogicStatus.CONTRADICTION) 
			{
				continue;
			}
			
			if (curBoard.isSolved() && AntiTedium.ApplyAntiTedium(curBoard) != LogicStatus.CONTRADICTION)
			{
				solutions.add(curBoard);
				continue;
			}


			
			if (atstatus == LogicStatus.LOGICED || tcstatus == LogicStatus.LOGICED) 
			{
				queue.add(curBoard);
				continue;
			}




			
			
			Point empty = curBoard.findEmpty();
			
//			System.out.println("--Guessed Point: " + empty);

			Board tileb = new Board(curBoard);
			Board treeb = new Board(curBoard);
			tileb.addTile(empty.x,empty.y);
			treeb.addTree(empty.x,empty.y);
			queue.add(tileb);
			queue.add(treeb);
		}
	}
}
		