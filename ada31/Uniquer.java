
import java.util.*;
import java.awt.Point;

// returns true if and only if there exists a unique point, for each actor, chosen from that actor's set.	
// if we get here, we know that there is at least one 

public class Uniquer
{
	State.ActorInfo[] actors;
	Map<State.ActorInfo,Point[]> pars;
	int[] counters;
	int[] maxcounters;
	
	private boolean counterDone()
	{
		for (int i = 0 ; i < counters.length ; ++i)
		{
			if (counters[i] != maxcounters[i] - 1) return false;
		}
		return true;
	}
	
	public void increment()
	{
		for (int idx = 0 ; idx < counters.length ; ++idx)
		{
			++counters[idx];
			if (counters[idx] < maxcounters[idx]) return;
			counters[idx] = 0;
		}
	}
	
	public boolean isUnique()
	{
		for (int outer = 0 ; outer < counters.length ; ++outer)
		{
			for (int inner = outer + 1 ; inner < counters.length ; ++inner)
			{
				if (pars.get(actors[outer])[counters[outer]].equals(pars.get(actors[inner])[counters[inner]])) return false;
			}
		}
		return true;
	}
	
	public boolean hasUniqueSolution()
	{
		for (int i = 0 ; i < counters.length ; ++i) counters[i] = 0;
	
		while(true)
		{
			if (isUnique()) return true;
			if (counterDone()) break;
			increment();
		}
		return false;
	}
	
	public Uniquer(Map<State.ActorInfo,Set<Point>> actorAccessables)
	{
		actors = actorAccessables.keySet().toArray(new State.ActorInfo[0]);
		counters = new int[actors.length];
		maxcounters = new int[actors.length];
		pars = new HashMap<State.ActorInfo,Point[]>();
		for (State.ActorInfo actor : actors)
		{
			pars.put(actor,actorAccessables.get(actor).toArray(new Point[0]));
		}
		for (int i = 0 ; i < counters.length ; ++i)
		{ 
			maxcounters[i] = pars.get(actors[i]).length;
		}
	}
}