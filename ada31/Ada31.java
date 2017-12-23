
import java.awt.image.*;
import java.awt.*;
import java.util.*;


public class Ada31
{

	private static class MyListener implements GridPanel.GridListener
	{
		private Board myBoard = null;
		State curState;
		
		
		private void DrawOnePath(BufferedImage bi, int curx,int cury, int ox, int oy)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();
			g.setColor(Color.blue);	
			
			int centerx = bi.getWidth()/2;
			int centery = bi.getHeight()/2;
			
			int edgex = centerx;
			int edgey = centery;
			
			if (curx < ox) { edgex = bi.getWidth(); edgey = centery; }
			if (curx > ox) { edgex = 0 ; edgey = centery; }
			if (cury < oy) { edgex = centerx ; edgey = bi.getHeight(); }
			if (cury > oy) { edgex = centerx ; edgey = 0; }
			g.drawLine(centerx,centery,edgex,edgey);
		}
			
			
			
		
		private void DrawPath(BufferedImage bi, Vector<Point> path, int curx, int cury)
		{
			if (path.size() == 0) return;
			

			int curidx;
			for (curidx = 0 ; curidx < path.size() ; ++curidx) { if (path.elementAt(curidx).x == curx && path.elementAt(curidx).y == cury) break; }
			
			if (curidx != 0)
			{
				DrawOnePath(bi,curx,cury,path.elementAt(curidx-1).x,path.elementAt(curidx-1).y);
			}
			
			if (curidx != path.size()-1)
			{
				DrawOnePath(bi,curx,cury,path.elementAt(curidx+1).x,path.elementAt(curidx+1).y);
			}
		}
			
		
		
		
		
		
		
		public MyListener(Board b,State s) { myBoard = b; curState = s; }
	
		public int getNumXCells() { return myBoard.width; }
		public int getNumYCells() { return myBoard.height; }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public boolean drawCellContents(int cx,int cy, BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();
			
			State.ActorInfo ai = curState.actorAt(cx,cy);
			if (ai == null) return false;
			
			Point first = ai.path.firstElement();
			Point last = ai.path.lastElement();
			
			if (first.x == cx && first.y == cy)
			{
				g.setColor(Color.red);
				g.setStroke(new BasicStroke(5));
				g.drawOval(10,10,bi.getWidth()-20,bi.getHeight()-20);
				GridPanel.DrawStringInCell(bi,Color.black,"" + ai.house);
			}
			
			if (last.x == cx && last.y == cy)
			{
				g.setColor(Color.green);
				g.setStroke(new BasicStroke(5));				
				g.drawOval(10,10,bi.getWidth()-20,bi.getHeight()-20);
				GridPanel.DrawStringInCell(bi,Color.black,"" + ai.house);
			}
			
			DrawPath(bi,ai.path,cx,cy);
			
			g.setColor(Color.black);
			if (myBoard.numbers[cx][cy] != Board.NUMBER_SENTINEL)
			{	
				StringBuffer sb = new StringBuffer();
				sb.append(myBoard.numbers[cx][cy] < 0 ? "" : "+").append(myBoard.numbers[cx][cy]);
				int downset = g.getFontMetrics().getHeight();
				int rightinset = g.getFontMetrics().stringWidth(sb.toString()) + 3;
				g.drawString(sb.toString(),bi.getWidth()-rightinset,downset);
			}

		
/* this drawing stuff was invaluable for solving		
			if (curState.isWas(cx,cy))
			{
				g.setColor(Color.red);
				
				g.drawLine(10,10,bi.getWidth()-10,bi.getHeight()-10);
				g.drawLine(bi.getWidth()-10,10,10,bi.getHeight()-10);
			}
			else if (curState.isHome(cx,cy))
			{
				g.setColor(Color.green);
				g.setStroke(new BasicStroke(10));
				g.drawOval(10,10,bi.getWidth()-10,bi.getHeight()-10);
			}	
			else if (curState.isMove(cx,cy) && curState.actorCurrentlyAt(cx,cy) == null)
			{	
				g.setColor(Color.blue);
				g.setStroke(new BasicStroke(10));
				g.drawRect(10,10,bi.getWidth()-20,bi.getHeight()-20);
			} 
			else if (curState.MustMove(cx,cy))
			{
				g.setColor(Color.yellow);
				g.setStroke(new BasicStroke(10));
				g.drawOval(10,10,bi.getWidth()-10,bi.getHeight()-10);				
			}
*/			
			
	


				
			return true;
		}
	}
	
	private static class MyEdgeListener implements GridPanel.EdgeListener
	{
		private Board myBoard = null;
		public MyEdgeListener(Board b) { myBoard = b; }
		
		public GridPanel.EdgeListener.EdgeDescriptor onBoundary()
		{
			return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,5);
		}
		
		public GridPanel.EdgeListener.EdgeDescriptor toEast(int x,int y)
		{
			int w = myBoard.regions[x][y] == myBoard.regions[x+1][y] ? 1 : 5;
			return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,w);
		}	
	
		public GridPanel.EdgeListener.EdgeDescriptor toSouth(int x,int y)
		{
			int w = myBoard.regions[x][y] == myBoard.regions[x][y+1] ? 1 : 5;
			return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,w);
		}
	}

	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Bad Command Line");
			System.exit(1);
		}
		
		GridFileReader gfr = new GridFileReader(args[0]);
	
		if (!gfr.hasBlock("REGIONS") || !gfr.hasBlock("LETTERS") || !gfr.hasBlock("NUMBERS"))
		{
			System.out.println("Bad description file");
			System.exit(1);
		}
		
		Board theBoard = new Board(gfr.getWidth(),gfr.getHeight(),gfr.getBlock("REGIONS"),gfr.getBlock("NUMBERS"));
		State initialState = new State(theBoard,gfr.getBlock("LETTERS"));
		
		MoveMaker.MoveMaker("moves.txt",initialState);
		LogicStatus ls;
	
