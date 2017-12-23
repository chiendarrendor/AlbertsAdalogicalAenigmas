
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;
import java.awt.Point;
import java.util.*;


// various functions for the support of the Galaxy Solver
public class AISupport
{
	// This function, given an Expansion board (from GalaxyBoardAreaOverlays)
	// will determine if all of the spaces for an area are connected
	public static boolean isAreaConnected(GalaxyBoard exp,int areaid)
	{
		AreaGraph ag = new AreaGraph(exp,areaid);
		ConnectivityInspector<Point,DefaultEdge> ci = new ConnectivityInspector<Point,DefaultEdge>(ag);
		return ci.isGraphConnected();
	}

	private static boolean separatesSources(GalaxyBoardAreaOverlays gbao,GalaxyBoard exp, int areaid, Point p)
	{
		AreaGraph ag = new AreaGraph(exp,areaid,p);
		ConnectivityInspector<Point,DefaultEdge> ci = new ConnectivityInspector<Point,DefaultEdge>(ag);
		List<Set<Point>> components = ci.connectedSets();
		// at least two of the sets have to have at least one item that is marked isSource
		int ccount = 0;
		for (Set<Point> set : components)
		{
			boolean found = false;
			for (Point sp : set)
			{
				GalaxyBoardAreaOverlays.OverlayState os = gbao.cellstates[sp.x][sp.y];
				if (os.isSource)
				{
					found = true;
					break;
				}
			}
			if (found)
			{
				++ccount;
				if (ccount > 1) return true;
			}
		}
		return false;
	}
	
	
	
	// This function, given a GalaxyBoardAreaOverlays and an area id,
	// where we can assume that isAreaConnected() is true for that area
	// will return, for that area, all spaces such that:
	//   1) the space is !isCore, !isSource
	//   2) the space has the specified areaId
	//   3) the space is an articulation point of area
	//   4) more than one disconnected component of area-space contains an isSource space for the area
	public static Vector<Point> getConnectors(GalaxyBoardAreaOverlays gbao,int areaid)
	{
		AreaGraph ag = new AreaGraph(gbao.overlays.get(areaid),areaid);
		ArticulationPoints ap = new ArticulationPoints(ag);
		Vector<Point> result = new Vector<Point>();
		for (Point p : ap.getArticulationPoints())
		{
			if (!separatesSources(gbao,gbao.overlays.get(areaid),areaid,p)) continue;
			if (gbao.cellstates[p.x][p.y].isSource) continue;
			result.add(p);
			
		}
		return result;
	}
		
}