import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String [] lines;

        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static final EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) {  return b.getRegionId(x,y) == b.getRegionId(x+1,y) ? PATH : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return b.getRegionId(x,y) == b.getRegionId(x,y+1) ? PATH : WALL; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            if (cy == 0 && b.hasColClue(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getColClue(cx,cy),Direction.NORTH);
            if (cy != 0 && b.hasColClue(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getColClue(cx,cy),Direction.SOUTH);
            if (cx == 0 && b.hasRowClue(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getRowClue(cx,cy),Direction.WEST);
            if (cx != 0 && b.hasRowClue(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getRowClue(cx,cy),Direction.EAST);

            StringBuffer sb = new StringBuffer();
            Cell c = b.getCell(cx,cy);
            if (c.canBePositive()) sb.append('+');
            if (c.canBeNegative()) sb.append('-');
            if (c.canBeBlank()) sb.append('#');
            GridPanel.DrawStringInCell(bi,Color.BLACK,sb.toString());

            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getSplay(cx,cy),Direction.SOUTHEAST);


            return true;
        }
    }

    public static void RunSolverManually(Solver s, Board b) {
        //s.debug();

        while(true) {
            FlattenLogicer.RecursionStatus rs = s.recursiveApplyLogic(b);
            System.out.println("RAL A: " + rs);
            if (rs != FlattenLogicer.RecursionStatus.GO) break;

            LogicStatus ls = s.applyTupleSuccessors(b);
            System.out.println("ATS: " + ls);
            if (ls != LogicStatus.LOGICED) break;

            FlattenLogicer.RecursionStatus rs2 = s.recursiveApplyLogic(b);
            System.out.println("RAL B: " + rs2);
            if (rs2 != FlattenLogicer.RecursionStatus.GO) break;

        }
    }

    public static int columnPlusCount(Board b,int x) {
        int count = 0;
        for (int y = 0 ; y < b.getHeight() ; ++y) {
            if (b.getCell(x,y).isPositive()) ++count;
        }
        return count;
    }




    public static void main(String[] args) {
	    if (args.length != 1) {
            System.out.println("Bad command line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
	    Solver s = new Solver(b);
        String[] lines = new String[] { "Adalogical","Aenigma"};

        s.Solve(b);

        //RunSolverManually(s,b);

        System.out.println("# of Solutions: " + s.GetSolutions().size());

        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);
            Board fb = b;
            StringBuffer sb = new StringBuffer();
            fb.forEachCell((x,y)->{
                if (fb.getCell(x,y).isPositive()) {
                    sb.append(LetterRotate.Rotate(fb.getLetter(x,y),columnPlusCount(fb,x)));
                }
            });
            lines[0] = sb.toString();
            lines[1] = b.gfr.getVar("SOLUTION");


        }



        MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #81",1200,800,  myl,myl);

    }
}
