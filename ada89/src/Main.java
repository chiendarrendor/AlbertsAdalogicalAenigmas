import com.sun.org.apache.regexp.internal.RE;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Main {
	public static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
		Board b;
		String[] lines;
		public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }

		@Override public int getNumXCells() { return b.getWidth();	}
		@Override public int getNumYCells() { return b.getHeight(); }
		@Override public boolean drawGridNumbers() { return true; }
		@Override public boolean drawGridLines() { return true; }
		@Override public boolean drawBoundary() { return true; }
		@Override public String[] getAnswerLines() { return lines; }

		private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
		private static final EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);

		private EdgeDescriptor inDirection(int x,int y, Direction d) {
			Point op = d.delta(x,y,1);
			return b.getRegionId(x,y) == b.getRegionId(op.x,op.y) ? PATH : WALL;
		}

		@Override public EdgeDescriptor onBoundary() { return WALL; }
		@Override public EdgeDescriptor toEast(int x, int y) { return inDirection(x,y,Direction.EAST); }
		@Override public EdgeDescriptor toSouth(int x, int y) { return inDirection(x,y,Direction.SOUTH); }



		@Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
			Graphics2D g = (Graphics2D)bi.getGraphics();
			g.setColor(Color.CYAN);
			g.setStroke(new BasicStroke(5.0f));
			int cenx = bi.getWidth()/2;
			int ceny = bi.getHeight()/2;
			GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy),Direction.NORTHWEST);

			for (Direction d : Direction.orthogonals()) {
				switch(b.getEdge(cx,cy,d)) {
					case UNKNOWN:
						break;
					case PATH:
						switch(d) {
							case NORTH: g.drawLine(cenx,ceny,cenx,0); break;
							case SOUTH: g.drawLine(cenx,ceny,cenx,bi.getHeight()); break;
							case WEST: g.drawLine(cenx,ceny,0,ceny); break;
							case EAST: g.drawLine(cenx,ceny,bi.getWidth(),ceny); break;
						}
						break;
					case WALL:
						GridPanel.DrawStringInCorner(bi, Color.RED,"X",d);
						break;
				}
			}

			return true;
		}


	}
    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
	    String[] lines = new String[] {"Adalogical", "Aenigma #89"};
	    int maxdepth = 10000;

	    IntermediateProcessor proc = new IntermediateProcessor();
	    Solver s = new Solver(proc,maxdepth);
	    s.init(b);

		s.Solve(b);
		//s.testRecursion(b);

		if (s.GetSolutions().size() == 1) {
			b = s.GetSolutions().get(0);
			Path p = b.getPaths().iterator().next();
			Point porig = new Point(0,b.getHeight()-1);
			Path.Cursor pc = p.getCursor(porig.x,porig.y);
			Point pnext = new Point(0,b.getHeight()-2);
			if (!pnext.equals(pc.getNext())) {
				p.reverse();
				pc = p.getCursor(porig.x,porig.y);
			}
			if (!pnext.equals(pc.getNext())) throw new RuntimeException("next to lower left isn't lower left?");
			StringBuffer sb = new StringBuffer();
			while(!pc.getNext().equals(porig)) {
				if (Turns.makeTurn(pc.getPrev(),pc.get(),pc.getNext()) == Turns.RIGHT) {
					sb.append(b.getLetter(pc.get().x,pc.get().y));
				}
				pc.next();
			}
			lines[0] = sb.toString();
			lines[1] = b.gfr.getVar("SOLUTION");
		} else if (proc.bestBoard != null){
			lines[0] = "best found after";
			lines[1] = "" + maxdepth + " iterations is " + proc.bestGrade + " unknowns";
			b = proc.bestBoard;
		}


		MyListener myl = new MyListener(b,lines);
		GridFrame gf = new GridFrame("Adalogical Aenigma #89 Solver",1200,800,myl,myl);
    }





}
