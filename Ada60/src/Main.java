import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Main {

	private static class MyGridListener implements GridPanel.GridListener {
		private Board b;
		private String[] lines;
		public MyGridListener(Board b, String[] lines) { this.b = b; this.lines = lines; }

		public String getAnswerText() {
			StringBuffer sb = new StringBuffer();
			sb.append("<html><font size=\"5\">");
			Arrays.stream(lines).forEach(line->sb.append(line).append("<br>"));
			sb.append("</font></html>");
			return sb.toString();
		}

		@Override public int getNumXCells() { return b.getWidth(); }
		@Override public int getNumYCells() { return b.getHeight(); }
		@Override public boolean drawGridNumbers() { return true; }
		@Override public boolean drawGridLines() { return true; }
		@Override public boolean drawBoundary() { return true; }

		private static final int INSET=10;
		@Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
			Graphics2D g = (Graphics2D)bi.getGraphics();
			if (b.isLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi, Color.BLACK,""+b.getLetter(cx,cy));
			if (b.isDot(cx,cy)) {
				Color dotcolor = Color.GRAY;
				switch(b.getColor(cx,cy)) {
					case 'B': dotcolor = Color.BLACK; break;
					case 'W': dotcolor = Color.WHITE; break;
				}
				g.setColor(dotcolor);
				g.fillOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
				g.setColor(Color.BLACK);
				g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
			}

			for (Direction d : Direction.orthogonals()) {
				Point center = new Point(bi.getWidth()/2,bi.getHeight()/2);
				int dist = (d == Direction.NORTH || d == Direction.SOUTH) ? bi.getHeight()/2 : bi.getWidth()/2;
				g.setStroke(new BasicStroke(5.0f));
				g.setColor(Color.GREEN);
				switch(b.getEdge(cx,cy,d)) {
					case WALL: GridPanel.DrawStringInCorner(bi,Color.RED,"X",d); break;
					case PATH:
						Point ep = d.delta(center,dist);
						g.drawLine(center.x,center.y,ep.x,ep.y);
				}
			}



			return true;
		}
	}

	// given a point at a bend, one of the arms goes north-south
	// calculate how many cells north or south it goes before it bends again.
	private static int verticalOf(Path path,Point p) {
		Path.Cursor c = path.getCursor(p.x,p.y);
		Point fp = c.getNext();
		Point rp = c.getPrev();
		// determine which direction goes north/south
		Direction forDir = Direction.fromTo(p.x,p.y,fp.x,fp.y);
		Direction revDir = Direction.fromTo(p.x,p.y,rp.x,rp.y);

		int count = 0;
		if (forDir == Direction.NORTH || forDir == Direction.SOUTH) {
			while(Direction.fromTo(c.get().x,c.get().y,c.getNext().x,c.getNext().y) == forDir) { ++count; c.next(); }
		} else {
			while(Direction.fromTo(c.get().x,c.get().y,c.getPrev().x,c.getPrev().y) == revDir) { ++count; c.prev(); }
		}
		return count;
	}


    public static void main(String[] args) {
	    if (args.length == 0) {
	        System.out.println("bad command line");
	        System.exit(1);
        }
        Board b = new Board(args[0]);
	    Solver s = new Solver(b);
	    s.Solve(b);
		b = s.GetSolutions().get(0);

		Path p = b.getPaths().iterator().next();
		Path.Cursor c = p.getCursor(0,0);
		if (!c.getNext().equals(new Point(1,0))) {
			p.reverse();
			c = p.getCursor(0,0);
		}
		c.next();
		StringBuffer sb = new StringBuffer();
		while(!c.get().equals(new Point(0,0))) {
			if (b.isLetter(c.get().x,c.get().y) &&
				Turns.isBend(Turns.makeTurn(c.getPrev(),c.get(),c.getNext()))) {
				sb.append(LetterRotate.Rotate(b.getLetter(c.get().x, c.get().y), verticalOf(p, c.get())));
			}
			c.next();
		}
		System.out.println("Solution: " + sb.toString());

		String[] lines = new String[] { sb.toString(), b.getSolution() };



	    GridFrame gf = new GridFrame("Adalogical Aenigma #60 Solver",1000,800,
				new MyGridListener(b,lines));
    }
}
