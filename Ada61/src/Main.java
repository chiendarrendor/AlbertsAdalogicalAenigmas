import grid.lambda.CellLambda;
import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    static class MyGridListener implements GridPanel.GridListener {
        Board ob;
        GraphDistill gd;
        String[] lines;
        public MyGridListener(Board b,String[] lines) { this.ob = b; gd = new GraphDistill(b.rg); this.lines = lines; }

        public String getAnswerText() {
            StringBuffer sb = new StringBuffer();
            sb.append("<html><font size=\"5\">");
            Arrays.stream(lines).forEach(line->sb.append(line).append("<br>"));
            sb.append("</font></html>");
            return sb.toString();
        }

        public int getNumXCells() { return gd.width; }
        public int getNumYCells() { return gd.height; }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }
        public boolean drawCellContents(int cx, int cy, BufferedImage bi) {

            LetterContainer lc = gd.letters.getCell(cx,cy);

            GridPanel.DrawStringInCorner(bi,Color.RED,"" + ob.getRegionId(cx,cy),Direction.NORTH );

            if (lc != null) {
                char let = lc.letter;
                Direction d = lc.d;

                if (d == null) {
                    GridPanel.DrawStringInCell(bi,Color.BLACK,""+let);
                } else {
                    GridPanel.DrawStringInCorner(bi,Color.BLACK,""+let,d);
                }

            }


            return true;
        }
    }

    static class MyEdgeListener implements GridPanel.EdgeListener {
        Board ob;
        GraphDistill gd;
        public MyEdgeListener(Board b) { this.ob = b; gd = new GraphDistill(b.rg); }
        EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        EdgeDescriptor DOTS = new EdgeDescriptor(Color.BLACK,1);
        EdgeDescriptor OPEN = new EdgeDescriptor(Color.LIGHT_GRAY,0);

        public EdgeDescriptor onBoundary() { return WALL; }
        public EdgeDescriptor toEast(int x, int y) {
            switch(gd.edges.getEdge(x,y,Direction.EAST)) {
                case OPEN: return OPEN;
                case UNKNOWN: return DOTS;
                case CLOSED: return WALL;
                default: throw new RuntimeException("Unknown edge type!");
            }
        }
        public EdgeDescriptor toSouth(int x, int y) {
            switch (gd.edges.getEdge(x, y, Direction.SOUTH)) {
                case OPEN:
                    return OPEN;
                case UNKNOWN:
                    return DOTS;
                case CLOSED:
                    return WALL;
                default:
                    throw new RuntimeException("Unknown edge type!");
            }
        }
    }
    
    private static Point superCoord(int x,int y,Direction d) {
        int xbase = 2 * x;
        int ybase = 2 * y;
        int xdelta = 0;
        int ydelta = 0;

        if (d == null) { xdelta = 1; ydelta = 1; }
        else {
            switch (d) {
                case NORTHWEST:
                case WEST:
                case SOUTHWEST:
                    xdelta = 0;
                    break;
                case NORTH:
                case SOUTH:
                    xdelta = 1;
                    break;
                case NORTHEAST:
                case EAST:
                case SOUTHEAST:
                    xdelta = 2;
                    break;
            }
            switch (d) {
                case NORTHWEST:
                case NORTH:
                case NORTHEAST:
                    ydelta = 0;
                    break;
                case WEST:
                case EAST:
                    ydelta = 1;
                    break;
                case SOUTH:
                case SOUTHEAST:
                case SOUTHWEST:
                    ydelta = 2;
                    break;
            }
        }
        return new Point(xbase + xdelta, ybase + ydelta);
    }

    private static void incrementMPI(Map<Point,Integer> map,Point p) {
        map.put(p,(map.containsKey(p) ? map.get(p) : 0)+1);
    }

    // calculates the center superCoord of the set of edges, as long as there are no more than two edges
    // and they are adjacent.
    // also, we can assume, due to structure, that all edges listed are either easts or souths.
    private static Point superCoordEdgeCenter(Set<EdgeContainer.CellCoord> edges) {
        if (edges.size() > 2) throw new RuntimeException("Don't know how to calculate center of more than two edges");
        if (edges.size() == 0) throw new RuntimeException("Wat?");
        if (edges.size() == 1) {
            EdgeContainer.CellCoord cc = edges.iterator().next();
            return superCoord(cc.x,cc.y,cc.d);
        }
        // if we get here, we are exactly two.
        Map<Point,Integer> cornercounts = new HashMap<>();
        for (EdgeContainer.CellCoord cc : edges) {
            incrementMPI(cornercounts,superCoord(cc.x,cc.y,cc.d.clockwise()));
            incrementMPI(cornercounts,superCoord(cc.x,cc.y,cc.d.counterclockwise()));
        }
        Set<Point> duplicates = cornercounts.keySet().stream().filter(x->cornercounts.get(x) > 1).collect(Collectors.toSet());
        if (duplicates.size() == 0 || duplicates.size() > 1) throw new RuntimeException("can't be!");
        return duplicates.iterator().next();
    }

    private static char findIntermediateLetter(LetterContainer lc1,LetterContainer lc2) {
        LetterContainer first = lc1.compareTo(lc2) < 0 ? lc1 : lc2;
        LetterContainer second = lc1.compareTo(lc2) < 0 ? lc2 : lc1;
        char topletter = first.letter;
        char bottomletter = second.letter;

        int index = 0;

        while(true) {
            char topshifted = LetterRotate.Rotate(topletter,index);
            char bottomshifted = LetterRotate.Rotate(bottomletter,-index);
            if (topshifted == bottomshifted) return topshifted; // same letter twice should land on itself!
            if (topshifted == bottomletter) throw new RuntimeException("They passed each other!");

            ++index;
        }
    }


    
    
    public static void main(String[] args) {

	    if (args.length != 1) {
	        System.out.println("Bad command line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);

        Solver s = new Solver(b);



        s.Solve(b);
        System.out.println("# of Solutions: " + s.GetSolutions().size());

        b = s.GetSolutions().get(0);

        CellContainer<Character> supercontainer = new CellContainer<Character>(
                b.getWidth()*2+1,b.getHeight()*2 + 1,(x,y)->'.');

        for (Edge e : b.rg.edges) {
            if (e.getState() == EdgeState.CLOSED) continue;
            Point sec = superCoordEdgeCenter(e.edges);
            Region r1 = b.rg.regions.get(e.regionid1);
            Region r2 = b.rg.regions.get(e.regionid2);

            try {
                char theletter = findIntermediateLetter(r1.getLetterContainer(), r2.getLetterContainer());
                supercontainer.setCell(sec.x,sec.y,theletter);
            } catch(Exception ex) {
                System.out.println("Exception: " + ex + " caught");
                System.out.println("Region pair " + e.regionid1 + " + " + e.regionid2);
                break;
            }



        }

        StringBuffer sb = new StringBuffer();
        CellLambda.forEachCell(supercontainer.getWidth(),supercontainer.getHeight(),
                (x,y)->{
                    if (supercontainer.getCell(x,y) != '.') sb.append(supercontainer.getCell(x,y));
                });



        String[] solutions = new String[] { sb.toString(),b.gfr.getVar("SOLUTION") };


        GridFrame gf = new GridFrame("Ada 61 Solver",1200,800,
                new MyGridListener(b,solutions),new MyEdgeListener(b));
    }



}
