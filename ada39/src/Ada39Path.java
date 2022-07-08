
import java.awt.image.*;
import java.awt.*;
import java.util.*;

import grid.file.GridFileReader;
import grid.letter.LetterRotate;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;
import java.awt.Point;
import java.util.*;
import java.util.List;

public class Ada39Path
{
	private static Point getGFRPoint(GridFileReader gfr, String name) {
		String pair = gfr.getVar(name);
		String[] parts = pair.split(" ");
		return new Point(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
	}


	public static class MyListener implements GridPanel.GridListener
	{
		private GridFileReader gfr;
		private Set<Point> ipe;
		String[] lines;
		public MyListener(GridFileReader gfr,Set<Point> ipe,String[] lines) { this.gfr = gfr; this.ipe = ipe; this.lines = lines;}
		
		public int getNumXCells() { return gfr.getWidth(); }
		public int getNumYCells() { return gfr.getHeight(); }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		@Override public String[] getAnswerLines() { return lines; }

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
		if (args.length != 1) {
			System.out.println("Bad Command Line");
			System.exit(1);
		}

		GridFileReader gfr = new GridFileReader(args[0]);
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
		Point start = getGFRPoint(gfr,"START");
		Point end = getGFRPoint(gfr,"END");
		String[] lines = new String[] { "Adalogical", "Aenigma", "#39 pather"};

		GraphPath<Point,Edge> path = fwsp.getPath(start,end);
		List<Edge> edges = path.getEdgeList();
		
		Set<Point> inpathEdges = new HashSet<Point>();

		StringBuffer sb = new StringBuffer();
		for (Edge e: edges)
		{
			sb.append(LetterRotate.Rotate(e.c,e.s == '/' ? 1 : -1));
			inpathEdges.add(new Point(e.x,e.y));
		}
		lines[0] = gfr.getVar("NAME");
		lines[1] = sb.toString();
		lines[2] = gfr.getVar("SOLUTION");

				
		
		
		
		
		
		
		GridFrame gf = new GridFrame("Adalogical Aenigma #39",1300,768,new MyListener(gfr,inpathEdges,lines));
	}
}