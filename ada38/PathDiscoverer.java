
import java.awt.Point;
import java.util.*;


// this class will discover and store all valid paths by all Travelers for the given Board/State
// a path is valid if:
// 1) it is a straight orthogonal line
// 2) it is exactly as long as the traveler's movedist (if movedist is 0, path length may be any number, including 0)
// 3) it does not land on or cross another traveler
// 4) it does not land on or cross a shadowed space
// 5) it is a space on the legal board.
// 6) it terminates in a region that has 0 moved travelers in it
// 7) the traveler has not yet been moved

public class PathDiscoverer
{
	public class Path
	{
		State.Traveler traveler;
		Point destination;
		Vector<Point> shadows = new Vector<Point> (); // this will contain all points other than destination (including source) passed through on the way
		
		public Path(State.Traveler t,Point p) { traveler = t ; destination = p; }
		
	}



	Board tb;
	State ts;

	Set<Character> filledRegions = new HashSet<Character>();
	Map<Character,Vector<Path> > regionDestinations = new HashMap<Character,Vector<Path> >();
	Map<State.Traveler,Vector<Path> > paths = new HashMap<State.Traveler,Vector<Path> >();
	int movedtravelercount = 0;

	
	private void AddPath(State.Traveler t,int x,int y,Vector<Point> shadows)
	{
		if (!paths.containsKey(t))
		{
			paths.put(t,new Vector<Path>());
		}
		
		Path p = new Path(t,new Point(x,y));
		if (shadows != null)
		{
			for (Point sp : shadows) { p.shadows.add(sp); }
		}
		
		paths.get(t).add(p);
		
		// also, add the path to the region destinations.
		char regid = tb.regions[x][y].regionid;
		if (!regionDestinations.containsKey(regid))
		{
			regionDestinations.put(regid,new Vector<Path>());
		}
		regionDestinations.get(regid).add(p);
	}
	
	private void FindFilledRegions()
	{
		for (Board.Region reg : tb.rmap.values())
		{
			for (Point p : reg.cells)
			{
				State.Traveler t = ts.getTraveler(p.x,p.y);
				if (t == null) continue;
				if (t.moved) 
				{
					filledRegions.add(reg.regionid);
					break;
				}
			}
		}
	}
	
	static Point[] deltas = new Point[] { new Point(1,0),new Point(-1,0),new Point(0,1),new Point(0,-1) };
	private void FindPaths()
	{
		for (State.Traveler trv : ts.travelers)
		{
			if (trv.moved) { ++movedtravelercount; continue; } // this checks case 7 for all.
			// special case.... if movedist = 0, cases 1,2,3,4,5 are all trivially valid, and only case 6 has to be checked.
			if (trv.movedist == 0 && !filledRegions.contains(tb.regions[trv.curx][trv.cury].regionid))
			{
				AddPath(trv,trv.curx,trv.cury,null);
			}
			
			// cases unchecked 23456 checked 71
			// nature of below mechanism only checks spaces in straight lines from traveler, so 1 is checked
			for (Point delta : deltas)
			{
				// every direction gets a new shadow array.
				Vector<Point> shadows = new Vector<Point>();
				for (int dist = 1 ; ; ++dist) // this will terminate validly due to above cases (at the very least case 5)
				{
					// we do this here so that we always do it (notice all the breaks and continues below)
					shadows.add(new Point(trv.curx + delta.x * (dist-1) , trv.cury + delta.y * (dist - 1)));
					Point np = new Point(trv.curx + delta.x * dist , trv.cury + delta.y * dist);
					if (!tb.isOnBoard(np.x,np.y)) break; // case 5
					if (ts.isShadow(np.x,np.y)) break; // case 4 ... and the break here will prevent trying cases past a shadow
					if (ts.travelerboard[np.x][np.y] != null) break; // case 3 .. and the break here will prevent trying cases past a traveler
					if (trv.movedist != 0 && dist < trv.movedist) continue; // case 2 part 1
					if (trv.movedist != 0 && dist > trv.movedist) break; // case 2 part 2
					if (filledRegions.contains(tb.regions[np.x][np.y].regionid)) continue; // case 6
					// cases unchecked: checked: 1234567
					AddPath(trv,np.x,np.y,shadows);
				}
			}
		}
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Destinations for each movable traveler:\n");
		for (State.Traveler trav : paths.keySet() )
		{
			sb.append( "(" + trav.curx + "," + trav.cury + ")" + ":");
			for (Path p : paths.get(trav) )
			{
				sb.append(" " + "(" + p.destination.x + "," + p.destination.y + ")");
			}
			sb.append("\n");
		}
		sb.append("Moveable Travelers for each non-filled region:\n");
		for (char regid : regionDestinations.keySet())
		{
			sb.append( "" + regid + ":");
			for (Path p : regionDestinations.get(regid))
			{
				sb.append(" " + "(" + p.traveler.curx + "," + p.traveler.cury + ")");
			}
			sb.append("\n");
		}
		
		
		
		return sb.toString();
	}
	
	
	
	public PathDiscoverer(State s)
	{
		tb = s.getBoard();
		ts = s;
		
		FindFilledRegions(); // all regions with a moved traveler in it
		FindPaths(); // all possible legal movements of travelers (no element in this list will be in a filled region)
		
		// so, things we know here:
		// |filledRegions| + |regionDestinations| should equal # of regions (or logic fail has occured)
		// |movedtravelercount| + |paths| should equal # of travelers (or logic fail has occured)
		// (existence of traveler in paths indicates at least one possible path for that traveler)
		
	}



}