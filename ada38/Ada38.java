
import java.awt.image.*;
import java.awt.*;
import java.util.*;


public class Ada38
{

	private static class MyListener implements GridPanel.GridListener
	{
		private Board myBoard = null;
		State curState;
	
		public MyListener(Board b,State s) { myBoard = b; curState = s; }
	
		public int getNumXCells() { return myBoard.width; }
		public int getNumYCells() { return myBoard.height; }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		
		private static int OVALINSET = 10;
		
		public boolean drawCellContents(int cx,int cy, BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();
			if (myBoard.GetLetter(cx,cy) != '.') 
			{
				GridPanel.DrawStringUpperLeftCell(bi,Color.black,""+myBoard.GetLetter(cx,cy));
			}
			
			if (curState.isShadow(cx,cy))
			{
				g.setColor(Color.RED);
				g.drawLine(OVALINSET,OVALINSET,bi.getWidth()-OVALINSET,bi.getHeight()-OVALINSET);
				g.drawLine(OVALINSET,bi.getHeight()-OVALINSET,bi.getWidth()-OVALINSET,OVALINSET);
				return true;
			}
			
			
			
			State.Traveler curT = curState.getTraveler(cx,cy);
			if (curT != null)
			{
				g.setColor(curT.moved ? Color.GREEN : Color.BLACK);
				g.drawOval(OVALINSET,OVALINSET,bi.getWidth()-2*OVALINSET,bi.getHeight()-2*OVALINSET);
				if (curT.movedist != 0) GridPanel.DrawStringInCell(bi,Color.black,"" + curT.movedist);
			}
			
			

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
	
		if (!gfr.hasBlock("REGIONS") || !gfr.hasBlock("LETTERS") || !gfr.hasBlock("TRAVELLERS"))
		{
			System.out.println("Bad description file");
			System.exit(1);
		}
		
		Board theBoard = new Board(gfr.getWidth(),gfr.getHeight(),gfr.getBlock("REGIONS"),gfr.getBlock("LETTERS"));
		State initialState = new State(theBoard,gfr.getBlock("TRAVELLERS"));
		
		Vector<State> results = Logic.Logic(initialState);
		if (results.size() == 0)
		{
			System.out.println("No results found");
		}
		else
		{
			System.out.println("Results found: " + results.size());
			State result = results.firstElement();
			GridFrame gf = new GridFrame("Adalogical Aenigma #38",1300,768,new MyListener(theBoard,result),new MyEdgeListener(theBoard));
			
			for (int y = 0 ; y < theBoard.getHeight() ; ++y)
			{
				for (int x = 0 ; x < theBoard.getWidth(); ++x)
				{
					if (result.isShadow(x,y) || result.travelerboard[x][y] == null) continue;
					char c = theBoard.letters[x][y];
					int rot = result.travelerboard[x][y].movedist;
					char r = LetterRotate.Rotate(c,rot);
					System.out.print("" + r);
				}
			}
			System.out.println("");
			
			
			
			
			
		}
		
		
	}
}

		