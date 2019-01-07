import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import javax.xml.bind.Unmarshaller;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

public class Main {

    private static class MyGridListener implements GridPanel.GridListener {
        private Board b;
        private String[] lines;
        public MyGridListener(Board b,String[] lines) { this.b = b; this.lines = lines; }

        @Override public int getNumXCells() { return b.getWidth() * b.getBoardCount(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            int bid = cx / b.getWidth();
            int tcx = cx % b.getWidth();
            SubBoard sb = b.getSubBoard(bid);
            CellState cs = sb.getCell(tcx,cy);
            Graphics2D g = (Graphics2D)bi.getGraphics();

            switch(cs) {
                case UNKNOWN: g.setColor(Color.DARK_GRAY); break;
                case WHITE: g.setColor(Color.WHITE); break;
                case BLACK: g.setColor(Color.BLACK); break;
            }

            g.fillRect(0,0,bi.getWidth(),bi.getHeight());


            if (sb.hasLetter(tcx,cy)) {
                GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+sb.getLetter(tcx,cy));
            }

            if (cy == 0 && sb.getVClue(tcx) != -1) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+sb.getVClue(tcx), Direction.NORTH);
            }

            if (tcx == 0 && sb.getHClue(cy) != -1) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+sb.getHClue(cy),Direction.WEST);
            }

            return true;
        }
    }

    private static class MyEdgeListener implements GridPanel.EdgeListener {
        private Board b;
        public MyEdgeListener(Board b) { this.b = b; }

        private EdgeDescriptor boundED = new EdgeDescriptor(Color.BLACK,5);
        private EdgeDescriptor innerED = new EdgeDescriptor(Color.BLACK,1);


        @Override public EdgeDescriptor onBoundary() { return boundED; }

        @Override public EdgeDescriptor toEast(int x, int y) {
            return (x % b.getWidth() == b.getWidth() - 1) ? boundED : innerED;
        }

        @Override public EdgeDescriptor toSouth(int x, int y) { return innerED; }
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

        b = s.GetSolutions().get(0);
        String line1 = "Empty";
        String line2 = "Also Empty";
        if (b.getBoardCount() == 2) {
            StringBuffer sbuf = new StringBuffer();
            for (int y = 0; y < b.getHeight(); ++y) {
                for (int mx = 0; mx < b.getWidth() * b.getBoardCount(); ++mx) {
                    int bid = mx / b.getWidth();
                    int x = mx % b.getWidth();
                    SubBoard sb = b.getSubBoard(bid);
                    SubBoard ob = b.getSubBoard(bid == 0 ? 1 : 0); // assumption being made here that there are exactly 2 boards

                    if (sb.getCell(x, y) == CellState.BLACK) continue;
                    if (ob.getCell(x, y) != CellState.BLACK) continue;
                    sbuf.append(sb.getLetter(x, y));
                }
            }
            line1 = sbuf.toString();
            line2 = b.gfr.getVar("SOLUTION");
        }

        String[] lines = new String[] { line1, line2 };

        GridFrame gf = new GridFrame("Adalogical Aenigma #63 Solver",1200,800,
                new MyGridListener(b,lines),new MyEdgeListener(b));
    }
}
