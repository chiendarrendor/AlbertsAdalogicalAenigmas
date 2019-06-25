import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import org.jgraph.graph.Edge;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {
    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;

        public MyListener(Board b,String[] lines) { this.b = b; this.lines = lines; }

        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.RED,5);
        private static EdgeDescriptor NOTWALL = new EdgeDescriptor(Color.BLUE,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return b.getRegionId(x,y) == b.getRegionId(x+1,y) ? NOTWALL : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return b.getRegionId(x,y) == b.getRegionId(x,y+1) ? NOTWALL : WALL; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true;  }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            CellType ct = b.getCellType(cx,cy);
            Color c = null;
            switch(ct) {
                case WHITE: c = Color.GREEN; break;
                case BLACK: c = Color.BLACK; break;
            }
            if (c != null) {
                g.setColor(c);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+b.getLetter(cx,cy));
            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getRegionId(cx,cy), Direction.SOUTHEAST);

            return true;
        }

    }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

	    Board b = new Board(args[0]);
	    Solver s = new Solver(b);

	    s.Solve(b);

	    System.out.println("# of solutions: " + s.GetSolutions().size());
	    final Board fb = s.GetSolutions().get(0);

	    int ellcount = 0;
        for (Region r : fb.regionsById.values()) {
            if (r.overlaps.get(0).omino.mirrorfamily == fb.ellOminoFamily) ++ellcount;
        }
        StringBuffer sb = new StringBuffer();
        final int fbel = ellcount;
        fb.forEachCell((x,y)-> {
            if (fb.getCellType(x,y) == CellType.BLACK) return;
            char regid = fb.getRegionId(x,y);
            Region r = fb.regionsById.get(regid);
            OminoSet.Omino theomino = r.overlaps.get(0).omino;
            if (theomino.mirrorfamily == fb.ellOminoFamily) sb.append(LetterRotate.Rotate(fb.getLetter(x,y),fbel));
        });






        String[] lines = new String[] { "Ell Count: " + ellcount,sb.toString(),fb.gfr.getVar("SOLUTION")};
        MyListener myl = new MyListener(fb,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #68 Solver",1200,800,myl,myl);
    }
}
