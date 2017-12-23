
import java.util.*;


// this class embodies a standard rectilinear board as per the various common puzzles in the
// computer game witness.
// structurally, the board is made up of a grid of square spaces, with paths between and around them.
// the spaces, the paths, and the vertices all may have potentially multiple different Sigils attached to them, including
// 'start vertex' sigils and 'end vertex' sigils 

public class WitnessBoard
{
	// args are the number of square spaces horizontally and vertically
	// both starting to count from 0, and going right/downward from there
	//
	// this implies (width+1) * (height+1) vertices
	// upper left vertex is 0,0, increasing down and to the right/downward
	// this implies, for a given space x,y:
	//   UL vertex coords = (x,y)
	//   UR vertex coords = (x+1,y)
	//   LL vertex coords = (x,y+1)
	//   LR vertex coords = (x+1,y+1)
	// this also implies (width)*(height+1) horizontal edges such that:
	//    going East from vertex (x,y) crosses horizontal edge (x,y)
	//    going West from vertex (x,y) crosses horizontal edge (x-1,y)
	// this also implies (width+1)*(height) vertical edges such that:
	//   going North from vertex (x,y) crosses vertical edge (x,y-1)
	//   going South from vertex (x,y) crosses vertical edge (x,y)
	
	public interface Sigil
	{
	}
	
	public interface SpaceSigil extends Sigil
	{
		public boolean ValidateBoard(WitnessBoard wb,Space s,Path p);
	}
	
	public interface VertexSigil extends Sigil
	{
	}
	
	public interface EdgeSigil extends Sigil
	{
	}
	
	public class Space
	{
		public Vector<SpaceSigil> sigils = new Vector<SpaceSigil>();
		public Coordinate location;
		public Space(int x,int y) { location = new Coordinate(x,y); }
		
		public Edge[] adjacentEdges()
		{
			Edge[] result = new Edge[4];
			result[0] = edges.get(new EdgeCoordinate(location.x,location.y,Direction.HORIZONTAL));
			result[1] = edges.get(new EdgeCoordinate(location.x,location.y+1,Direction.HORIZONTAL));
			result[2] = edges.get(new EdgeCoordinate(location.x,location.y,Direction.VERTICAL));
			result[3] = edges.get(new EdgeCoordinate(location.x+1,location.y,Direction.VERTICAL));
			return result;
		}
	}
	
	public class Vertex
	{
		public Vector<VertexSigil> sigils = new Vector<VertexSigil>();
		public Coordinate location;
		
		private Vector<EdgeCoordinate> edgedirs;
		
		public Vertex(int x,int y) 
		{ 
			location = new Coordinate(x,y);
			edgedirs = new Vector<EdgeCoordinate>();
			edgedirs.add(new EdgeCoordinate(location.x,location.y-1,Direction.VERTICAL));
			edgedirs.add(new EdgeCoordinate(location.x,location.y,Direction.VERTICAL));
			edgedirs.add(new EdgeCoordinate(location.x,location.y,Direction.HORIZONTAL));
			edgedirs.add(new EdgeCoordinate(location.x-1,location.y,Direction.HORIZONTAL));
		}
		
		public Vector<Edge> adjacentEdges()
		{
			Vector<Edge> result = new Vector<Edge>();
			for (EdgeCoordinate ec : edgedirs)
			{
				if (edges.containsKey(ec)) { result.add(edges.get(ec)); }
			}
			return result;
		}
		
		
	}
	
	public class Edge
	{
		public Vector<EdgeSigil> sigils = new Vector<EdgeSigil>();
		public EdgeCoordinate location;
		public Edge(int x,int y, Direction d) { location = new EdgeCoordinate(x,y,d); }
		
		public Vertex[] GetVertices()
		{
			Vertex[] result = new Vertex[2];
			result[0] = vertices.get(new Coordinate(location.x,location.y));
			result[1] = vertices.get(new Coordinate( location.direction == Direction.HORIZONTAL ? location.x + 1 : location.x , 
													 location.direction == Direction.VERTICAL ?  location.y + 1 : location.y ));
			return result;
		}
		
		public Vertex GetOtherVertex(Vertex v)
		{
			Vertex[] choices = GetVertices();
			if (v.location.equals(choices[0].location)) return choices[1];
			return choices[0];
		}
		
		public Vector<Space> GetSpaces()
		{
			Vector<Space> result = new Vector<Space>();
			Coordinate nc1 = new Coordinate(location.x,location.y);
			if (spaces.containsKey(nc1)) result.add(spaces.get(nc1));
			if (location.direction == Direction.HORIZONTAL)
			{
				Coordinate nc2 = new Coordinate(location.x,location.y-1);
				if (spaces.containsKey(nc2)) result.add(spaces.get(nc2));
			}
			else
			{
				Coordinate nc2 = new Coordinate(location.x-1,location.y);
				if (spaces.containsKey(nc2)) result.add(spaces.get(nc2));
			}
			return result;
		}
		
		public Space GetOtherSpace(Space s)
		{
			Vector<Space> adjspaces = GetSpaces();
			if (adjspaces.size() == 1) return null;
			if (s == adjspaces.elementAt(0)) return adjspaces.elementAt(1);
			return adjspaces.elementAt(0);
		}
	}
	
	int width;
	int height;
	Map<Coordinate,Space> spaces = new HashMap<Coordinate,Space>();
	Map<Coordinate,Vertex> vertices = new HashMap<Coordinate,Vertex>();
	Map<EdgeCoordinate,Edge> edges = new HashMap<EdgeCoordinate,Edge>();
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public Space getSpace(int x,int y) { return spaces.get(new Coordinate(x,y)); }
	public Space getSpace(Coordinate c) { return spaces.get(c); }
	public Vertex getVertex(int x,int y) { return vertices.get(new Coordinate(x,y)); }
	
	public Iterable<Space> getSpaces() { return spaces.values(); }
	public Iterable<Vertex> getVertices() { return vertices.values(); }
	public Iterable<Edge> getEdges() { return edges.values(); }
	
	
	public WitnessBoard(int width,int height)
	{
		this.width = width;
		this.height = height;
		
		// create spaces
		for (int x = 0 ; x < this.width ; ++x)
		{
			for (int y = 0 ; y < this.height ; ++y)
			{
				Space s = new Space(x,y);
				spaces.put(s.location,s);
			}
		}
		// create vertices
		for (int x = 0 ; x <= this.width ; ++x)
		{
			for (int y = 0 ; y <= this.height ; ++y)
			{
				Vertex v = new Vertex(x,y);
				vertices.put(v.location,v);
			}
		}
		// create horizontal edges
		for (int x = 0 ; x < this.width ; ++x)
		{
			for (int y = 0 ; y <= this.height ; ++y)
			{
				Edge e = new Edge(x,y,Direction.HORIZONTAL);
				edges.put(e.location,e);
			}
		}
		for (int x = 0 ; x <= this.width ; ++x)
		{
			for (int y = 0 ; y < this.height ; ++y)
			{
				Edge e = new Edge(x,y,Direction.VERTICAL);
				edges.put(e.location,e);
			}
		}
		
	}
		
}