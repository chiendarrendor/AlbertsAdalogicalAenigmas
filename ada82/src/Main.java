import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import grid.puzzlebits.newpath.PathContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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


        private static final int DOTRADIUS=5;

        // draws a filled arc centered at x,y, starting at start degrees (3-ocolock is 0) and
        // going length degrees (positive is coutnerclockwise
        private static void filledSemi(Graphics2D g,int x,int y,int start,int length) {
            g.fillArc(x-DOTRADIUS,y-DOTRADIUS,2*DOTRADIUS,2*DOTRADIUS,start,length);
        }



        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi, Color.BLACK,""+b.getLetter(cx,cy));

            int cenx = bi.getWidth() / 2;
            int ceny = bi.getHeight() / 2;

            g.setColor(Color.BLACK);
            if (b.hasRawClue(cx,cy) && b.getRawClue(cx,cy) == '@') {
                filledSemi(g,cenx,ceny,0,360);
            }

            for (Direction d: Direction.orthogonals()) {
                if (b.edgeClues.getEdge(cx,cy,d)) {
                    switch(d) {
                        case NORTH: filledSemi(g,cenx,0,0,-180); break;
                        case SOUTH: filledSemi(g,cenx,bi.getHeight(),0,180); break;
                        case EAST: filledSemi(g,bi.getWidth(),ceny,90,180); break;
                        case WEST: filledSemi(g,0,ceny,90,-180); break;
                    }
                }
                switch (b.getEdge(cx,cy,d)) {
                    case UNKNOWN: break;
                    case PATH:
                        switch(d) {
                            case NORTH: g.drawLine(cenx,ceny,cenx,0); break;
                            case SOUTH: g.drawLine(cenx,ceny,cenx,bi.getHeight()); break;
                            case WEST: g.drawLine(cenx,ceny,0,ceny); break;
                            case EAST: g.drawLine(cenx,ceny,bi.getWidth(),ceny); break;
                        }
                        break;
                    case WALL: GridPanel.DrawStringInCorner(bi,Color.RED,"X",d); break;
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
        String[] lines = new String[] { "Adalogical","Aenigma" };

        Solver s = new Solver(b);

        //System.out.println("RAL: " + s.recursiveApplyLogic(b));

        s.Solve(b);

        System.out.println("# of solutions: " + s.GetSolutions().size());


        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);

            // calculate the number of vertical segments per column
            int[] colsegmentcount = new int[b.getWidth()];
            for (int x = 0 ; x < b.getWidth() ; ++x) {
                colsegmentcount[x] = 0;
                boolean insegment = false;
                for(int y = 0 ; y < b.getHeight() ; ++y) {
                    if (!insegment && b.getEdge(x,y,Direction.SOUTH) == EdgeState.PATH) {
                        insegment = true;
                        ++colsegmentcount[x];
                    } else if (insegment && b.getEdge(x,y,Direction.SOUTH) == EdgeState.WALL) {
                        insegment = false;
                    }
                }
            }
            Point startp = null;
            // find the topmost clue on the left column of the board
            for (int y = 0 ; y < b.getHeight() ; ++y) {
                if (b.getRawClue(0,y) == '@') {
                    startp = new Point(0,y);
                    break;
                }
                if (b.getRawClue(0,y) == 'S') {
                    startp = new Point(0,y);
                    break;
                }
            }
            if (startp == null) throw new RuntimeException("Couldn't find clue on left column");

            // calculate which next cell is the clockwise one
            Point clock = null;
            if (b.getEdge(startp.x,startp.y,Direction.NORTH) == EdgeState.PATH) clock = new Point(startp.x,startp.y-1);
            else if (b.getEdge(startp.x,startp.y,Direction.EAST) == EdgeState.PATH) clock = new Point(startp.x+1,startp.y);
            else throw new RuntimeException("How does a left-edge path cell not go north or east?");

            // get the (unique) path from the board and orient it correctly
            Path p = b.getPaths().iterator().next();
            Path.Cursor c = p.getCursor(startp.x,startp.y);
            if (!c.getNext().equals(clock)) {
                p.reverse();
                c = p.getCursor(startp.x,startp.y);
                if (!c.getNext().equals(clock)) throw new RuntimeException("How did reversing not get us the right next?");
            }
            // loop and find the right-turns to build the clue

            StringBuffer sb = new StringBuffer();

            for (int i = 0 ; i < p.size() ; ++i,c.next()) {
                if (Turns.makeTurn(c.getPrev(),c.get(),c.getNext()) != Turns.RIGHT) continue;
                if (!b.hasLetter(c.get().x,c.get().y)) continue;
                sb.append(LetterRotate.Rotate(b.getLetter(c.get().x,c.get().y),colsegmentcount[c.get().x]));
            }
            lines[0] = sb.toString();
            lines[1] = b.gfr.getVar("SOLUTION");

        }


        GridFrame gf = new GridFrame("Adalogical Aenigma #82 Solver",1200,800,new MyListener(b,lines));
    }


}
