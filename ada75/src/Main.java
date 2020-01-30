import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.GridPathCell;
import grid.puzzlebits.Path.Path;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

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
            Graphics2D g = (Graphics2D)bi.getGraphics();

            GridPathCell gpc = b.getPathContainer().getCell(cx,cy);
            Path p = null;
            if (gpc.getTerminalPaths().size() > 0) p = gpc.getTerminalPaths().get(0);
            if (gpc.getInternalPaths().size() > 0) p = gpc.getInternalPaths().get(0);
            int length = -1;
            boolean doubleterminated = false;
            boolean weterminate = false;
            if (p != null) {
                length = p.size();
                if (b.hasClue(p.endOne().x,p.endOne().y) && b.hasClue(p.endTwo().x,p.endTwo().y)) doubleterminated = true;
                if (p.endOne().x == cx && p.endOne().y == cy) weterminate = true;
                if (p.endTwo().x == cx && p.endTwo().y == cy) weterminate = true;
                if (b.hasClue(cx,cy)) weterminate = false;
            }

            Color fillcolor = Color.GREEN;
            if (p == null && b.hasClue(cx,cy) && b.getClue(cx,cy) > 1) fillcolor = Color.YELLOW;

            if (b.onPath(cx,cy)) {
                g.setColor(fillcolor);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.hasClue(cx,cy)) {
                GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+b.getClue(cx,cy));
            }

            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+cx,Direction.SOUTHWEST);
            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+cy,Direction.SOUTHEAST);


            Color pathcolor = Color.RED;
            if (doubleterminated) pathcolor = Color.BLUE;

            if (weterminate) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+length,Direction.NORTHEAST);






            for (Direction d : Direction.orthogonals()) {
                if (b.getEdge(cx,cy,d) == EdgeState.PATH) {
                    int cenx = bi.getWidth()/2;
                    int ceny = bi.getHeight()/2;
                    int ex = cenx;
                    int ey = ceny;
                    switch(d) {
                        case NORTH: ey = 0; break;
                        case SOUTH: ey = bi.getHeight(); break;
                        case EAST: ex = bi.getWidth(); break;
                        case WEST: ex = 0; break;
                    }
                    g.setStroke(new BasicStroke(5));
                    g.setColor(pathcolor);
                    g.drawLine(cenx,ceny,ex,ey);
                }
                if (b.getEdge(cx,cy,d) == EdgeState.WALL) GridPanel.DrawStringInCorner(bi,Color.RED,"X",d);
            }

            return true;
        }


    }


    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad command line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
	    Loader.load(b,"guesses.txt");


	    Solver s = new Solver(b);
//	    s.Solve(b);



        while(true) {
            FlattenLogicer.RecursionStatus rs = s.recursiveApplyLogic(b);
            System.out.println("RAL 1: " + rs);
            if (rs != FlattenLogicer.RecursionStatus.GO) break;

            LogicStatus ls = s.applyTupleSuccessors(b);
            System.out.println("ATS: " + ls);
            if (ls != LogicStatus.LOGICED) break;

            rs = s.recursiveApplyLogic(b);
            System.out.println("RAL 2: " + rs);
            if (rs != FlattenLogicer.RecursionStatus.GO) break;
        }

/*
        if (s.GetSolutions().size() != 1) {
            System.out.println("Invalid solution count");
            System.exit(1);
        }

        b = s.GetSolutions().get(0);
*/

	    String[] lines = new String[] { b.gfr.getVar("SOLUTION") };
	    MyListener myl = new MyListener(b,lines);

        GridFrame gf = new GridFrame("Adalogical Aenigma #75 solver",1600,950,myl);

    }


}