/*	
		Logic.WalkingIdentityReference lwir = new Logic.WalkingIdentityReference(initialState,initialState.actorCurrentlyAt(15,8));
		for (int y = 0 ; y < lwir.getHeight() ; ++ y)
		{
			for (int x = 0 ; x < lwir.getWidth() ; ++ x)
			{
				System.out.print("" + (lwir.isIncludedCell(x,y) ? "#" : ".") + " ");
			}
			System.out.println("");
		}
		System.exit(1);
	*/	
		
		boolean doLoop;
		
		do
		{
			doLoop = false;
		
			ls = Logic.HouseIdentitiesByWalking(initialState);
			if (ls == LogicStatus.CONTRADICTION) throw new RuntimeException("Walking Contradiction");
			if (ls == LogicStatus.LOGICED) doLoop = true;
			ls = Logic.FindUniqueHouses(initialState);
			if (ls == LogicStatus.CONTRADICTION) throw new RuntimeException("Unique Houses Contradiction");
			if (ls == LogicStatus.LOGICED) doLoop = true;
			ls = Logic.FindUniqueRegions(initialState);
			if (ls == LogicStatus.CONTRADICTION) throw new RuntimeException("Unique Regions Contradiction");
			if (ls == LogicStatus.LOGICED) doLoop = true;			
		} while (doLoop);
		
		System.out.println("--------------------------------------");
		initialState.ShowState();

		System.out.print("Final Answer: ");
		for (int y = 0 ; y < initialState.getHeight(); ++y)
		{
			for (int x = 0 ; x < initialState.getWidth(); ++x)
			{
				if (theBoard.numbers[x][y] == Board.NUMBER_SENTINEL) continue;
				int dist = theBoard.numbers[x][y];
				Board.Region region = theBoard.regions[x][y];
				char house = initialState.regionPossibles.get(region).iterator().next();
				System.out.print(LetterRotate.Rotate(house,dist));
			}
		}
		System.out.println("");



		
		GridFrame gf = new GridFrame("Adalogical Aenigma #31",1300,768,new MyListener(theBoard,initialState),new MyEdgeListener(theBoard));
		
	}
}

		