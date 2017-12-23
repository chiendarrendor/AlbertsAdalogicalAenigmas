package grid.graph;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;
import java.awt.Point;
import java.util.*;

// this class will take a GridReference interface and build an 
// undirected graph out of the implied grid, assuming that:
// vertices are equivalent to cells, x,y
// all cells/vertices have indices 0 <= x < width, and 0 <= y < height
// the only possible adjacent cells are x+1,y  x-1,y  x,y+1  x,y-1
// the caller is guaranteed that edgeExistsEast will only be called when x < width-1
// and edgeExitsSouth will only be called when y < height-1
// 

public class GridGraph extends SimpleGraph<Point,DefaultEdge>
{
	int width;
	int height;
	ConnectivityInspector<Point,DefaultEdge> ci = null;
	FloydWarshallShortestPaths<Point,DefaultEdge> fwsp = null;
	
	public interface GridReference
	{
		int getWidth();
		int getHeight();
		boolean isIncludedCell(int x, int y);
		boolean edgeExitsEast(int x, int y);
		boolean edgeExitsSouth(int x,int y);
	}
		
	public GridGraph(GridReference ref)
	{
		super(DefaultEdge.class);
		width = ref.getWidth();
		height = ref.getHeight();
		
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				if (!ref.isIncludedCell(x,y)) continue;
				addVertex(new Point(x,y));
			}
		}
		
		for (Point p : vertexSet())
		{
			Point eP = new Point(p.x+1,p.y);
			Point sP = new Point(p.x,p.y+1);
			if (containsVertex(eP) && ref.edgeExitsEast(p.x,p.y)) addEdge(p,eP);
			if (containsVertex(sP) && ref.edgeExitsSouth(p.x,p.y)) addEdge(p,sP);
		}
	}

	private void makeci()
	{
		if (ci != null) return;
		ci = new ConnectivityInspector<Point,DefaultEdge>(this);
	}

	private void makefwsp()
	{
		if (fwsp != null) return;
		fwsp = new FloydWarshallShortestPaths<Point,DefaultEdge>(this);
	}




	public boolean isConnected()
	{
		makeci();
		return ci.isGraphConnected();
	}
	
	public List<Set<Point>> connectedSets()
	{
		makeci();
		return  ci.connectedSets();
	}
	
	Map<Point,Set<Point>> pointSets = null; 
	
	public Set<Point> connectedSetOf(Point p)
	{
		if (pointSets == null)
		{
			pointSets = new HashMap<Point,Set<Point>>();
			List<Set<Point>> cs = connectedSets();
			for (Set<Point> sp : cs)
			{
				for (Point pi : sp)
				{
					pointSets.put(pi,sp);
				}
			}
		}
		return pointSets.get(p);
	}
	
	
	
	public List<Point> shortestPathBetween(Point p1, Point p2)
	{
		makefwsp();
		return fwsp.getShortestPathAsVertexList(p1,p2);
	}
	
	
	
	// This block of stuff here is all private variables for calculating articulation points....
	// maybe its own class in a separate file? 
	
	Set<Point> visited = new HashSet<Point>();
	Map<Point,Integer> discovered = new HashMap<Point,Integer>();
	Map<Point,Integer> low = new HashMap<Point,Integer>();
	Map<Point,Point> parent = new HashMap<Point,Point>();
	Set<Point> isArticulation = null;
	int time=0;

	private void aputil(Point p)
	{
		int children = 0;
		visited.add(p);
		int now = ++time;
		discovered.put(p,now);
		low.put(p,now);
		Set<DefaultEdge> edges = edgesOf(p);
		for (DefaultEdge de : edges)
		{
			Point v = getEdgeTarget(de);
			if (v.equals(p)) v = getEdgeSource(de);
			
			if (!visited.contains(v))
			{
				children++;
				parent.put(v,p);
				aputil(v);
				low.put(p,Math.min(low.get(p),low.get(v)));
				
				if (!parent.containsKey(p) && children > 1) isArticulation.add(p);
				if (parent.containsKey(p) && low.get(v) >= discovered.get(p)) isArticulation.add(p);
			}
			else if (!v.equals(parent.get(p)))
			{
				low.put(p,Math.min(low.get(p),discovered.get(v)));
			}
		}
	}
		
	private void CalculateArticulationPoints()
	{
		isArticulation = new HashSet<Point>();
	
		for (Point p : vertexSet() )
		{
			if (!visited.contains(p)) aputil(p);
		}
	}

	// returns the set of points that, if removed, would break the graph into two or more disconnected sets.
	public Set<Point> getArticulationPoints() 
	{ 
		if (!isConnected()) throw new RuntimeException("Invalid to request Articulation Points of a Disconnected Set");
		
		if (isArticulation == null)
		{
			CalculateArticulationPoints();
		}
	
		return isArticulation;
	}
	

	
	
	
}