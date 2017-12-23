
import org.jgrapht.graph.*;
import java.util.*;
import java.awt.Point;

public class ArticulationPoints
{
	Set<Point> visited = new HashSet<Point>();
	Map<Point,Integer> discovered = new HashMap<Point,Integer>();
	Map<Point,Integer> low = new HashMap<Point,Integer>();
	Map<Point,Point> parent = new HashMap<Point,Point>();
	Set<Point> isArticulation = new HashSet<Point>();
	AreaGraph mygraph;
	int time=0;

	private void aputil(Point p)
	{
		int children = 0;
		visited.add(p);
		int now = ++time;
		discovered.put(p,now);
		low.put(p,now);
		Set<DefaultEdge> edges = mygraph.edgesOf(p);
		for (DefaultEdge de : edges)
		{
			Point v = mygraph.getEdgeTarget(de);
			if (v.equals(p)) v = mygraph.getEdgeSource(de);
			
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
		
	public ArticulationPoints(AreaGraph ag)
	{
		mygraph = ag;
		
		for (Point p : ag.vertexSet() )
		{
			if (!visited.contains(p)) aputil(p);
		}
	}

	public Iterable<Point> getArticulationPoints() { return isArticulation; }
	

}
