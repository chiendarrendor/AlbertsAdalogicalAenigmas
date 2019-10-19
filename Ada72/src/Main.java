import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
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
        @Override public int getNumXCells() { return b.getWidth();  }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines;  }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringInCorner(bi, Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            }

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
                    g.setColor(Color.GREEN);
                    g.drawLine(cenx,ceny,ex,ey);



                    GridPanel.DrawStringInCorner(bi,Color.GREEN,"o",d);
                }
                if (b.getEdge(cx,cy,d) == EdgeState.WALL) GridPanel.DrawStringInCorner(bi,Color.RED,"X",d);
            }

            if (b.hasClue(cx,cy)) {

                g.setStroke(new BasicStroke(5));
                g.setColor(Color.BLACK);
                int INSET = 8;
                g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                int clue = b.getClue(cx,cy);
                if (clue > 0) GridPanel.DrawStringInCell(bi,Color.BLACK,""+clue);
            }

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

/*
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
        String sb = "test";
*/

	    s.Solve(b);
	    System.out.println("# of Solutions: " + s.GetSolutions().size());
        b = s.GetSolutions().get(0);

	    Board fb = b;

	    StringBuffer sb = new StringBuffer();
	    fb.forEachCell((x,y)->{
	        if(fb.hasClue(x,y)) return;
	        char c = fb.getLetter(x,y);
	        Path p = fb.getPathAt(x,y);
	        PathInfo pi = new PathInfo(fb,p);
	        Path.Cursor pc = p.getCursor(x,y);
	        if (Turns.makeTurn(pc.getPrev(),pc.get(),pc.getNext()) == Turns.STRAIGHT) {
	            sb.append(LetterRotate.Rotate(c,pi.bendcount));
            }
        });



	    String[] lines = new String[] { sb.toString(),fb.gfr.getVar("SOLUTION")};

	    MyListener myl = new MyListener(fb,lines);
	    GridFrame gf = new GridFrame("Adalogical Aenigma #72 Solver",1200,800,myl);
    }


}
