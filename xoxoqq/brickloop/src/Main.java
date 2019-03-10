import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.solverrecipes.genericloopyflatten.LineState;
import grid.solverrecipes.genericloopyflatten.LoopyBoard;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import grid.spring.ListMultiListener;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static class MyGridListener extends ListMultiListener<Board> implements GridPanel.EdgeListener {
        String[] lines;

        public MyGridListener(List<Board> bar,String[] lines) { super(bar); this.lines = lines; }

        @Override public int getNumXCells() { return b().getWidth(0) * 2 + 2; }
        @Override public int getNumYCells() { return b().getHeight() + 2; }
        @Override public boolean drawGridNumbers() { return false; }
        @Override public boolean drawGridLines() { return false; }
        @Override public boolean drawBoundary() { return false; }
        @Override public String[] getAnswerLines() { return lines; }

        // we are drawing the cells thus:
        // 1) every board cell is to be shown as a left and a right grid cell
        // 2) there is a 1-cell boundary around the board
        // 3) odd numbered rows are offset one grid cell, and contain one less board cell.
        // so, for a given grid cell x,y, we need to know the following:
        // 1) LEFT, RIGHT, or NOT
        // 2) board x, board y

        ;
        private class CellTuple {
            int x;
            int y;
            SubCellType bct;
            public CellTuple(int x,int y,SubCellType bct) { this.x = x; this.y = y; this.bct = bct; }
        }

        private CellTuple getTuple(int x,int y) {
            if (y == 0 || y == b().getHeight() + 1) return new CellTuple(-1,-1, SubCellType.NOT);
            if (x == 0 || x == b().getWidth(0) * 2 + 1) return new CellTuple(-1,-1, SubCellType.NOT);
            if (y%2 == 0 && (x == 1 || x == b().getWidth(0) * 2)) return new CellTuple(-1,-1, SubCellType.NOT);
            --y;
            --x;
            if (y%2 == 1) --x;
            int nx = x / 2;
            int off = x % 2;
            return new CellTuple(nx,y,off == 0 ? SubCellType.LEFT : SubCellType.RIGHT);
        }

        private void fillIO(BufferedImage bi,Graphics2D g,int gx,int gy,boolean showNum) {
            int iostat = b().getIORunWidth(gx,gy);
            if (iostat == Board.SIDE_UNKNOWN) return;
            Color c = Color.PINK;
            if (iostat >= 0) c = new Color(0x98,0xfb,0x98);
            g.setColor(c);
            g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            if (showNum) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+iostat,Direction.SOUTHWEST);
        }



        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            CellTuple ct = getTuple(cx,cy);

            switch(ct.bct) {
                case NOT:
                    g.setColor(Color.GRAY);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                    break;
                case LEFT:
                    fillIO(bi,g,ct.x,ct.y,false);
                    GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b().getLetter(ct.x,ct.y),Direction.EAST);
                    GridPanel.DrawStringInCorner(bi,Color.BLACK,b().getLineName(ct.x,ct.y, Direction.NORTHWEST),Direction.NORTHWEST);
                    GridPanel.DrawStringInCorner(bi,Color.BLACK,b().getLineName(ct.x,ct.y,Direction.WEST),Direction.WEST);
                    GridPanel.DrawStringInCorner(bi,Color.BLACK,b().getLineName(ct.x,ct.y,Direction.SOUTHWEST),Direction.SOUTHWEST);
                    break;
                case RIGHT:
                    fillIO(bi,g,ct.x,ct.y,true);
                    if (b().hasClue(ct.x,ct.y)) {
                        GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b().getClue(ct.x,ct.y),Direction.WEST);
                    }
                    GridPanel.DrawStringInCorner(bi,Color.BLACK,b().getLineName(ct.x,ct.y,Direction.NORTHEAST),Direction.NORTHEAST);
                    GridPanel.DrawStringInCorner(bi,Color.BLACK,b().getLineName(ct.x,ct.y,Direction.EAST),Direction.EAST);
                    GridPanel.DrawStringInCorner(bi,Color.BLACK,b().getLineName(ct.x,ct.y,Direction.SOUTHEAST),Direction.SOUTHEAST);


                    break;
            }

            return true;
        }



        private static final EdgeDescriptor UNKNOWN=new EdgeDescriptor(Color.BLUE,1);
        private static final EdgeDescriptor NOPATH = new EdgeDescriptor(Color.RED,5);
        private static final EdgeDescriptor PATH = new EdgeDescriptor(Color.GREEN,5);
        private static final EdgeDescriptor NOLINE = null;

        private static EdgeDescriptor lsed(LineState ls) {
            switch(ls) {
                case PATH: return PATH;
                case NOTPATH: return NOPATH;
                case UNKNOWN: return UNKNOWN;
                default: throw new RuntimeException("Not possible!");
            }
        }


        @Override public EdgeDescriptor onBoundary() {  return NOLINE;  }

        @Override public EdgeDescriptor toEast(int x, int y) {
            CellTuple ct = getTuple(x,y);
            if (ct.bct == SubCellType.LEFT) return NOLINE;
            if (ct.bct == SubCellType.RIGHT) return lsed(b().getLineState(ct.x,ct.y,Direction.EAST));
            CellTuple no = getTuple(x+1,y);
            if (no.bct == SubCellType.LEFT) return lsed(b().getLineState(no.x,no.y,Direction.WEST));
            return NOLINE;
        }

        @Override public EdgeDescriptor toSouth(int x, int y) {
            CellTuple ct = getTuple(x,y);
            if (ct.bct == SubCellType.LEFT) return lsed(b().getLineState(ct.x,ct.y,Direction.SOUTHWEST));
            if (ct.bct == SubCellType.RIGHT) return lsed(b().getLineState(ct.x,ct.y,Direction.SOUTHEAST));
            CellTuple no = getTuple(x,y+1);
            if (no.bct == SubCellType.LEFT) return lsed(b().getLineState(no.x,no.y,Direction.NORTHWEST));
            if (no.bct == SubCellType.RIGHT) return lsed(b().getLineState(no.x,no.y,Direction.NORTHEAST));
            return NOLINE;
        }


    }



    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }




        Board b = new Board(args[0]);
        List<Board> bar = new ArrayList<>();
        bar.add(b);
        b = new Board(b);
        bar.add(b);

        Solver s = new Solver(b);
        s.Solve(b);


        System.out.println("# of Solutions: " + s.GetSolutions().size());
        bar.addAll(s.GetSolutions());
        if (s.GetSolutions().size() == 0) {
            bar.add(b);
        }

        StringBuffer sb = new StringBuffer();
        if (s.GetSolutions().size() == 1) {
            Board sol = s.GetSolutions().get(0);
            sol.forEachCell((x,y)-> {
                if (!sol.onBoard(x,y)) return;
                int iostat = sol.getIORunWidth(x,y);
                if (iostat == Board.SIDE_OUTSIDE) return;
                sb.append(LetterRotate.Rotate(sol.getLetter(x,y),iostat));
            });
        }




        String[] lines = new String[] { sb.toString(), b.gfr.getVar("SOLUTION") };

        MyGridListener mgl = new MyGridListener(bar,lines);
        GridFrame gf = new GridFrame("XOXO qq BrickLoop Solver",1200,800,
                mgl,mgl);
    }
}
