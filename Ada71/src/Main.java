import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {

	private static class MyListener implements GridPanel.GridListener {
		Board b;
		String[] lines;
		public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
		@Override public int getNumXCells() { return b.getWidth(); }
		@Override public int getNumYCells() { return b.getHeight(); }
		@Override public boolean drawGridNumbers() { return true; }
		@Override public boolean drawGridLines() { return true; }
		@Override public boolean drawBoundary() { return true; }
		@Override public String[] getAnswerLines() { return lines; }

		@Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
			Color fillcolor = null;
			Graphics2D g = (Graphics2D)bi.getGraphics();
			if (!b.hasLetter(cx,cy)) fillcolor = Color.DARK_GRAY;
			CellSet cs = b.getCell(cx,cy);
			if (cs != null && cs.isSolo() && cs.getSolo() == CellSet.BLACK) fillcolor = Color.BLACK;

			if (fillcolor != null) {
				g.setColor(fillcolor);
				g.fillRect(0,0,bi.getWidth(),bi.getHeight());
				return true;
			}

			if (cs == null) return true;

			GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
			if (cs.isSolo()) {
				GridPanel.DrawStringInCell(bi,Color.BLACK,""+cs.getSolo());
			}

			return true;
		}
	}

	// if we ever get called upon to do a bigger one of these, we might want to consider
	// doing an Eratosthenes sieve or similar.
	private static void setupPrimes(int maxsize) {}
	private static boolean isPrime(int x) { return x == 2 || x == 3 || x == 5; }



    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }
        Board b = new Board(args[0]);
	    setupPrimes(b.maxnum);

		Solver s = new Solver(b);

		s.Solve(b);

		System.out.println("# of Solutions: " + s.GetSolutions().size());

		final Board fb = s.GetSolutions().get(0);
		StringBuffer sb = new StringBuffer();
		fb.forEachCell((x,y)-> {
			CellSet cs = fb.getCell(x,y);
			if (cs == null) return;
			if (!isPrime(cs.getSolo())) return;
			sb.append(LetterRotate.Rotate(fb.getLetter(x,y),cs.getSolo()));
		});



		String[] lines = new String[] { "AE #71",sb.toString(), fb.gfr.getVar("SOLUTION")};

		MyListener myl = new MyListener(b,lines);
		GridFrame gf = new GridFrame("Adalogical Aenigma #71 Solver", 1200,800,myl);
    }


}
