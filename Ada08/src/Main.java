import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenDeep;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	private static class MyGridListener implements GridPanel.GridListener {
		Board b;
		String[] lines;

		public MyGridListener(Board b,String[] lines) { this.b = b; this.lines = lines; }
		public int getNumXCells() { return b.getWidth(); }
		public int getNumYCells() { return b.getHeight(); }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public String getAnswerText() {
			StringBuffer sb = new StringBuffer();
			sb.append("<html><font size=\"5\">");
			Arrays.stream(lines).forEach(line->sb.append(line).append("<br>"));
			sb.append("</font></html>");
			return sb.toString();
		}

		public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
			Graphics2D g = (Graphics2D)bi.getGraphics();

			switch(b.getCell(cx,cy)) {
                case ARROW:
                    ArrowInfo ai = b.getArrowInfo(cx,cy);
                    g.setColor(Color.YELLOW);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                    GridPanel.DrawStringInCell(bi,Color.BLACK,""+ai.size + ai.d.getSymbol());
                    break;
                case PATH:
                    g.setColor(Color.GREEN);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                    break;
                case UNKNOWN:
                    break;
                case WALL:
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                    break;
            }

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+b.getLetter(cx,cy));

			int wallcount = 0;

			for (Direction d : Direction.orthogonals()) {
                switch (b.getEdge(cx, cy, d)) {
                    case WALL:
                        ++wallcount;
                        GridPanel.DrawStringInCorner(bi, Color.RED, "X", d);
                        break;
                    case PATH:
                        GridPanel.DrawStringInCorner(bi, Color.BLACK, "" + d.getSymbol(), d);
                }
            }

            /*
            if (b.getCell(cx,cy) == CellState.PATH || b.getCell(cx,cy) == CellState.UNKNOWN) {
			    if (wallcount == 2) {
			        g.setColor(Color.BLACK);
			        g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                }
            }
            */






			return true;
		}
	}



	public static void main(String[] args) {
	    if (args.length != 1)  throw new RuntimeException("Bad Command Line");

	    Board b = new Board(args[0]);
	    Solver s = new Solver(b);

	    s.Solve(b);
	    System.out.println("# of Solutions: " + s.GetSolutions().size());

	    b = s.GetSolutions().get(0);

        Path fpath = b.getPaths().iterator().next();
        Point sp = new Point(0,11);
        Path.Cursor fpc = fpath.getCursor(sp.x,sp.y);
        fpc.next();
        if (fpc.get().x != sp.x || fpc.get().y != sp.y-1) {
            fpath.reverse();
            fpc = fpath.getCursor(sp.x,sp.y);
        } else {
            fpc.prev();
        }

        StringBuffer rsb = new StringBuffer();
        StringBuffer lsb = new StringBuffer();
        StringBuffer ssb = new StringBuffer();
        while(!fpc.getNext().equals(sp)) {
            switch(Turns.makeTurn(fpc.getPrev(),fpc.get(),fpc.getNext())) {
                case LEFT: lsb.append(b.getLetter(fpc.get().x,fpc.get().y)); break;
                case RIGHT: rsb.append(b.getLetter(fpc.get().x,fpc.get().y)); break;
                case STRAIGHT:  ssb.append(b.getLetter(fpc.get().x,fpc.get().y)); break;
            }
            fpc.next();
        }

        StringBuffer walls = new StringBuffer();
        final Board fb = b;
        b.forEachCell((x,y)->{
            if (fb.getCell(x,y) == CellState.WALL) walls.append(fb.getLetter(x,y));
        });




		String[] results = {rsb.toString(), b.getSolution(1), lsb.reverse().toString(),b.getSolution(2) };

        GridFrame gf = new GridFrame("Adalogical Aenigma #8 solver",1000,800,new MyGridListener(b,results));

    }
}
