import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.PointAdjacency;
import grid.puzzlebits.Turns;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import sun.rmi.runtime.Log;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyListener implements GridPanel.GridListener {
        Board b;
        String[] lines;

        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }

        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() {  return true; }
        public String[] getAnswerLines() { return lines; }

        private void drawLetter(BufferedImage bi,Gate g,Direction opdir, Direction drawdir) {
            char c = '?';
            if (g.hasLetter(opdir)) c = g.getLetter(opdir);
            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+c,drawdir);
        }


        public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            if (b.isBlock(cx,cy)) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());

                GateManager.GatePointer gp = b.getGatePointer(cx,cy);
                if (gp != null) {
                    for (Direction d: Direction.orthogonals()) {
                        Gate gate = gp.terminals.get(d);

                        if (gate != null && gate.isNumbered()) GridPanel.DrawStringInCorner(bi,Color.RED,""+gate.getNumber(),d);
                    }
                }
                return true;
            }

            Point sp = b.getStartCell();
            if (cx == sp.x && cy == sp.y) {
                g.setColor(Color.BLACK);
                int INSET = 5;
                g.drawOval(INSET,INSET,bi.getWidth() - 2 * INSET,bi.getHeight() - 2*INSET);
                GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getGateCount());
                return true;
            }


            GateManager.GatePointer gp = b.getGatePointer(cx,cy);

            // TODO: use b.getClueType() to determine how to specify clues on the board
            if (b.getClueType() == ClueType.CELLCLUES) {
                if (b.isLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy),Direction.NORTHWEST);
            } else if (gp != null && gp.g != null) {
                if (gp.g.getOrientation() == '|') {
                    drawLetter(bi,gp.g,Direction.WEST,Direction.NORTHWEST);
                    drawLetter(bi,gp.g,Direction.EAST,Direction.NORTHEAST);
                } else {
                    drawLetter(bi,gp.g,Direction.NORTH,Direction.NORTHWEST);
                    drawLetter(bi,gp.g,Direction.SOUTH,Direction.SOUTHWEST);
                }
            }
            // idea:
            //   for cell clue type, if cell is not a gate, put letter in NW
            //   for gate clue type:
            //      if cell is - clue, put < in NW and > in NE
            //      if cell is | clue, put ^ in NW and v in SW


            if (gp != null) {
                if (gp.g == null) {GridPanel.DrawStringInCell(bi,Color.BLACK,"?"); return true; }
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+gp.g.getId(),Direction.SOUTHEAST);
                g.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,1.0f,new float[]{2f,0f,2f},2f));
                g.setColor(Color.BLACK);
                if (gp.g.getOrientation() == '-') {
                    g.drawLine(0,bi.getHeight()/2,bi.getWidth(),bi.getHeight()/2);
                } else {
                    g.drawLine(bi.getWidth()/2,0,bi.getWidth()/2,bi.getHeight());
                }
            }

            for (Direction d : Direction.orthogonals()) {
                switch(b.getEdge(cx,cy,d)) {
                    case UNKNOWN: break;
                    case WALL: GridPanel.DrawStringInCorner(bi,Color.RED,"X",d); break;
                    case PATH:
                        int ex = -1;
                        int ey = -1;
                        int cenx = bi.getWidth() / 2;
                        int ceny = bi.getHeight() / 2;
                        switch(d) {
                            case NORTH: ex = cenx; ey = 0; break;
                            case SOUTH: ex = cenx; ey = bi.getHeight(); break;
                            case WEST: ex = 0; ey = ceny; break;
                            case EAST: ex = bi.getWidth(); ey = ceny; break;
                        }
                        g.setColor(Color.GREEN);
                        g.setStroke(new BasicStroke(5.0f));
                        g.drawLine(cenx,ceny,ex,ey);
                }
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
        StringBuffer solutionbuffer = new StringBuffer();


	    s.Solve(b);
	    System.out.println("# of Solutions: " + s.GetSolutions().size());

	    final Board fb = s.GetSolutions().get(0);

	    fb.forEachCell((x,y)-> {
	        int count = 0;
	        if (fb.getEdge(x,y,Direction.EAST) == EdgeState.PATH) { System.out.print("E"); ++count; }
	        if (fb.getEdge(x,y,Direction.SOUTH) == EdgeState.PATH) { System.out.print("S"); ++count; }
	        if (count == 0) { System.out.print(".") ; ++count; }
	        for(; count < 3 ; ++count) System.out.print(" ");
	        if (x == fb.getWidth() - 1) System.out.println("");
        });

        // take the masterpath, and orient it so that it goes through the gates in increasing
        // order as you go next to next.
        Path masterpath = fb.getPaths().iterator().next();
        Path.Cursor c = masterpath.getCursor(fb.getStartCell().x,fb.getStartCell().y);
        int prevgate = -1;
        for (;; c.next() ) {
            Point cp = c.get();
            if (!fb.isGate(cp.x,cp.y)) continue;
            Gate g = fb.getGate(cp.x,cp.y);
            if (!g.isNumbered()) continue;
            if (prevgate == -1) {
                prevgate = g.getNumber();
            } else {
                if (prevgate > g.getNumber()) masterpath.reverse();
                break;
            }
        }

        if (fb.getClueType() == ClueType.GATECLUES) {
            Path.Cursor nc = masterpath.getCursor(fb.getStartCell().x,fb.getStartCell().y);
            int gatecount = 0;
            for (;; nc.next() ) {
                Point curp = nc.get();
                Point nextp = nc.getNext();
                if (nextp.equals(fb.getStartCell())) break;
                if (!fb.isGate(curp.x,curp.y)) continue;
                Gate g = fb.getGate(curp.x,curp.y);
                ++gatecount;
                Direction d = PointAdjacency.adjacentDirection(curp,nextp);
                solutionbuffer.append(LetterRotate.Rotate(g.getLetter(d),gatecount));
            }
        } else if (fb.getClueType() == ClueType.CELLCLUES) {
            Path.Cursor nc = masterpath.getCursor(fb.getStartCell().x,fb.getStartCell().y);
            nc.next();
            int gatecount = 0;
            for (;; nc.next() ) {
                Point curp = nc.get();
                Point prevp = nc.getPrev();
                Point nextp = nc.getNext();
                if (nextp.equals(fb.getStartCell())) break;
                if (fb.isGate(curp.x,curp.y)) {
                    ++gatecount;
                    continue;
                }
                if (Turns.makeTurn(prevp,curp,nextp) == Turns.RIGHT) {
                    solutionbuffer.append(LetterRotate.Rotate(fb.getLetter(curp.x,curp.y),
                            gatecount % 2 == 0 ? 2 : 1));
                }
            }
        }



	    String[] lines = new String[] { solutionbuffer.toString(), fb.gfr.getVar("SOLUTION") };

	    MyListener myl = new MyListener(fb,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #5 Solver",1200,800,myl);
    }


}
