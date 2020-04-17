import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;

        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() {  return b.getWidth();  }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        private boolean regionMatches(int x,int y,Direction d) { return b.getRegionId(x,y) == b.getRegionId(d.delta(x,y,1)); };
        @Override public EdgeDescriptor toEast(int x, int y) { return regionMatches(x,y,Direction.EAST) ? PATH : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return regionMatches(x,y,Direction.SOUTH) ? PATH : WALL; }


        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.getCell(cx,cy) == CellState.BARRIER) {
                g.setColor(Color.YELLOW);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                return true;
            }

            if (b.getCell(cx,cy) == CellState.LABEL) {
                g.setColor(Color.GREEN);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            char rid = b.getRegionId(cx,cy);
            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy),Direction.NORTHEAST);
            }

            g.setColor(Color.BLACK);
            Board.CountHolder ch = b.getRegionCounts(rid);
            int rlabel = b.getRegionLabel(rid);

            if (b.hasLabel(cx,cy)) {
                g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 2.0f));
                GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getHeight(),""+b.getLabel(cx,cy));
            } else if (rlabel != -1) {
                g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 1.5f));
                GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getHeight(),"" + rlabel);
            } else if (ch.unknowns.size() == 0) {
                GridPanel.DrawStringInCell(bi,Color.BLACK,""+ch.labelcount);
            }

            GridPanel.DrawStringInCorner(bi,Color.BLACK,"L:" + ch.labelcount,Direction.SOUTHWEST);
            GridPanel.DrawStringInCorner(bi,Color.BLACK,"B:" + ch.barriercount,Direction.SOUTHEAST);
            GridPanel.DrawStringInCorner(bi,Color.BLACK,"U:" + ch.unknowns.size(),Direction.SOUTH);






            return true;
        }


    }
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad command line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        String[] lines = new String[] { "Adalogical Aenigma", "#78 Solver"};

        // Solver goes here
        Solver s = new Solver(b);

        s.Solve(b);
/*
        while(true) {
            FlattenLogicer.RecursionStatus rs1 = s.recursiveApplyLogic(b);
            System.out.println("RAL 1: " + rs1);
            if (rs1 != FlattenLogicer.RecursionStatus.GO) break;

            LogicStatus ls = s.applyTupleSuccessors(b);
            System.out.println("ATS: " + ls);
            if (ls != LogicStatus.LOGICED) break;

            FlattenLogicer.RecursionStatus rs2 = s.recursiveApplyLogic(b);
            System.out.println("RAL 1: " + rs2);
            if (rs2 != FlattenLogicer.RecursionStatus.GO) break;
        }
*/


        // Cluesolver goes here
        if (s.GetSolutions().size() == 1) {
            Board fb = s.GetSolutions().get(0);
            StringBuffer sb = new StringBuffer();
            fb.forEachCell((x,y)->{
                if (!fb.hasLetter(x,y)) return;
                if (fb.getCell(x,y) != CellState.LABEL) return;
                if (fb.liveLabelCount(fb.getRegionId(x,y)) % 2 == 0) return;
                sb.append(LetterRotate.Rotate(fb.getLetter(x,y),fb.liveLabelCount(fb.getRegionId(x,y))));
            });
            lines[0] = sb.toString();
            lines[1] = fb.gfr.getVar("SOLUTION");
            b = fb;
        }

        MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #78 Solver", 1600,800,myl,myl);

    }


}
