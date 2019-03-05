package grid.graph;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

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

	GridReference ref;
	public GridReference getGridReference() { return ref; }

	public GridGraph(GridReference ref)
	{
		super(DefaultEdge.class);
		this.ref = ref;
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

	public boolean hasCycle() {
		return !GraphTests.isForest(this);
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
		GraphPath<Point,DefaultEdge> gp = fwsp.getPath(p1,p2);
		if (gp == null) return null;
		return gp.getVertexList();
	}
	

	// This block of stuff here is all private variables for calculating articulation points....
	// maybe its own class in a separate file? 


    private Map<Point,ArticulationSetNode> nodesByPoint = new HashMap<>();
	private Set<Point> articulationPoints = null;
	private int time = 0;
    ArticulationSetNode masternode = null;

	private boolean visited(Point p) { return nodesByPoint.containsKey(p); }
	private ArticulationSetNode getVisited(Point p) { return nodesByPoint.get(p); }

    private class ArticulationSetNode {

	    boolean iAmArticulationPoint = false;
	    Point me;
	    Point parent;
	    Set<Point> childrenPoints = new HashSet<>();

	    List<ArticulationSetNode> articulateChildren = new ArrayList<>();
	    List<ArticulationSetNode> inarticulateChildren = new ArrayList<>();

	    int discovered;
	    int low;

	    public ArticulationSetNode(Point me,Point parent) {
	        int now = ++time;
	        discovered = now;
	        low = now;
	        nodesByPoint.put(me,this);
	        this.me = me;
	        this.parent = parent;
	        childrenPoints.add(me);
        }

        public boolean isParent(Point p) { return p.equals(parent); }
        public boolean hasParent() { return parent != null; }
        public int low() { return this.low; }
        public int discovered() { return this.discovered; }
        public void decrementLow(int newval) { if (newval < low) low = newval; }

        private void addArticulateChild(ArticulationSetNode child) {
	        articulateChildren.add(child);
	        childrenPoints.addAll(child.childrenPoints);
        }

        private void addInarticulateChild(ArticulationSetNode child) {
	        inarticulateChildren.add(child);
	        childrenPoints.addAll(child.childrenPoints);
        }




        public void addChildNode(ArticulationSetNode child) {
	        if (!hasParent()) {
	            addArticulateChild(child);
	            if (articulateChildren.size() > 1) iAmArticulationPoint = true;
	            return;
            }

            if (child.low() >= discovered()) {
	            addArticulateChild(child);
	            iAmArticulationPoint = true;
            } else {
	            addInarticulateChild(child);
            }
	    }

	    private Map<ArticulationSetNode,Set<Point>> parentSets = new HashMap<>();
	    public Set<Point> getParentSet(ArticulationSetNode child) {
	        if (!parentSets.containsKey(child)) {
	            Set<Point> result = new HashSet<>();
	            result.add(me);

	            for (ArticulationSetNode arnode : articulateChildren) {
	                if (arnode == child) continue;
	                result.addAll(arnode.childrenPoints);
                }

                for (ArticulationSetNode arnode : inarticulateChildren) {
                    if (arnode == child) continue;
                    result.addAll(arnode.childrenPoints);
                }

                if (parent != null) result.addAll(getVisited(parent).getParentSet(this));
	            parentSets.put(child,result);
            }
            return parentSets.get(child);
        }

        Set<Point> myparentset = null;

        public Set<Point> getMyParentSet() {
            if (myparentset == null) {
                myparentset = new HashSet<>();

                for (ArticulationSetNode child : inarticulateChildren) {
                    myparentset.addAll(child.childrenPoints);
                }
                if (parent != null) myparentset.addAll(getVisited(parent).getParentSet(this));
            }
	        return myparentset;
        }








	    List<Set<Point>> arsets = null;
	    public List<Set<Point>> getArticulationSets() {
	        if (!iAmArticulationPoint) throw new RuntimeException("Can't get Articulation Sets of a non-articulation point");

	        if (arsets == null) {
	            arsets = new ArrayList<>();
	            if (parent != null) arsets.add(getMyParentSet());
	            for (ArticulationSetNode arch : articulateChildren) {
	                arsets.add(arch.childrenPoints);
                }
            }
            return arsets;
        }
    }




	private ArticulationSetNode aputil(Point me,Point parent)
	{
	    ArticulationSetNode result = new ArticulationSetNode(me,parent);

		Set<DefaultEdge> edges = edgesOf(me);
		for (DefaultEdge de : edges)
		{
			Point child = getEdgeTarget(de);
			if (child.equals(me)) child = getEdgeSource(de);

			if (result.isParent(child)) continue;
		    if (visited(child)) {
		        result.decrementLow(getVisited(child).discovered());
            } else {
		        ArticulationSetNode childnode = aputil(child,me);
		        result.decrementLow(childnode.low());
		        result.addChildNode(childnode);
            }
		}

		return result;
	}

	// returns the set of points that, if removed, would break the graph into two or more disconnected sets.
	public Set<Point> getArticulationPoints() 
	{ 
		if (!isConnected()) throw new RuntimeException("Invalid to request Articulation Points of a Disconnected Set");
		
		if (masternode == null)
		{
            masternode = aputil(vertexSet().iterator().next(),null);
			articulationPoints = nodesByPoint.keySet().stream().filter(k->nodesByPoint.get(k).iAmArticulationPoint).collect(Collectors.toSet());
		}
	
		return articulationPoints;
	}

	public List<Set<Point>> getArticulationSet(Point p) {
		return nodesByPoint.get(p).getArticulationSets();
	}
	

	
	
	
}