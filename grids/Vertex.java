package grids;

import java.awt.Point;
import java.util.Vector; 

public class Vertex
{

	public static interface Operator
	{
		public void Operate(Vertex v);
	}

	Point coord;
	Object contents;
	Vector<Edge> edges = new Vector<Edge>();
	Vector<Space> spaces = new Vector<Space>();
	
	public Vertex(Point loc, Object con)
	{
		coord = loc;
		contents = con;
	}	
	
	public Point GetCoord() { return coord; }
	public Object GetContents() { return contents; }
	
	public void ForEachEdge(Edge.Operator op) { for (Edge e : edges) { op.Operate(e); } }
	public void ForEachSpace(Space.Operator op) { for (Space s : spaces) { op.Operate(s); } }
	
	public void AddSpace(Space sp) { spaces.add(sp); }
	public void AddEdge(Edge e) { edges.add(e); }
}

