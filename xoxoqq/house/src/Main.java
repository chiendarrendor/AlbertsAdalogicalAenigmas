import com.sun.org.apache.regexp.internal.RE;
import grid.graph.GridGraph;
import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

public class Main {

    private static class MyGridListener implements GridPanel.MultiGridListener {
        List<Board> bar;
        int curidx = 0;

        private Board b() { return bar.get(curidx); }

        @Override public boolean hasNext() { return curidx < bar.size() - 1; }
        @Override public boolean hasPrev() { return curidx > 0; }
        @Override public void moveToNext() { ++curidx; }
        @Override public void moveToPrev() { --curidx; }


        String[] lines;
        public MyGridListener(List<Board> bar, String[] lines) { this.bar = bar; this.lines = lines; }

        @Override public int getNumXCells() { return b().getWidth(); }
        @Override public int getNumYCells() { return b().getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            Color fillColor = null;
            if (!b().onBoard(cx,cy)) fillColor = Color.BLACK;
            else if (b().getCell(cx,cy) == CellType.BLACK) fillColor = Color.RED;
            else if (b().getCell(cx,cy) == CellType.WHITE) fillColor = Color.GREEN;

            if (fillColor != null) {
                g.setColor(fillColor);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (!b().onBoard(cx,cy)) return true;

           if (b().hasNumber(cx,cy)) GridPanel.DrawStringInCell(bi,Color.BLACK,""+b().getNumber(cx,cy));
           if (b().hasLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+b().getLetter(cx,cy));
           if (b().getCell(cx,cy) == CellType.WHITE) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b().getRegion(cx,cy), Direction.SOUTHWEST);

            return true;
        }
    }

    private static void deepLogic(Board b,Solver s) {
        while(true) {
            FlattenLogicer.RecursionStatus rs1 = s.recursiveApplyLogic(b);
            System.out.println("RAL1: " + rs1);
            if (rs1 != FlattenLogicer.RecursionStatus.GO) break;

            LogicStatus ats = s.applyTupleSuccessors(b);
            System.out.println("ATS: " + ats);
            if (ats != LogicStatus.LOGICED) break;

            FlattenLogicer.RecursionStatus rs2 = s.recursiveApplyLogic(b);
            System.out.println("RAL1: " + rs2);
            if (rs2 != FlattenLogicer.RecursionStatus.GO) break;
        }
    }



    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("bad command line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        Solver s = new Solver(b);
        s.Solve(b);
        //deepLogic(b,s);

        System.out.println("# of Solutions: " + s.GetSolutions().size());
        Board solution = s.GetSolutions().get(0);

        GridGraph whitesetgg = new GridGraph(new NoEmptyWhiteRegionLogicStep.WhiteGroupGridReference(solution));

        StringBuffer sb = new StringBuffer();
        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0 ; x < b.getWidth() ; ++x) {
                if (solution.getCell(x,y) != CellType.WHITE) continue;
                int size = whitesetgg.connectedSetOf(new Point(x,y)).size();
                sb.append(LetterRotate.Rotate(solution.getLetter(x,y),size));
            }
        }





        String[] lines = new String[] { sb.toString() , solution.gfr.getVar("SOLUTION")};
	    GridFrame gf = new GridFrame("xoxo qq House Solver",1200,800,new MyGridListener(s.GetSolutions(),lines));
    }


}
