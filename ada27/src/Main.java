import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.puzzlebits.newpath.PathContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyListener implements GridPanel.GridListener {
        Board b;
        String[] lines;
        PathDecomposer pdecon;
        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; pdecon = new PathDecomposer(b.getPaths()); }

        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static Color DARK_GREEN = new Color(2,75,48);


        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            if (b.hasIce(cx,cy)) {
                g.setColor(Color.CYAN);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }


            for (Direction d: Direction.orthogonals()) {
                if (b.getEdge(cx,cy,d) == EdgeState.WALL) GridPanel.DrawStringInCorner(bi,Color.RED,"X",d);
                if (b.getEdge(cx,cy,d) == EdgeState.PATH) GridPanel.DrawStringInCorner(bi,DARK_GREEN,"□",d);

                drawPathSegment(d,pdecon.getDecomposition(cx,cy,d),bi,g);
            }

            if (b.getEntrance().x == cx && b.getEntrance().y == cy) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,"IN",Direction.NORTHEAST);
            }

            if (b.getExit().x == cx && b.getExit().y == cy) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,"OUT",Direction.NORTHEAST);
            }

            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            }

            return true;
        }

        private static int ARROWSIZE = 5;

        private void drawPathSegment(Direction d, DrawDirection decomposition,BufferedImage bi,Graphics2D g) {
            g.setColor(Color.BLACK);
            if (decomposition == DrawDirection.NONE) return;
            int centerx = bi.getWidth()/2;
            int centery = bi.getHeight()/2;

            int edgex = 0;
            int edgey = 0;
            int ahx = 0;
            int ahy = 0;

            switch(d) {
                case NORTH:
                    edgex = centerx;
                    edgey = 0;
                    ahx = centerx;
                    ahy = bi.getHeight() / 4;
                    break;
                case SOUTH:
                    edgex = centerx;
                    edgey = bi.getHeight();
                    ahx = centerx;
                    ahy = (bi.getHeight() / 4) * 3;
                    break;
                case WEST:
                    edgex = 0;
                    edgey = centery;
                    ahy = centery;
                    ahx = bi.getWidth() / 4;
                    break;
                case EAST:
                    edgex = bi.getWidth();
                    edgey = centery;
                    ahy = centery;
                    ahx = (bi.getWidth() / 4) * 3;
                    break;
            }

            g.drawLine(centerx,centery,edgex,edgey);
            if (decomposition == DrawDirection.REVERSIBLE) return;

            int ulx = ahx - ARROWSIZE;
            int uly = ahy - ARROWSIZE;
            int lrx = ahx + ARROWSIZE;
            int lry = ahy + ARROWSIZE;


            if (decomposition == DrawDirection.INBOUND) {
                switch(d) {
                    case NORTH:
                        g.drawLine(ahx,ahy,ulx,uly);
                        g.drawLine(ahx,ahy,lrx,uly);
                        break;
                    case SOUTH:
                        g.drawLine(ahx,ahy,ulx,lry);
                        g.drawLine(ahx,ahy,lrx,lry);
                        break;
                    case EAST:
                        g.drawLine(ahx,ahy,lrx,uly);
                        g.drawLine(ahx,ahy,lrx,lry);
                        break;
                    case WEST:
                        g.drawLine(ahx,ahy,ulx,uly);
                        g.drawLine(ahx,ahy,ulx,lry);
                        break;
                }
            } else {
                switch(d) {
                    case SOUTH:
                        g.drawLine(ahx,ahy,ulx,uly);
                        g.drawLine(ahx,ahy,lrx,uly);
                        break;
                    case NORTH:
                        g.drawLine(ahx,ahy,ulx,lry);
                        g.drawLine(ahx,ahy,lrx,lry);
                        break;
                    case WEST:
                        g.drawLine(ahx,ahy,lrx,uly);
                        g.drawLine(ahx,ahy,lrx,lry);
                        break;
                    case EAST:
                        g.drawLine(ahx,ahy,ulx,uly);
                        g.drawLine(ahx,ahy,ulx,lry);
                        break;
                }
            }





        }


    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        Solver s = new Solver(b);

        //System.out.println("RAL 1: " + s.recursiveApplyLogic(b));
        //System.out.println("ATS 1: " + s.applyTupleSuccessors(b));
        //System.out.println("RAL 2: " + s.recursiveApplyLogic(b));

        s.Solve(b);
        String[] lines = new String[]{"Adalogical", "Aenigma #27", "","","",""};

        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);
            StringBuffer sb = new StringBuffer();
            PathContainer.Path path = b.getPaths().getPaths().get(0);

            if (b.gfr.getVar("CLUETYPE").equals("ADA27")) {
                int counter = 0;
                for (Point p : path.getCells()) {
                    if (!b.hasIce(p.x,p.y)) {
                        ++counter;
                        if (counter % 5 == 0) sb.append(b.getLetter(p.x,p.y));
                    }
                }
            } else if (b.gfr.getVar("CLUETYPE").equals("ADDENDA21")) {
                int counter = 0;
                for (Point p : path.getCells()) {
                    if (b.hasLetter(p.x,p.y)) {
                        ++counter;
                        if (counter %3 == 0) sb.append(b.getLetter(p.x,p.y));
                    }
                }
            }


            lines[0] = sb.toString();
            lines[1] = b.gfr.getVar("SOLUTION");

            if (b.gfr.hasVar("RECLUETYPE")) {
                StringBuffer rsb = new StringBuffer();
                if (b.gfr.getVar("RECLUETYPE").equals("NONPATHPLUSTWO") || b.gfr.getVar("RECLUETYPE").equals("NONPATH")) {
                    int offset = b.gfr.getVar("RECLUETYPE").equals("NONPATHPLUSTWO") ? 2 : 0;
                    Board fb = b;
                    b.forEachCell((x,y)-> {
                        if (!fb.hasLetter(x,y)) return;
                        if(path.getCells().contains(new Point(x,y))) return;
                        rsb.append(LetterRotate.Rotate(fb.getLetter(x,y),offset));
                    });
                }
                lines[2] = rsb.toString();
                lines[3] = b.gfr.getVar("SOLUTION2");
            }

            if (b.gfr.hasVar("RERECLUETYPE") && b.gfr.getVar("RERECLUETYPE").equals("REVERSEBY3")) {
                StringBuffer rrsb = new StringBuffer();
                int counter = 0;
                for (int i = path.getCells().size() - 1 ; i >= 0 ; --i) {
                    Point p = path.getCells().get(i);
                    if (b.hasLetter(p.x,p.y)) {
                        ++counter;
                        if (counter % 3 == 0) rrsb.append(b.getLetter(p.x,p.y));
                    }
                }
                lines[4] = rrsb.toString();
                lines[5] = b.gfr.getVar("SOLUTION3");
            }



        }





        MyListener myl = new MyListener(b, lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #27 Solver", 1200, 800, myl);
    }



}
