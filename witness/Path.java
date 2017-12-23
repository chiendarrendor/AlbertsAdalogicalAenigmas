import java.util.*;

public class Path
{
	public Vector<WitnessBoard.Vertex> vertices = new Vector<WitnessBoard.Vertex>();
	public Set<Coordinate> vCoords = new HashSet<Coordinate>();
	public Vector<WitnessBoard.Edge> edges = new Vector<WitnessBoard.Edge>();
	public Set<EdgeCoordinate> eCoords = new HashSet<EdgeCoordinate>();

	int endCount = 0;
	public int getEndCount() { return endCount; }
	public void incrementEndCount() { ++endCount; }
	
	int throughCount = 0;
	public int getThroughCount() { return throughCount; }
	public void incrementThroughCount() { ++throughCount; }
	
	public Path(WitnessBoard.Vertex iv)
	{
		vertices.add(iv);
		vCoords.add(iv.location);
	}
	
	public Path(Path right)
	{
		this(right.vertices.elementAt(0));
		for (int i = 0 ; i < right.edges.size() ; ++i)
		{
			add(right.edges.elementAt(i),right.vertices.elementAt(i+1));
		}
		this.endCount = right.getEndCount();
		this.throughCount = right.getThroughCount();
	}
	
	public WitnessBoard.Vertex tailVertex() { return vertices.lastElement(); }
	public WitnessBoard.Edge tailEdge() { return edges.size() > 0 ? edges.lastElement() : null; }
	
	
	public boolean hasVertex(WitnessBoard.Vertex v) { return vCoords.contains(v.location); }
	public boolean hasEdge(WitnessBoard.Edge e) { return eCoords.contains(e.location); }
	
	public void add(WitnessBoard.Edge e,WitnessBoard.Vertex v)
	{
		edges.add(e);
		eCoords.add(e.location);
		vertices.add(v);
		vCoords.add(v.location);
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(vertices.elementAt(0).location);
		for (int i = 0 ; i < edges.size() ; ++i)
		{
			sb.append(" -> ");
			sb.append(edges.elementAt(i).location);
			sb.append(" -> ");
			sb.append(vertices.elementAt(i+1).location);
		}
		return sb.toString();
	}
	
	
}