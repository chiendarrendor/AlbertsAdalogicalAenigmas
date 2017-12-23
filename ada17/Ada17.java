
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class Ada17
{
	private static class MyListener implements GridPanel.GridListener
	{
		private Board myBoard = null;
		private Numbers myNums = null;
		
		public MyListener(Board b,Numbers n) { myBoard = b; myNums = n; }
	
		public int getNumXCells() { return myBoard.width; }
		public int getNumYCells() { return myBoard.height; }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public boolean drawCellContents(int cx,int cy, BufferedImage bi)
		{
			Board.CellColor cc = myBoard.colors[cx][cy];
			Color col = Color.black;
			switch(cc)
			{
				case WHITE: col = Color.white; break;
				case GREY: col = Color.lightGray; break;
				case BLACK: col = Color.black; break;
			}
			Graphics2D g = (Graphics2D)bi.getGraphics();
			g.setColor(col);
			g.fillRect(0,0,bi.getWidth(),bi.getHeight());
			
			Point xy = new Point(cx,cy);
			if (myBoard.clueset.cluemap.containsKey(xy))
			{
				for(ClueSet.Clue clue : myBoard.clueset.cluemap.get(xy))
				{
					clue.AlterCellImage(xy,bi);
				}
			}
			
			Numbers.IntSet is = myNums.numbers[cx][cy];
			if (is != null)
			{
				StringBuffer sb = new StringBuffer();
				for (int i = 1 ; i <= 7 ; ++i) { if (is.contains(i)) sb.append(i); }
				GridPanel.DrawStringInCell(bi,Color.black,sb.toString());
			}
			
				
			return true;
		}
	}



	public static void main(String[] args)
	{
		if (args.length == 0) throw new RuntimeException("Bad command line");
		String filename = args[0];
		
		Board theBoard = new Board(filename);
		Numbers nums = new Numbers(theBoard);
		Solver s = new Solver(theBoard.clueset,nums);
		
		Numbers solnum = s.solutions.elementAt(0);
		
		for (int x = 0 ; x < theBoard.width ; ++x)
		{
			int sum = 0;
			for (int y = 0 ; y < theBoard.height ; ++y)
			{
				if (theBoard.colors[x][y] != Board.CellColor.GREY) continue;
				sum += solnum.numbers[x][y].iterator().next();
			}
			System.out.print("" + Character.toString((char)(sum + 64)));
		}
		System.out.println("");
		
		
		
		GridFrame gf = new GridFrame("Adalogical Aenigma #17",1300,768,new MyListener(theBoard, solnum));
		
	}
}