
import java.awt.image.*;
import java.awt.*;
import java.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;
import java.awt.Point;
import java.util.*;

public class Ada39Path
{
	public static class MyListener implements GridPanel.GridListener
	{
		private GridFileReader gfr;
		private Set<Point> ipe;
		public MyListener(GridFileReader gfr,Set<Point> ipe) { this.gfr = gfr; this.ipe = ipe;}
		
		public int getNumXCells() { return gfr.getWidth(); }
		public int getNumYCells() { return gfr.getHeight(); }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }		

		public boolean drawCellContents(int cx,int cy, BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();
			GridPanel.DrawStringInCell(bi,Color.black,"" + gfr.getBlock("LETTERS")[cx][cy]);
			
			String slant = gfr.getBlock("PATHS")[cx][cy];
			
			int x1,y1,x2,y2;
			
			if (slant.equals("/"))
			{
				x1 = 0; y1 = bi.getHeight();
				x2 = bi.getWidth() ; y2 = 0;
			}
			else
			{
				x1 = 0; y1 = 0;
				x2 = bi.getWidth() ; y2 = bi.getHeight();
			}
			
			g.setColor(Color.BLACK);
			if (ipe.contains(new Point(cx,cy)))
			{
				g.setColor(Color.RED);
			}
			
			g.drawLine(x1,y1,x2,y2);
			
			return true;
		}
	}
		
	public static class Edge
	{
		int x;
		int y;
		char c;
		char s;
		Edge(int x,int y,char c,char s) { this.x = x; this.y = y; this.c = c; this.s = s; }
	}
		
		
	public static void main(String[] args)
	{
		GridFileReader gfr = new GridFileReader("ada39.txt");
		// So we are going to make a graph, where a point exists for every intersection of the 
		// grid, with indexing thus:
		// (0,0) ---- (1,0)
		//   |    0.0   |
		// (0,1) ---- (1,1)
		// i.e. UL corner of any cell x,y is x,y
		//      UR corner is x+1,y
		//      LL corner is x,y+1
		//      LR corner is x+1,y+1
		// any given cell has an edge that connects either UL with LR or UR with LL
		
		SimpleGraph<Point,Edge> mygraph = new SimpleGraph<Point,Edge>(Edge.class);
		
		for (int x = 0 ; x <= gfr.getWidth(); ++x)
		{
			for (int y = 0 ; y <= gfr.getHeight() ; ++y)
			{
				Point p = new Point(x,y);
				mygraph.addVertex(p);
			}
		}
		
		for (int x = 0 ; x < gfr.getWidth(); ++x)
		{
			for (int y = 0 ; y < gfr.getHeight(); ++y)
			{
				String slant = gfr.getBlock("PATHS")[x][y];
				int x1,y1,x2,y2;
				char s;
				if (slant.equals("/"))
				{
					x1 = x; y1 = y+1;
					x2 = x+1; y2=y;
					s = '/';
				}
				else
				{
					x1 = x ; y1 = y;
					x2 = x+1 ; y2 = y+1;
					s = '\\';
				}
			
				Point p1 = new Point(x1,y1);
				Point p2 = new Point(x2,y2);
				Edge e = new Edge(x,y,gfr.getBlock("LETTERS")[x][y].charAt(0),s);
				mygraph.addEdge(p1,p2,e);
			}
		}
		
		FloydWarshallShortestPaths<Point,Edge> fwsp = new FloydWarshallShortestPaths<Point,Edge>(mygraph);
		java.util.List<Edge> edges = fwsp.getShortestPath(new Point(2,0),new Point(4,12)).getEdgeList();
		
		Set<Point> inpathEdges = new HashSet<Point>();
		
		for (Edge e: edges)
		{
			System.out.print(LetterRotate.Rotate(e.c,e.s == '/' ? 1 : -1));
			inpathEdges.add(new Point(e.x,e.y));
		}
		System.out.println("");
				
		
		
		
		
		
		
		GridFrame gf = new GridFrame("Adalogical Aenigma #39",1300,768,new MyListener(gfr,inpathEdges));
	}
}