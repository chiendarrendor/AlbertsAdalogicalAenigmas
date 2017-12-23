package grids;

import java.util.Vector;

public class Edge
{
	public static interface Operator
	{
		public void Operate(Edge e);
	}

	Vertex v1;
	Vertex v2;
	Object contents;
	Vector<Space> spaces = new Vector<Space>();
	
	public Edge(Vertex v1, Vertex v2, Object con)
	{
		this.v1 = v1;
		this.v2 = v2;
		contents = con;
	}	
	
	public Vertex GetV1() { return v1; }
	public Vertex GetV2() { return v2; }
	public Object GetContents() { return contents; }
	
	public void ForEachVertex(Vertex.Operator op) { op.Operate(v1) ; op.Operate(v2); }
	public void ForEachSpace(Space.Operator op) { for (Space s : spaces) { op.Operate(s); } }
	
	public void AddSpace(Space sp) { spaces.add(sp); }
}

