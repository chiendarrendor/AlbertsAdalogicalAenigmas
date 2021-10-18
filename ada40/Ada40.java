import grid.file.GridFileReader;
import grid.graph.GridGraph;
import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
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
		String[] lines;
		public MyListener(Board b,String[] lines) { this.b = b; this.lines = lines; }

		public int getNumXCells() { return b.getWidth(); }
		public int getNumYCells() { return b.getHeight(); }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		@Override public String[] getAnswerLines() { return lines; }


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
		if (args.length != 1) {
			System.out.println("Bad Command Line");
			System.exit(1);
		}


		GridFileReader gfr = new GridFileReader(args[0]);
		RectangleList rl = new RectangleList(gfr.getWidth(),gfr.getHeight());
		String[] lines = new String[] { "Adalogical", "Aenigma","#40 Solver" };
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


        Board fb = solutions.elementAt(0);
        // now, to get the final answer, we need to determine the size of each area.  standard
        // orthogonally connected GridGraph where walls prevent valid vertices and board is otherwise connected
        // the size of the cell's connected set will get us the info we seek.
        GridGraph gg = new GridGraph(new WallGraph(fb));

        // the above solver cannot solve for non-rectangleness of non-numbered areas,
        // but seems to provide unique solutions regardless...using this GridGraph, we should at least be able
        // to detect all empty spaces that are not rectangular so a solver can add to the 'WALLS' GFR block themselves

        for ( Set<Point> cc : gg.connectedSets()) {
            int minx = Integer.MAX_VALUE;
            int miny = Integer.MAX_VALUE;
            int maxx = Integer.MIN_VALUE;
            int maxy = Integer.MIN_VALUE;

            for (Point p : cc) {
                if (p.x < minx) minx = p.x;
                if (p.y < miny) miny = p.y;
                if (p.x > maxx) maxx = p.x;
                if (p.y > maxy) maxy = p.y;
            }

            int xsize = maxx - minx + 1;
            int ysize = maxy - miny + 1;

            if (cc.size() != xsize * ysize) {
                System.out.println("Non-rectangle found: ");
                for (Point p: cc) System.out.print(p);
                System.out.println("");
            }
        }



		// The above code creates a unique solution that is _almost_ right
		// all empty space must be rectangles; there is one area that is not,
		// and since the path goes through that area, there is only one
		// way to make the area legal rectangles:

		//fb.getCellInfo(12,0).type = CellInfo.WALL;







		StringBuffer sb = new StringBuffer();
		for (int y = 0 ; y < fb.getHeight() ; ++y)
		{
			for(int x = 0 ; x < fb.getWidth() ; ++x)
			{
                CellInfo ci = fb.getCellInfo(x, y);
			    if (gfr.hasVar("CLUELOGIC") && gfr.getVar("CLUELOGIC").equals("ADDENDUM31")) {
			        if (fb.getChar(x,y) == '.') continue;
			        if (ci.type == CellInfo.WALL) continue;
			        Point p = new Point(x,y);
			        Set<Point> rectCells = gg.connectedSetOf(p);
			        int size = rectCells.size();

			        boolean isAdjacent = false;
			        for (Direction d: Direction.orthogonals()) {
			            Point ap = d.delta(x,y,1);
			            if (!gfr.inBounds(ap)) continue;
			            if (!fb.getNumber(ap.x,ap.y).equals(".")) {
			                isAdjacent = true;
			                break;
                        }
                    }

                    if (isAdjacent) continue;


			        sb.append(LetterRotate.Rotate(fb.getChar(x,y),size));

                } else {

                    if (ci.type != CellInfo.UNKNOWN) continue;
                    Set<Point> cc = gg.connectedSetOf(new Point(x, y));
                    char nc = LetterRotate.Rotate(fb.getChar(x, y), cc.size());
                    sb.append(nc);
                }
			}
		}

		lines[0] = gfr.getVar("PUZZLENAME");
		lines[1] = sb.toString();
		lines[2] = gfr.getVar("SOLUTION");


		new GridFrame("Adalogical Aenigma #40", 1300, 768, new MyListener(solutions.elementAt(0),lines));


    }
	
}