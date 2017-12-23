import java.awt.image.*;
import java.awt.*;
import java.util.*;

public class Ada12
{
	static Board theBoard;

	private static class MyListener implements GridPanel.GridListener
	{
		public int getNumXCells() { return theBoard.width; }
		public int getNumYCells() { return theBoard.height; }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public boolean drawCellContents(int x,int y,BufferedImage bi)
		{
			Board.CellInfo ci = theBoard.cells[x][y];
			Graphics2D g = (Graphics2D)bi.getGraphics();
			// insetting slightly
			int INSET = 2;
			int ulx = INSET;
			int uly = INSET;
			
			int w = bi.getWidth() - 2*INSET;
			int h = bi.getHeight() - 2*INSET;
			
			switch(ci.type)
			{
				case TARGET: 
					g.setColor(Color.green);
					g.fillOval(ulx + 5,uly+5,w-10,h-10);
					break;
				case NONTARGET: 
					g.setColor(Color.red);
					g.fillOval(ulx + 5,uly+5,w-10,h-10);
					break;
				case UNKNOWN:
					break;
				case PATH:
					g.setColor(Color.blue);
					g.fillRect(ulx,uly,w,h);
					break;
			}
			
			if (ci.containsToken)
			{
				g.setColor(ci.tokenOriginalPosition != null ? Color.green : Color.red);
				g.drawOval(ulx,uly,w,h);
				GridPanel.DrawStringInCell(bi,Color.black,ci.tokenString());			
			}
		
		
			return true;
		}
	}

	private static class VectorPanel extends FixedSizePanel
	{
		private int INSET=20;
		Vector<Point> myV = new Vector<Point>();
		Vector<Boolean> pendowns;
		
		public VectorPanel(int width,int height,Vector<Point> rawV,Vector<Boolean> pendowns)
		{
			super(width,height);
			
			this.pendowns = pendowns;
			int drawWidth = width - 2*INSET;
			int drawHeight = height - 2*INSET;
			
			// we are going to make our own scaling and translation of the set of vectors
			int minX = rawV.firstElement().x;
			int maxX = rawV.firstElement().x;
			int minY = rawV.firstElement().y;
			int maxY = rawV.firstElement().y;
			
			for (int i = 1 ; i < rawV.size() ; ++i)
			{
				if (rawV.elementAt(i).x < minX) minX = rawV.elementAt(i).x;
				if (rawV.elementAt(i).x > maxX) maxX = rawV.elementAt(i).x;
				if (rawV.elementAt(i).y < minY) minY = rawV.elementAt(i).y;
				if (rawV.elementAt(i).y > maxY) maxY = rawV.elementAt(i).y;
			}
			System.out.println(" X: " + minX + " to " + maxX + " Y: " + minY + " to " + maxY);
			int xTrans = -minX;
			int yTrans = -minY;
			int xScale = drawWidth / (maxX - minX);
			int yScale = drawHeight / (maxY - minY);
			
			for (Point p : rawV)
			{
				myV.add( new Point ( (p.x+xTrans) * xScale + INSET, (p.y+yTrans) * yScale + INSET));
			}
		}
		
		public void paint(Graphics g)
		{
			g.setColor(Color.red);
			for (int i = 0 ; i < myV.size() - 1 ; ++i)
			{
				Point p1 = myV.elementAt(i);
				Point p2 = myV.elementAt(i+1);
				if (!pendowns.elementAt(i+1)) continue;
				g.drawLine(p1.x,p1.y,p2.x,p2.y);
			}
		}
		
	}
			
			
			
			
	
	

	public static void main(String[] args)
	{
		theBoard = new Board("ada12.txt");
		
		
		Vector<Board> queue = new Vector<Board>();
		Vector<Board> solutions = new Vector<Board>();
		
		queue.add(theBoard);
		
		while(queue.size() > 0)
		{
			System.out.println("queue size: " + queue.size() + " solution size: " + solutions.size());
			Board qb = queue.remove(0);
			if (qb.isSolution()) 
			{
				solutions.add(qb);
				continue;
			}
			
			switch(qb.DoLogic())
			{
				case CONTRADICTION:
					break;
				case LOGIC:
					queue.add(qb);
					break;
				case STYMIED:
					queue.addAll(qb.Guesses());
					break;
			}
		}
		
		// 7 (currently) gets me an image that looks like stonehenge, which fits the solution word space
		theBoard = solutions.elementAt(7);
		
		//theBoard = solutions.elementAt(0);
		for (String s : theBoard.why)
		{
			System.out.println(s);
		}
		
		
		
		Point p = new Point(0,0);
		Vector<Point> vectorPoints = new Vector<Point>();
		Vector<Boolean> pendowns = new Vector<Boolean>();
		for (Board.DrawVector dv : theBoard.vectors)
		{
			Point dp = dv.getVector();
			p.x += dp.x;
			p.y += dp.y;
			vectorPoints.add(new Point(p.x,p.y));
			pendowns.add(dv.pendown);
		}
		
		
		GridFrame gf = new GridFrame("Adalogical Aenigma #12 Board",1024,768,new MyListener());
		
		SinglePanelFrame spf = new SinglePanelFrame("Adalogical Aenigma #12 Vectors",new VectorPanel(600,600,vectorPoints,pendowns));	
	}
}