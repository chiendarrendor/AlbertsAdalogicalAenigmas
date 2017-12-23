
import java.util.*;

public class State implements Solvable<State>
{
	Board myBoard;

	CellState[][] state;
	Set<Character> fired = new HashSet<Character>();
	Set<Character> unfired = new HashSet<Character>();
	
        public boolean isSolution()
        {
            return unfired.size() == 0;
        }
        
        
	public void Fire(Board.Path path)
	{
		unfired.remove(path.id);
		fired.add(path.id);
	}
        
        public boolean fired(Board.Path path)
        {
            return fired.contains(path.id);
        }
	
	public State(Board b)
	{
		myBoard = b;
		state = new CellState[myBoard.width][myBoard.height];
		
		for (int x = 0 ; x < myBoard.width ; ++x)
		{
			for (int y = 0 ; y < myBoard.height ; ++y)
			{
				state[x][y] = CellState.UNKNOWN;
			}
		}
		
		for (Board.Path path : myBoard.pathsById.values())
		{
			unfired.add(path.id);
		}
		
	}
	
	public State(State right)
	{
		myBoard = right.myBoard;
		state = new CellState[myBoard.width][myBoard.height];
		
		for (int x = 0 ; x < myBoard.width ; ++x)
		{
			for (int y = 0 ; y < myBoard.height ; ++y)
			{
				state[x][y] = right.state[x][y];
			}
		}
		for (char c : right.fired) { fired.add(c); }
		for (char c : right.unfired) { unfired.add(c); }
	}
        
        
        public List<State> Successors()
        {
            ArrayList<State> result = new ArrayList<>();
            int minnummirrors = myBoard.regionsById.size() + 1;
            Board.Path minPath = null;
            
            for (Board.Path path : myBoard.paths)
            {
                if (fired(path)) continue;
                if (path.numMirrors < minnummirrors)
                {
                    minPath = path;
                    minnummirrors = path.numMirrors;
                }
            }
                

            FireField ff = new FireField(this,minPath,minPath.p1);
            for (FirePath fp : ff.paths)
            {
                State ns = new State(this);
                fp.ApplyPath(ns);
                result.add(ns);
                ns.Fire(minPath);
            }

            return result;
        }
        
        
        
}

