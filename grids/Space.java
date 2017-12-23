package grids;

import java.awt.Rectangle;
import java.util.Vector;

public class Space
{
	public static interface Operator
	{
		public void Operate(Space s);
	}

	Rectangle location;
	Object contents;
	Vector<Vertex> vertices = new Vector<Vertex>();
	Vector<Edge> edges = new Vector<Edge>();
	
	public Space(Rectangle loc, Object con)
	{
		location = loc;
		contents = con;
	}
	
	public Object GetContents() { return contents; }
	public Rectangle GetLocation() { return location; }
	
	public void AddVertex(Vertex v) { vertices.add(v); }
	public void AddEdge(Edge e) { edges.add(e); }
	
	public void ForEachVertex(Vertex.Operator op) { for (Vertex v : vertices) { op.Operate(v); } }
	public void ForEachEdge(Edge.Operator op) { for(Edge e: edges) { op.Operate(e); } }
}
		