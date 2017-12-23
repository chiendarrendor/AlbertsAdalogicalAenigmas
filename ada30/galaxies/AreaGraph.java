import org.jgrapht.*;
import org.jgrapht.graph.*;
import java.awt.Point;
import java.util.*;

public class AreaGraph extends SimpleGraph<Point,DefaultEdge>
{
	public AreaGraph()
	{
		super(DefaultEdge.class);
	}
	
	
	private void AddEdge(GalaxyBoard exp,GalaxyBoard.Area a,Point p,int dx,int dy)
	{	
		// so, p is in g already
		Point nextP = new Point(p.x+dx,p.y+dy); // so, nextP is _not_ in g (but might be equals() to something that is)
		if (!exp.isInBounds(nextP.x,nextP.y)) return; // off the board
		GalaxyBoard.Area.Pair nextPair = exp.getBoardCell(nextP.x,nextP.y);
		if (nextPair == null) return; // nobody home
		if (nextPair.getAreaId() != a.getId()) return; // not us
		
		Point adjPoint;
		
		if (nextPair.p1.equals(nextP)) adjPoint = nextPair.p1;
		else adjPoint = nextPair.p2;
		
		if (!containsVertex(adjPoint)) return;
		addEdge(p,adjPoint);
	}	
	
	public AreaGraph(GalaxyBoard exp, int areaid)
	{
		this(exp,areaid,null);
	}
	
	public AreaGraph(GalaxyBoard exp, int areaid, Point exempt)
	{
		this();
		GalaxyBoard.Area area = exp.getAreaById(areaid);
		for (GalaxyBoard.Area.Pair pair : area.pairs)
		{
			if (!pair.p1.equals(exempt)) addVertex(pair.p1);
			if (!pair.p2.equals(exempt)) addVertex(pair.p2);
		}
		for (Point p : vertexSet())
		{
			AddEdge(exp,area,p,-1,0);
			AddEdge(exp,area,p,1,0);
			AddEdge(exp,area,p,0,-1);
			AddEdge(exp,area,p,0,1);
		}
	}
	
	
}