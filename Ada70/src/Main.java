import com.sun.org.apache.regexp.internal.RE;
import grid.letter.LetterRotate;
import grid.logic.ContainerRuntimeException;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

public class Main {

    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }

        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return b.getRegionId(x,y) == b.getRegionId(x+1,y) ? PATH : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return b.getRegionId(x,y) == b.getRegionId(x,y+1) ? PATH : WALL; }

        private static final float[] dash = { 2f,0f,2f };
        private static final BasicStroke dashstroke = new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,1.0f,dash,2f);
        private static final BasicStroke fatstroke = new BasicStroke(5.0f);
        private static final int INSET = 5;

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (b.isPossession(cx,cy) && b.hasLetter(cx,cy)) throw new RuntimeException("Possession/Letter");
            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            }

            if (b.isPossession(cx,cy) && b.getPossession(cx,cy) > 0) {
                GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getPossession(cx,cy));
            }

            boolean drawline = false;
            Point startp = null;
            Point endp = null;
            boolean drawcircle = false;

            switch(b.getCell(cx,cy).getPathType()) {
                case INITIAL: drawcircle = true; g.setColor(Color.BLACK); break;
                case INITIALEMPTY: drawcircle = true; g.setColor(Color.BLACK); g.setStroke(dashstroke); break;
                case EMPTY: break;
                case TERMINAL:
                    drawcircle = true;
                    g.setColor(Color.GREEN);
                    g.setStroke(fatstroke);
                    if (b.isPossession(cx,cy)) {
                        g.drawOval(INSET*2,INSET*2,bi.getWidth()-4*INSET,bi.getHeight()-4*INSET);
                    }
                    break;
                case HORIZONTAL:
                    drawline = true;
                    g.setColor(Color.GREEN); g.setStroke(fatstroke);
                    startp = new Point(0,bi.getHeight()/2); endp = new Point(bi.getWidth(),bi.getHeight()/2);
                    break;
                case VERTICAL:
                    drawline = true;
                    g.setColor(Color.GREEN); g.setStroke(fatstroke);
                    startp = new Point(bi.getWidth()/2,0); endp = new Point(bi.getWidth()/2,bi.getHeight());
                    break;
            }

            if (drawline) g.drawLine(startp.x,startp.y,endp.x,endp.y);
            if (drawcircle) g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);

            char ptype = '?';
            Color pcolor = Color.BLACK;
            switch(b.getCell(cx,cy).getPresenceType()) {
                case UNKNOWN: break;
                case FORBIDDEN: pcolor = Color.RED; ptype = 'X'; break;
                case REQUIRED: pcolor = Color.RED; ptype ='!'; break;
            }
            GridPanel.DrawStringInCorner(bi,pcolor,""+ptype,Direction.SOUTHWEST);

            GridPanel.DrawStringInCorner(bi,Color.BLACK,b.getRegionPair(cx,cy),Direction.SOUTHEAST);

            JumpSet js = b.getJumpSet(cx,cy);
            if (js != null) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+js.size(),Direction.NORTHEAST);
            }



            return true;
        }
    }

    private static void cycle(Board b) {
        Solver s = new Solver(b);
        while(true) {
            FlattenLogicer.RecursionStatus rs1 = s.recursiveApplyLogic(b);
            System.out.println("RAL 1: " + rs1);
            if (rs1 != FlattenLogicer.RecursionStatus.GO) break;

            LogicStatus ls = s.applyTupleSuccessors(b);
            System.out.println("ATS: " + ls);
            if (ls != LogicStatus.LOGICED) break;

            FlattenLogicer.RecursionStatus rs2 = s.recursiveApplyLogic(b);
            System.out.println("RAL 2: " + rs2);
            if (rs2 != FlattenLogicer.RecursionStatus.GO) break;
        }
    }

    public static void findAndSet(Board b,int sx,int sy, int ex,int ey) {
        JumpSet js = b.getJumpSet(sx,sy);
        if (js == null) throw new RuntimeException("Board has no JumpSet for " + sx + " "+ sy);
        if (js.size() == 1) throw new RuntimeException("Jumpset for " + sx + " " + sy + " already has been set");
        Point sp = null;
        if (sx != ex || sy != ey) sp = new Point(ex,ey);
        Jump j = null;
        for (Jump tj : js) {
            if (tj.terminal == null && sp == null) { j = tj; break; }
            if (sp != null && tj.terminal != null && sp.equals(tj.terminal)) { j = tj; break; }
        }
        if (j == null) throw new RuntimeException("No Jump found for " + sx + " " + sy);
        if (!j.isLegal(b)) throw new RuntimeException("Jump found for " + sx + " " + sy + " is illegal!");

        js.set(j);
        j.place(b);

    }



    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }
        Board b = new Board(args[0]);
        Solver s = new Solver(b);


        try {
            s.Solve(b);
        } catch(ContainerRuntimeException cre) {
            b = (Board)cre.getContained();
            System.out.println("Exception Thrown: " + cre);
        }

        System.out.println("# of Solutions: " + s.GetSolutions().size());
        final Board fb = s.GetSolutions().get(0);

        StringBuffer sb = new StringBuffer();

        fb.forEachCell((x,y)->{
            if (fb.getCell(x,y).getPathType() != PathType.TERMINAL) return;
            if (!fb.hasLetter(x,y)) return;

            JumpList jlist = fb.getBackReferences(x,y);
            List<Jump> targets = jlist.stillValidJumps(fb);
            if (targets.size() > 1) throw new RuntimeException("Multiple Valid Jumps to here?");
            if (targets.size() == 0) throw new RuntimeException("No valid jumps to here?");
            Jump j = targets.get(0);
            int length = j.intermediates.size() + 1;
            sb.append(LetterRotate.Rotate(fb.getLetter(x,y),length));

        });


        String[] lines = new String[] { sb.toString(), fb.gfr.getVar("SOLUTION")};
        MyListener myl = new MyListener(fb,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #70",1200,800,myl,myl);

    }


}
