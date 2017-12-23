
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;

public class Ada23
{

	private static class MyListener implements GridPanel.GridListener
	{
		private Board myBoard;
		State curState;
		
		public MyListener(Board b,State s) { myBoard = b; curState = s; }
	
		public int getNumXCells() { return myBoard.width; }
		public int getNumYCells() { return myBoard.height; }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public boolean drawCellContents(int cx,int cy, BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();

			if (myBoard.hasTrap[cx][cy])
			{
				g.setColor(Color.blue);
				g.fillRect(0,0,bi.getWidth(),bi.getHeight());
			}
			
			char gg = curState.golfgame[cx][cy];
			int inset = 10;
			
			if (gg == '@')
			{
				g.setColor(Color.green);
				g.setStroke(new BasicStroke(10));
				g.drawOval(inset,inset,bi.getWidth() - 2*inset,bi.getHeight()-2*inset);
			}
			else if (gg == '-')
			{
				g.setColor(Color.blue);
				g.setStroke(new BasicStroke(5));
				g.drawLine(0,bi.getHeight()/2,bi.getWidth(),bi.getHeight()/2);
				g.drawLine(bi.getWidth()/2,0,bi.getWidth()/2,bi.getHeight());
			}
			else if (gg == '*')
			{
				g.setColor(Color.gray);
				g.setStroke(new BasicStroke(10));
				g.fillOval(inset,inset,bi.getWidth() - 2*inset,bi.getHeight()-2*inset);
			}
			else if (gg != '.')
			{
				g.setColor(Color.black);
				g.setStroke(new BasicStroke(1));
				g.drawOval(inset,inset,bi.getWidth() - 2*inset,bi.getHeight()-2*inset);
				GridPanel.DrawStringInCell(bi,Color.black,"" + gg);
			}
			else if (myBoard.letters[cx][cy] != '.')
			{
				GridPanel.DrawStringInCell(bi,Color.black,"" + myBoard.letters[cx][cy]);
			}
			
			
			return true;
		}
	}

			



	public static void main(String[] args)
	{
		if (args.length != 1) throw new RuntimeException("bad command line");
		String fname = args[0];
		
		GridFileReader gfr = new GridFileReader(fname,new String[]{"TRAPS","LETTERS","BALLS"});
		
		Board theBoard = new Board(gfr);
		State initialState = new State(gfr);
		
		Logic log = new Logic(theBoard,initialState);
		
		State finalState = log.solutions.firstElement();
		
		GridFrame gf = new GridFrame("Adalogical Aenigma #23",1300,768,new MyListener(theBoard,finalState));
		
	}
}