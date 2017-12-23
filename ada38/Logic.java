

import java.util.*;
import java.awt.Point;


// various bits of logic that may aid in solving this damn puzzle...
public class Logic
{
	public static void ApplyPath(State s,PathDiscoverer.Path p)
	{
		// one step more sophisticated:  the path from a pathdiscoverer refers to a Traveler from a particular state.
		// once we start cloning states for guesses, we may have to apply a Path to a different state...let's assume that.
		// so, instead of simply saying State.Traveler t = p.traveler;  we say:
		State.Traveler t = s.travelerboard[p.traveler.curx][p.traveler.cury];
	
		s.travelerboard[t.curx][t.cury] = null;
		s.travelerboard[p.destination.x][p.destination.y] = t;
		t.curx = p.destination.x;
		t.cury = p.destination.y;
		t.moved = true;
		for (Point pt : p.shadows) { s.setShadow(pt.x,pt.y); }
		// repurposing traveler's movedist:  once it moved, how far _did_ it move?
		t.movedist = p.shadows.size();
	}

	public static boolean isDone(State s, PathDiscoverer pd)
	{
		if (pd.paths.size() > 0) return false;
		if (pd.regionDestinations.size() > 0) return false;
		if (pd.filledRegions.size() < s.getBoard().rmap.size()) return false;
		if (pd.movedtravelercount < s.travelers.size()) return false;
		return true;
	}
	
	public static LogicStatus LogicStep(State s, PathDiscoverer pd)
	{
		int fr = pd.filledRegions.size();
		int rd = pd.regionDestinations.size();
		int nr = s.getBoard().rmap.size();
		
		int mt = pd.movedtravelercount;
		int umt = pd.paths.size();
		int nt = s.travelers.size();
		
		if (nr != (fr+rd)) return LogicStatus.CONTRADICTION;
		if (nt != (mt+umt)) return LogicStatus.CONTRADICTION;
		
		// If the above invariants hold, then all unmoved travelers have a place to go,
		// and there is a traveler, potentially, for every region.
		
		// let's see if there is a traveler with only one destination
		for (Vector<PathDiscoverer.Path> travpaths : pd.paths.values() )
		{
			if (travpaths.size() > 1) continue;
			PathDiscoverer.Path thep = travpaths.firstElement();
			ApplyPath(s,thep);
			return LogicStatus.LOGICED;
		}
		
		// no?  Let's see if there is a region with only one traveler's path
		for (Vector<PathDiscoverer.Path> travpaths : pd.regionDestinations.values())
		{
			if (travpaths.size() > 1) continue;
			PathDiscoverer.Path thep = travpaths.firstElement();
			ApplyPath(s,thep);
			return LogicStatus.LOGICED;
		}
		
		return LogicStatus.STYMIED;
	}
	
	// finds either a region or a traveler with minimal paths (if we get here, then # of paths must be > 1)
	// also, if we get here, we know that the State is non contradictory and not done
	public static Vector<PathDiscoverer.Path> GetGuess(State s,PathDiscoverer pd)
	{
		Vector<PathDiscoverer.Path> result = null;
		int minpathcount = 1000000; // some ridiculous large number of paths for a single traveler or region
		
		for (Vector<PathDiscoverer.Path> travpaths : pd.paths.values() )
		{
			if (travpaths.size() < minpathcount)
			{
				result = travpaths;
				minpathcount = travpaths.size();
			}
		}
		
		for (Vector<PathDiscoverer.Path> travpaths : pd.regionDestinations.values() )
		{
			if (travpaths.size() < minpathcount)
			{
				result = travpaths;
				minpathcount = travpaths.size();
			}
		}
		
		return result;
	}
		
		
	
	
	
	
		

	public static Vector<State> Logic(State iState)
	{
		Vector<State> queue = new Vector<State>();
		Vector<State> results = new Vector<State>();
		
		queue.add(iState);
		
		while(queue.size() > 0)
		{
			System.out.println("Queue Size: " + queue.size());
			State curs = queue.remove(0);
			PathDiscoverer pd = new PathDiscoverer(curs);
			
			if (isDone(curs,pd))
			{
				results.add(curs);
				continue;
			}
			
			LogicStatus stat = LogicStep(curs,pd);
			
			switch(stat)
			{
			case CONTRADICTION:
				System.out.println("Contradiction Found");
				break;
			case LOGICED:
				System.out.println("Some Logic Found");
				queue.add(curs);
				break;
			case STYMIED:
				System.out.println("Making Guess.");
				Vector<PathDiscoverer.Path> guesses = GetGuess(curs,pd);
				
				for (PathDiscoverer.Path p : guesses)
				{
					State newg = new State(curs);
					ApplyPath(newg,p);
					queue.add(newg);
				}
				
				break;
			}	
		}
		return results;
	}
}
	