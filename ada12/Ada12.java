import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.FixedSizePanel;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import grid.spring.SinglePanelFrame;

import java.awt.image.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public class Ada12
{
	static Board theBoard;
	static String[] lines = new String[] {"Adalogical Aenigma","#12 Solver"};

	private static class MyListener implements GridPanel.GridListener
	{
		public int getNumXCells() { return theBoard.width; }
		public int getNumYCells() { return theBoard.height; }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public String[] getAnswerLines() { return lines; }
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
					g.setColor(Color.cyan);
					g.fillRect(ulx,uly,w,h);
					break;
			}
			
			if (ci.containsToken)
			{
				g.setColor(ci.tokenOriginalPosition != null ? Color.green : Color.red);
				g.drawOval(ulx,uly,w,h);
				GridPanel.DrawStringInCell(bi,Color.black,ci.tokenString());			
			}

			if (theBoard.hasLetter(x,y)) {
				GridPanel.DrawStringInCorner(bi, Color.BLACK,""+theBoard.getLetter(x,y), Direction.NORTHWEST);
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
			//System.out.println(" X: " + minX + " to " + maxX + " Y: " + minY + " to " + maxY);
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
				if (!pendowns.elementAt(i)) continue;
				g.drawLine(p1.x,p1.y,p2.x,p2.y);
			}
		}
		
	}
			
			
			
			
	
	

	public static void main(String[] args)
	{
		if (args.length != 1) {
			System.out.println("Bad Command Line");
			System.exit(1);
		}

		theBoard = new Board(args[0]);

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

		System.out.println("# of Solutions: " + solutions.size());

		if (solutions.size() == 1) {
			theBoard = solutions.elementAt(0);
			lines[0] = "--unused--";
			lines[1] = theBoard.getSolution();

			if (theBoard.hasDrawVectorStage()) {
				Point p = new Point(0, 0);
				Vector<Point> vectorPoints = new Vector<Point>();
				Vector<Boolean> pendowns = new Vector<Boolean>();
				vectorPoints.add(new Point(0, 0));
				for (Board.DrawVector dv : theBoard.vectors) {
					Point dp = dv.getVector();
					p.x += dp.x;
					p.y += dp.y;
					vectorPoints.add(new Point(p.x, p.y));
					pendowns.add(dv.pendown);
					//System.out.println("Draw: " + dp.x + "," + dp.y + " " + (dv.pendown ? "pendown" : "penup"));
				}
				SinglePanelFrame spf = new SinglePanelFrame("Adalogical Aenigma #12 Vectors", new VectorPanel(600, 600, vectorPoints, pendowns));
			} else {
				StringBuffer sb = new StringBuffer();
				for (int y = 0 ; y < theBoard.height ; ++y) {
					for (int x = 0 ; x < theBoard.width ; ++x) {
						Board.CellInfo ci = theBoard.cells[x][y];
						if (!ci.containsToken) continue;
						if (!theBoard.hasLetter(x,y)) continue;
						int size = Math.abs(ci.tokenOriginalPosition.y - ci.tokenCurrentPosition.y) +
								Math.abs(ci.tokenOriginalPosition.x - ci.tokenCurrentPosition.x);
						sb.append(LetterRotate.Rotate(theBoard.getLetter(x,y),size));
					}
					lines[0] = sb.toString();
				}
			}
		}
		
		
		GridFrame gf = new GridFrame("Adalogical Aenigma #12 Board",1024,768,new MyListener());
		

	}
}