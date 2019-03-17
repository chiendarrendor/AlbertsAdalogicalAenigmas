import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.solverrecipes.genericloopyflatten.LineState;
import grid.spring.ExpandedGridEdgeListener;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Main {
    private static final int INSET=5;
    private static class MyExpandedReference implements ExpandedGridEdgeListener {
        Board b;
        public MyExpandedReference(Board b) { this.b = b; }
        public int getBoardWidth() { return b.getWidth(); }
        public int getBoardHeight() { return b.getHeight(); }
        public String[] getAnswerLines() { return new String[0]; }

        public boolean drawBoardCellContents(int cx,int cy,BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            CellState cs = b.getCellState(cx,cy);
            Color c = null;
            switch (cs) {
                case UNKNOWN: break;
                case OUTSIDE: c = Color.PINK; break;
                case INSIDE: c = new Color(0x98,0xfb,0x98); break;
            }

            if (c != null) { g.setColor(c); g.fillRect(0,0,bi.getWidth(),bi.getHeight()); }

            if (b.hasClue(cx,cy)) {
                GridPanel.DrawStringInCell(bi,Color.BLACK,"" + b.getClue(cx,cy));
            }


            if (b.hasPod(cx,cy)) {
                g.setColor(Color.BLACK);
                g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getPodValue(cx,cy));
            }

            if (!b.isBigClueCell(cx,cy)) {
                dotClue(bi,cx,cy,Direction.NORTHEAST);
                dotClue(bi,cx,cy,Direction.NORTHWEST);
                dotClue(bi,cx,cy,Direction.SOUTHWEST);
                dotClue(bi,cx,cy,Direction.SOUTHEAST);
            }



            return true;
        }

        private void dotClue(BufferedImage bi, int cx, int cy, Direction d) {
            VertexClueColor vcc = b.getVertexClue(cx,cy,d);
            if (vcc == null) return;
            GridPanel.DrawStringInCorner(bi,Color.BLACK,(vcc==VertexClueColor.WHITE ? "◦" : "•"),d);
        }

        private static final EdgeDescriptor NONE = null;
        private static final EdgeDescriptor UNKNOWN = new EdgeDescriptor(Color.BLUE,1);
        private static final EdgeDescriptor PATH = new EdgeDescriptor(Color.GREEN,5);
        private static final EdgeDescriptor NOTPATH = new EdgeDescriptor(Color.RED,5);

        public EdgeDescriptor getBoardEdgeDescriptor(int x,int y,Direction d) {
            LineState ls = b.getLineState(x,y,d);
            if (ls == null) return NONE;
            switch(ls) {
                case PATH: return PATH;
                case NOTPATH: return NOTPATH;
                case UNKNOWN: return UNKNOWN;
                default: throw new RuntimeException("Unknown LineState");
            }
        }

    }





    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Illegal Command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        Solver s = new Solver(b);

        s.Solve(b);

        System.out.println("# of Solutions: " + s.GetSolutions().size());
        b = s.GetSolutions().get(0);


        MyExpandedReference myr = new MyExpandedReference(b);
        GridFrame gf = new GridFrame("Area 51 - 2019 Solver",1200,800,myr,myr);

    }


}
