import sun.rmi.runtime.Log;

import java.awt.*;
import java.awt.image.*;
import java.util.Set;
import java.util.Vector;

public class Ada40
{
	public static class MyListener implements GridPanel.GridListener
	{
		private Board b;
		public MyListener(Board b) { this.b = b; }

		public int getNumXCells() { return b.getWidth(); }
		public int getNumYCells() { return b.getHeight(); }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }		

		public boolean drawCellContents(int cx,int cy, BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();
			if (b.getChar(cx,cy) != '.')
			{
				GridPanel.DrawStringUpperLeftCell(bi,Color.black,"" + b.getChar(cx,cy));
			}
			
			CellInfo ci = b.getCellInfo(cx,cy);

			switch (ci.type)
			{
				case CellInfo.RECTANGLE:
					StringBuffer sb = new StringBuffer();
					if (!b.getNumber(cx,cy).equals("."))
					{
						sb.append(""+b.getNumber(cx,cy));
					}

					sb.append("(").append(ci.rectnum).append(")");

					GridPanel.DrawStringInCell(bi,Color.black,sb.toString());
					break;
				case CellInfo.EMPTY:
					GridPanel.DrawStringInCell(bi,Color.blue,"X");
					break;
				case CellInfo.WALL:
					g.setColor(Color.black);
					g.fillRect(5,5,bi.getWidth() - 10, bi.getHeight()-10);
					break;

			}


			return true;
		}
	}

	private static class WallGraph implements GridGraph.GridReference
	{
		Board board;
		public WallGraph(Board board) { this.board = board;}
		public int getWidth() { return board.getWidth();}
		public int getHeight() { return board.getHeight();}
		public boolean isIncludedCell(int x, int y) { return board.getCellInfo(x,y).type != CellInfo.WALL; }
		public boolean edgeExitsEast(int x, int y) { return true; }
		public boolean edgeExitsSouth(int x, int y) { return true;}
	}


	public static void main(String[] args)
	{
		GridFileReader gfr = new GridFileReader("ada40.txt");
		RectangleList rl = new RectangleList(gfr.getWidth(),gfr.getHeight());
		Board b = new Board(gfr,rl);

		Vector<Board> queue = new Vector<Board>();
		Vector<Board> solutions = new Vector<Board>();

		queue.add(b);

		while(queue.size() > 0)
		{
			System.out.println("Queue Size: " + queue.size());
			Board curb = queue.remove(0);

			if (curb.solved())
			{
				solutions.add(curb);
				continue;
			}

			LogicStatus lstat = curb.update();

			switch(lstat)
			{
				case LOGICED:
					queue.add(curb);
					break;
				case CONTRADICTION:
					break;
				case STYMIED:
					Vector<Board> children = curb.successors();
					queue.addAll(0,children);
					break;
			}


		}

		System.out.println("Number of Solutions: " + solutions.size() );
		// The above code creates a unique solution that is _almost_ right
		// all empty space must be rectangles; there is one area that is not,
		// and since the path goes through that area, there is only one
		// way to make the area legal rectangles:
		Board fb = solutions.elementAt(0);
		fb.getCellInfo(12,0).type = CellInfo.WALL;



        new GridFrame("Adalogical Aenigma #40", 1300, 768, new MyListener(solutions.elementAt(0)));

        // now, to get the final answer, we need to determine the size of each area.  standard
		// orthogonally connected GridGraph where walls prevent valid vertices and board is otherwise connected
		// the size of the cell's connected set will get us the info we seek.
		GridGraph gg = new GridGraph(new WallGraph(fb));

		StringBuffer sb = new StringBuffer();
		for (int y = 0 ; y < fb.getHeight() ; ++y)
		{
			for(int x = 0 ; x < fb.getWidth() ; ++x)
			{
				CellInfo ci = fb.getCellInfo(x,y);
				if (ci.type != CellInfo.UNKNOWN) continue;
				Set<Point> cc = gg.connectedSetOf(new Point(x,y));
				char nc = LetterRotate.Rotate(fb.getChar(x,y),cc.size());
				sb.append(nc);
			}
		}
		System.out.println(sb.toString());



    }
	
}