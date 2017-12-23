
import java.util.*;
import java.awt.Point;


public class Logic
{
	Vector<State> solutions = new Vector<State>();
	Point[] deltas = new Point[] { new Point(1,0),new Point(-1,0),new Point(0,1),new Point(0,-1) };	
	
	private boolean ballCanTravel(Board b,State s, int ballx, int bally, Point dir)
	{
		if (!s.isBall(ballx,bally)) throw new RuntimeException("not a ball at " + ballx + "," + bally);
		int bdist = s.ballValue(ballx,bally);
		for (int i = 1 ; i <= bdist ; ++i)
		{
			int dx = ballx + dir.x * i;
			int dy = bally + dir.y * i;
			if (!s.onBoard(dx,dy)) return false;
			
			if (i == bdist && s.blocksFlightLast(dx,dy)) return false;
			if (i == bdist && b.hasTrap[dx][dy]) return false;
			
			if (i != bdist && s.blocksFlight(dx,dy)) return false;
			
			if (bdist == 1 && !s.isHole(dx,dy)) return false;
			
		}
		return true;
	}
	
	private void doFlight(Board b,State s, int ballx, int bally, Point dir)
	{
		if (!ballCanTravel(b,s,ballx,bally,dir)) throw new RuntimeException("trying an illegal flight!");
		
		// we don't have to check anything in this loop. ballCanTravel did it for us.
		int bdist = s.ballValue(ballx,bally);
		s.setFlight(ballx,bally);
		for (int i = 1 ; i <= bdist ; ++i)
		{
			int dx = ballx + dir.x * i;
			int dy = bally + dir.y * i;

			if (i == bdist) s.setBall(dx,dy,bdist-1);
			else s.setFlight(dx,dy);
		}
	}
	
	
	
	
	
	
	private LogicStatus ApplyLogic(Board b, State s)
	{
		LogicStatus result = LogicStatus.STYMIED;
		for (int x = 0 ; x < b.width ; ++x)
		{
			for (int y = 0 ; y < b.height ; ++y)
			{
				if (!s.isBall(x,y)) continue;
				Vector<Point> validDeltas = new Vector<Point>();
				for (Point dir : deltas)
				{
					if (ballCanTravel(b,s,x,y,dir)) validDeltas.add(dir);
				}
				if (validDeltas.size() == 0) return LogicStatus.CONTRADICTION;
				if (validDeltas.size() > 1) continue;
				
				doFlight(b,s,x,y,validDeltas.firstElement());
				result = LogicStatus.LOGICED;
			}
		}
		return result;
	}
				
	private Collection<State> Guess(Board b, State s)
	{
		Vector<State> successors = new Vector<State>();
		
		for (int x = 0 ; x < b.width ; ++x)
		{
			for (int y = 0 ; y < b.height ; ++y)
			{
				if (!s.isBall(x,y)) continue;
				Vector<Point> validDeltas = new Vector<Point>();
				for (Point dir : deltas)
				{
					if (!ballCanTravel(b,s,x,y,dir)) continue;
					State news = new State(s);
					doFlight(b,news,x,y,dir);
					successors.add(news);
				}				
				return successors;
			}
		}
		return null;
	}
				
				
				
				
	public Logic(Board theBoard,State initialState)
	{
		Vector<State> queue = new Vector<State>();
		queue.add(initialState);
		
		while(queue.size() > 0)
		{
			System.out.println("Queue size: " + queue.size() + " solution size: " + solutions.size());
			State curState = queue.remove(0);
		
			if (curState.isDone())
			{
				solutions.add(curState);
				continue;
			}

			LogicStatus status = ApplyLogic(theBoard,curState);
			
			switch(status)
			{
			case LOGICED:
				queue.add(curState);
				break;
			case CONTRADICTION:
				break;
			case STYMIED:
				queue.addAll(Guess(theBoard,curState));
			}
		}
	}
}