import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static class MyListener implements GridPanel.MultiGridListener {
        List<Board> bar;
        String[] lines;
        int curidx = 0;

        public MyListener(List<Board> bar,String[] lines) { this.bar = bar; this.lines = lines; }

        @Override public boolean hasNext() { return curidx < bar.size() - 1; }
        @Override public void moveToNext() { ++curidx; }
        @Override public boolean hasPrev() { return curidx > 0; }
        @Override public void moveToPrev() { --curidx; }

        private Board b() { return bar.get(curidx); }

        @Override public int getNumXCells() { return b().getWidth(); }
        @Override public int getNumYCells() { return b().getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b().isSpecial(cx, cy)) {
                g.setColor(Color.PINK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            GridPanel.DrawStringInCell(bi,Color.BLACK,b().getCellSet(cx,cy).toString());

            if (b().hasInequality(cx,cy)) {
                Direction d = b().getInequalityDirection(cx,cy);
                char c = b().getInequalitySymbol(cx,cy);
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+c,d);
            }

            if (b().hasDiff(cx,cy)) {
                Direction d = b().getDiffDirection(cx,cy);
                int c = b().getDiffSize(cx,cy);
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+c,d);
            }
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
            System.out.println("Bad command line" );
            System.exit(1);
        }
        Board b = new Board(args[0]);
        Solver s = new Solver(b);
        List<Board> bar = new ArrayList<>();

        bar.add(b);
        b = new Board(b);

        //s.debug();
        s.Solve(b);



        System.out.println("Number of solutions: " + s.GetSolutions().size());
        bar.addAll(s.GetSolutions());

        Board solution = s.GetSolutions().get(0);

        StringBuffer sb = new StringBuffer();
        for (int x = 0 ; x < solution.getWidth() ; ++x ) {
            int rot = 0;
            for (int y = 0 ; y < solution.getHeight() ; ++y) {
                if (!solution.isSpecial(x,y)) continue;
                rot += solution.getCellSet(x,y).theNumber();
            }
            if (rot > 0) sb.append(LetterRotate.Rotate('Z',rot));
        }

        String[] lines = new String[] { sb.toString(), b.gfr.getVar("SOLUTION") };

        MyListener mylistener = new MyListener(bar,lines);
        GridFrame gf = new GridFrame("XOXO qq Chalk Solver",1200,800,mylistener);
    }
}
