

import java.util.*;
import java.awt.Point;


// various bits of logic that may aid in solving this damn puzzle...
public class Logic
{
	public static class WalkingIdentityReference implements GridGraph.GridReference
	{
		State theState;
		State.ActorInfo theActor;
		public WalkingIdentityReference(State s, State.ActorInfo ai) { theState = s ; theActor = ai; }
		
		public int getWidth() { return theState.getWidth(); }
		public int getHeight() { return theState.getHeight(); }
		
		public boolean isIncludedCell(int x, int y) 
		{
			if (x == theActor.curx && y == theActor.cury) return true;
			return !theState.blocksActor(theActor,x,y);
		}
		public boolean edgeExitsEast(int x, int y) { return true; }
		public boolean edgeExitsSouth(int x,int y) { return true; }
	}
	
	private static Map<State.ActorInfo,GridGraph> actorGrids = new HashMap<State.ActorInfo,GridGraph>();
	private static GridGraph GetActorWalkingGraph(State s, State.ActorInfo ai)
	{
		if (!actorGrids.containsKey(ai))
		{
			actorGrids.put(ai,new GridGraph(new WalkingIdentityReference(s,ai)));
		}
		return actorGrids.get(ai);
	}
		
	private static boolean actorIsInRegion(Board.Region r, State s, State.ActorInfo actor)
	{
		Point ap = new Point(actor.curx,actor.cury);
		for (Point p : r.cells) 
		{
			if (ap.equals(p)) return true;
		}
		return false;
	}	
	
	private static Set<Point> actorAccessibleCellsInRegion(Board.Region r, State s, State.ActorInfo actor)
	{
		GridGraph ag = GetActorWalkingGraph(s,actor);
		Set<Point> result = new HashSet<Point>();
		Point ap = new Point(actor.curx,actor.cury);
		for (Point p : r.cells) 
		{
			Set<Point> conset = ag.connectedSetOf(ap);
			if (conset.contains(p)) result.add(p);
		}
		return result;
	}	
		
	// returns true if and only if there exists a unique point, for each actor, chosen from that actor's set.	
	// if we get here, we know that there is at least one 
	private static boolean CanAllBeUnique(Map<State.ActorInfo,Set<Point>> actorAccessables)
	{
		Uniquer u = new Uniquer(actorAccessables);
		return u.hasUniqueSolution();
	}
				
	private static boolean housePossibleForRegion(Board.Region r, State s, char potentialHouseName)
	{
		Map<State.ActorInfo,Set<Point>> accessableByActor = new HashMap<State.ActorInfo,Set<Point>>();
		
		for (State.ActorInfo houseActor : s.actorsByHouse.get(potentialHouseName))
		{
			if (actorIsInRegion(r,s,houseActor)) continue;
			Set<Point> actorPoints = actorAccessibleCellsInRegion(r,s,houseActor);
			if (actorPoints.size() == 0)
			{
				System.out.println("  Actor " + houseActor.house + " at " + houseActor.curx + "," + houseActor.cury + " Can't get here!");
				return false;
			}
			accessableByActor.put(houseActor,actorPoints);
		}
		if (!CanAllBeUnique(accessableByActor)) 
		{
			System.out.println("  Region doesn't have enough empty space for all moving actors with house " + potentialHouseName);
			return false;
		}
		return true;
	}
	
	
	
	
	// a house can only have a particular identity if all of the actors with that identity
	// can get to a unique empty space of that house.
	// that may be O(2^n) ... how about if all actors with that identity can get to _an_
	// empty space of the house?
	
	public static LogicStatus HouseIdentitiesByWalking(State theState)
	{
		LogicStatus result = LogicStatus.STYMIED;
		actorGrids.clear();
				
		for (Map.Entry<Board.Region,Set<Character>> ent : theState.regionPossibles.entrySet())
		{		
			Board.Region region = ent.getKey();
			Set<Character> possibles = ent.getValue();
			Set<Character> impossibles = new HashSet<Character>();
			System.out.println("For Region: " + region);

			for (char potentialHouseName : possibles)
			{
				if (!housePossibleForRegion(region,theState,potentialHouseName))
				{
					impossibles.add(potentialHouseName);
				}
			}
			if (impossibles.size() == possibles.size()) return LogicStatus.CONTRADICTION;
			if (impossibles.size() == 0) continue;
			result = LogicStatus.LOGICED;
			for (char c : impossibles) { possibles.remove(c); }			
		}		
		return result;
	}
	
	// if a particular house has only one possible region, then that region
	// should have no other houses.
	public static LogicStatus FindUniqueHouses(State theState)
	{
		LogicStatus result = LogicStatus.STYMIED;
		System.out.println("FindUniqueHouses");
		for (char house : theState.actorsByHouse.keySet())
		{
			System.out.println("For House " + house);
			Board.Region uniqueRegion = null;
			boolean isUnique = true;
			
			int rcount = 0;
			for (Board.Region region : theState.regionPossibles.keySet())
			{
				System.out.print("  Region " + region);
				if (!theState.regionPossibles.get(region).contains(house)) 
				{
					System.out.println(" is not a possible region for house");
					continue;
				}
				++rcount;
				if (uniqueRegion == null)
				{
					System.out.println(" might be unique region");
					uniqueRegion = region;
				}
				else
				{
					System.out.println(" no unique region");
					isUnique = false;
					uniqueRegion = null;
					break;
				}
			}
			if (rcount == 0) return LogicStatus.CONTRADICTION;
			
			if (uniqueRegion != null)
			{
//				System.out.println("Unique Region " + uniqueRegion + " found for house " + house);
				for (Iterator<Character> it = theState.regionPossibles.get(uniqueRegion).iterator() ; it.hasNext() ; )
				{
					char c = it.next();
					if (c == house) continue;
					it.remove();
					result = LogicStatus.LOGICED;
				}
			}
		}
		return result;
	}
	
	public static LogicStatus FindUniqueRegions(State theState)
	{
		LogicStatus result = LogicStatus.STYMIED;
		
		for (Board.Region region : theState.regionPossibles.keySet())
		{
			if (theState.regionPossibles.get(region).size() == 0) return LogicStatus.CONTRADICTION;
			if (theState.regionPossibles.get(region).size() > 1) continue;
			// if we're here, then this region has only one possible.
			
			char thePossible = theState.regionPossibles.get(region).iterator().next();
			for (Board.Region delregion : theState.regionPossibles.keySet())
			{
				if (delregion == region) continue;
				if (!theState.regionPossibles.get(delregion).contains(thePossible)) continue;
				result = LogicStatus.LOGICED;
				theState.regionPossibles.get(delregion).remove(thePossible);
			}
		}
		return result;
	}
			
			
	
	
}
	