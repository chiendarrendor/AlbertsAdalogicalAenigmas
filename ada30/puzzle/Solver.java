import java.util.*;

public class Solver
{
	Vector<Board> solutions = new Vector<Board>();
	
	public Solver(Board ground,List<Board> pieces)
	{
		Vector<PuzzleBoard> queue = new Vector<PuzzleBoard>();
		queue.add(new PuzzleBoard(ground,pieces));
		int startingcount = pieces.size();
		
		while(queue.size() > 0)
		{
			PuzzleBoard pb = queue.remove(0);
			System.out.println("Queue Size: " + queue.size() + " Solution Size: " + solutions.size() + " depth: " + (startingcount - pb.unusedBoards.size()));
			List<PuzzleBoard> succ = pb.Successors();
			if (succ == null)
			{
				solutions.add(pb.theBoard);
				continue;
			}
			
			for (PuzzleBoard npb : succ) queue.insertElementAt(npb,0);
		}
	}
}