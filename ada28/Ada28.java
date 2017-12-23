
import java.awt.*;
import java.awt.image.*;
import java.util.*;


public class Ada28
{
	private static class MyListener implements GridPanel.GridListener
	{
		GridFileReader gfr;
		int[][] dists;
		Vector<Point> path;
		
		public MyListener(GridFileReader gfr,int[][] dists,Vector<Point> path) { this.gfr = gfr; this.dists = dists; this.path = path; }
	
		public int getNumXCells() { return gfr.getWidth(); }
		public int getNumYCells() { return gfr.getHeight(); }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public boolean drawCellContents(int cx,int cy, BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();

			if (!gfr.getBlock("NUMBERS")[cx][cy].equals("."))
			{
				g.setColor(Color.red);
				g.drawOval(0,0,bi.getWidth(),bi.getHeight());
				GridPanel.DrawStringInCell(bi,Color.red,gfr.getBlock("NUMBERS")[cx][cy]);
			}
			
			if (!gfr.getBlock("LETTERS")[cx][cy].equals("."))
			{
				GridPanel.DrawStringInCell(bi,Color.black,gfr.getBlock("LETTERS")[cx][cy]);
			}	
			if (gfr.getBlock("BLOCKS")[cx][cy].equals("#"))
			{
				g.setColor(Color.black);
				g.fillRect(0,0,bi.getWidth(),bi.getHeight());
			}
			
			if (path.contains(new Point(cx,cy)))
			{
				g.setColor(Color.blue);
				g.setStroke(new BasicStroke(5));
				g.drawRect(2,2,bi.getWidth()-4,bi.getHeight()-4);
			}
			
			
			
			return true;
		}
	}
	
	private static class MyReference implements GridGraph.GridReference
	{
		GridFileReader gfr;
		public MyReference(GridFileReader gfr) { this.gfr = gfr; }
		public int getWidth() { return gfr.getWidth(); }
		public int getHeight() { return gfr.getHeight(); }
		public boolean isIncludedCell(int x,int y) { return !gfr.getBlock("BLOCKS")[x][y].equals("#"); }
		public boolean edgeExitsEast(int x,int y) { return true; }
		public boolean edgeExitsSouth(int x,int y) { return true; }
	}
	
	private static class QueueElement
	{
		Point curP;
		int curDist;
		public QueueElement(Point p,int i) { curP = p ; curDist = i; }
	}

	private static boolean IsStraightLine(Point p1,Point p2,Point p3)
	{
		int dxa = p1.x - p2.x;
		int dya = p1.y - p2.y;
		int dxb = p2.x - p3.x;
		int dyb = p2.y - p3.y;
		return dxa == dxb && dya == dyb;
	}
	
	
	

	public static void main(String[] args)
	{
		GridFileReader gfr = new GridFileReader("ada28.txt");
		Vector<Point> vistas = new Vector<Point>();
		Map<Point,Integer> vistaValue = new HashMap<Point,Integer>();
		
		for (int x = 0 ; x < gfr.getWidth() ; ++x)
		{
			for (int y = 0 ; y < gfr.getHeight() ; ++y)
			{
				if (gfr.getBlock("NUMBERS")[x][y].equals(".")) continue;
				Point p = new Point(x,y);
				vistas.add(p);
				vistaValue.put(p,Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]));
			}
		}
			
		GridGraph gg = new GridGraph(new MyReference(gfr));
		Point p1 = null;
		Point p2 = null;
		int longest = 0;
		int smallsum = 1000000;
		
		for (int i = 0 ; i < vistas.size() ; ++i)
		{
			for (int j = i+1 ; j < vistas.size() ; ++j)
			{
				Point myp1 = vistas.get(i);
				Point myp2 = vistas.get(j);
			
				int dist = gg.shortestPathBetween(myp1,myp2).size();
				int tot = vistaValue.get(myp1) + vistaValue.get(myp2);
				
				if (dist < longest) continue;
				else if (dist > longest) smallsum = tot;
				else if (dist == longest && tot > smallsum) continue;
				
				p1 = myp1;
				p2 = myp2;
				longest = dist;
				smallsum = tot;
				
								
				System.out.println("Distance between (" + p1.x + "," + p1.y + ") and (" + p2.x + "," + p2.y + ") is " + dist);
			}
		}
		if (vistaValue.get(p2) < vistaValue.get(p1))
		{
			Point t = p1;
			p1 = p2;
			p2 = t;
		}
		
		Point[] deltas = new Point[] { new Point(1,0),new Point(0,1),new Point(-1,0), new Point(0,-1) };
		

		
		Vector<QueueElement> queue = new Vector<QueueElement>();
		int[][] dist = new int[gfr.getWidth()][gfr.getHeight()];
		for (int x = 0 ; x < gfr.getWidth() ; ++x) for (int y = 0 ; y < gfr.getHeight(); ++y) dist[x][y] = -1;
		queue.add(new QueueElement(p2,0));
		
		while (queue.size() > 0)
		{
			QueueElement op = queue.remove(0);
			dist[op.curP.x][op.curP.y] = op.curDist;
			
			for (Point delta : deltas)
			{
				Point np = new Point(op.curP.x+delta.x,op.curP.y+delta.y);
				if (np.x < 0 || np.y < 0 || np.x >= gfr.getWidth() || np.y >= gfr.getHeight()) continue;
				if (gfr.getBlock("BLOCKS")[np.x][np.y].equals("#")) continue;
				if (dist[np.x][np.y] != -1) continue;
				queue.add(new QueueElement(np,op.curDist+1));
			}
		}
			
		Vector<Point> path = new Vector<Point>();
		Point curTail=p1;
		Point prevTail = null;
		path.add(p1);
		
		while(true)
		{
			int curDist = dist[curTail.x][curTail.y];
			if (curDist == 0) break;
			
			Vector<Point> descendings = new Vector<Point>();
			for (Point delta : deltas)
			{
				Point np = new Point(curTail.x+delta.x,curTail.y+delta.y);			
				if (np.x < 0 || np.y < 0 || np.x >= gfr.getWidth() || np.y >= gfr.getHeight()) continue;
				if (dist[np.x][np.y] == -1) continue;
				if (dist[np.x][np.y] >= curDist) continue;
				descendings.add(np);
			}
			
			if (descendings.size() == 1)
			{
				prevTail = curTail;
				curTail = descendings.get(0);
				path.add(curTail);
				continue;
			}
			
			boolean added = false;
			for (Point trial : descendings)
			{
				if (IsStraightLine(prevTail,curTail,trial))
				{
					prevTail = curTail;
					curTail = trial;
					path.add(trial);
					added = true;
				}
			}
			if (!added) throw new RuntimeException("Can't find a straight line!");
		}
		
		int shift = 0;
		for (Point p : path)
		{
			if (!gfr.getBlock("NUMBERS")[p.x][p.y].equals("."))
			{
				shift = Integer.parseInt(gfr.getBlock("NUMBERS")[p.x][p.y]);
			}
			else
			{
				char c = gfr.getBlock("LETTERS")[p.x][p.y].charAt(0);
				System.out.print(LetterRotate.Rotate(c,shift));
			}
		}
		System.out.println("");
		
		
		
		
		
		
		GridFrame gf = new GridFrame("Adalogical Aenigma #28",1300,768,new MyListener(gfr,dist,path));
	}
}