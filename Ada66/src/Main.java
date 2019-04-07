import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Main {
    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }
        public String[] getAnswerLines() { return lines; }

        private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static final EdgeDescriptor NOTWALL = new EdgeDescriptor(Color.BLACK,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) {
            return b.getRegionId(x,y) == b.getRegionId(x+1,y) ? NOTWALL : WALL;
        }

        @Override public EdgeDescriptor toSouth(int x, int y) {
            return b.getRegionId(x,y) == b.getRegionId(x,y+1) ? NOTWALL : WALL;
        }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            char myrid = b.getRegionId(cx,cy);
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.getRegionStatus(myrid) == RegionStatus.DAY) {
                g.setColor(Color.YELLOW);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }
            if (b.getRegionStatus(myrid) == RegionStatus.NIGHT) {
                g.setColor(new Color(0x87ceeb));
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+b.getLetter(cx,cy));
            }

            for (Direction d : Direction.orthogonals()) {
                EdgeState es = b.getEdge(cx,cy,d);
                if (es == EdgeState.UNKNOWN) continue;
                if (es == EdgeState.WALL) {
                    GridPanel.DrawStringInCorner(bi,Color.RED,"X",d);
                    continue;
                }
                int cenx = bi.getWidth() / 2;
                int ceny = bi.getHeight() / 2;

                int ox = 0;
                int oy = 0;
                switch(d) {
                    case NORTH: ox = cenx; oy = 0; break;
                    case SOUTH: ox = cenx; oy = bi.getHeight(); break;
                    case EAST: ox = bi.getWidth() ; oy = ceny; break;
                    case WEST: ox = 0 ; oy = ceny; break;
                }
                g.setStroke(new BasicStroke(5));
                g.setColor(Color.GREEN);
                g.drawLine(cenx,ceny,ox,oy);



            }







            g.setFont(g.getFont().deriveFont(22.0f));
            g.setColor(Color.BLACK);

            if (b.isSun(cx,cy)) {
                GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getHeight(),"○");
            }

            if (b.isMoon(cx,cy)) {
                GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getHeight(),"☽");
            }






            return true;
        }

    }

    private static String followPath(Board b,Path p,Point start,boolean turns) {
        Path.Cursor curs = p.getCursor(start.x,start.y);
        StringBuffer sb = new StringBuffer();
        while(true) {
            Point cur = curs.get();
            Point nxt = curs.getNext();
            Point prev = curs.getPrev();

            if (b.hasLetter(cur.x, cur.y)) {
                Turns t = Turns.makeTurn(prev, cur, nxt);
                if (turns == Turns.isBend(t)) {
                    int delta = b.getRegionStatus(b.getRegionId(cur.x, cur.y)) == RegionStatus.DAY ? 1 : -1;
                    char rotl = LetterRotate.Rotate(b.getLetter(cur.x, cur.y), delta);
                    sb.append(rotl);
                }
            }

            curs.next();
            if (curs.get().equals(start)) break;
        }

        return sb.toString();
    }

    private static boolean isPathCell(Board b, int x,int y) {
        for (Direction d : Direction.orthogonals()) {
            if (b.getEdge(x,y,d) == EdgeState.PATH) return true;
        }
        return false;
    }




    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line" );
	        System.exit(1);
        }

        Board b = new Board(args[0]);

	    Solver s = new Solver(b);
        s.Solve(b);


//	    System.out.println("RAL1: " + s.recursiveApplyLogic(b));
//	    System.out.println("ATS: " + s.applyTupleSuccessors(b));
//	    System.out.println("RAL2: " + s.recursiveApplyLogic(b));


        b = s.GetSolutions().get(0);

        Path p = b.getPaths().iterator().next();
        Path.Cursor curs = p.getCursor(0,0);
        Point next = curs.getNext();
        if (!next.equals(new Point(1,0))) {
            p.reverse();
            curs = p.getCursor(0, 0);
        }

        String bends = followPath(b,p,new Point(0,0),true);
        p.reverse();
        String straights = followPath(b,p,new Point(0,0),false);





	    String[] lines = new String[] { bends, b.gfr.getVar("SOLUTION") , straights, b.gfr.getVar("EE1") };
	    MyListener myl = new MyListener(b,lines);
	    GridFrame gf = new GridFrame("Adalogical Aenigma #66 Solver",1200,800,myl,myl);
    }


}
