import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Turns;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Vector;

public class Ada02
{
    private static class MyListener implements GridPanel.GridListener
    {
        Board b;
        PathAnalyzer pa;
        public MyListener(Board b) { this.b = b; pa = new PathAnalyzer(b); }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi, Color.GRAY,""+b.getLetter(cx,cy));
            else
            {
                char regionid = b.getRegionId(cx,cy);
                if (b.getRegionExpectedCellCount(regionid) > 0)
                {
                    GridPanel.DrawStringUpperLeftCell(bi,Color.black,""+b.getRegionExpectedCellCount(regionid));
                }
            }

            PathAnalyzer.CellInfo ci = pa.getCellInfo(cx,cy);
            int asize = 20;
            int cenx = bi.getWidth()/2;
            int ceny = bi.getHeight()/2;
            int ulx = cenx - asize/2;
            int uly = ceny - asize/2;

            if (ci.noPath)
            {
                g.setColor(Color.RED);
                g.drawRect(ulx,uly,asize,asize);
                g.drawLine(ulx,uly,ulx+asize,uly+asize);
                g.drawLine(ulx+asize,uly,ulx,uly+asize);
            }

            if (ci.unknownPath)
            {
                g.setColor(Color.BLUE);
                g.fillRect(ulx,uly,asize,asize);
            }

            if (ci.isTerminal)
            {
                g.setColor(ci.isLoop ? Color.RED : Color.GREEN);
                g.drawRect(ulx,uly,asize,asize);
            }

            g.setColor(Color.green);

            if (ci.isNorth) g.drawLine(cenx,ceny,cenx,0);
            if (ci.isSouth) g.drawLine(cenx,ceny,cenx,bi.getHeight());
            if (ci.isWest) g.drawLine(cenx,ceny,0,ceny);
            if (ci.isEast) g.drawLine(cenx,ceny,bi.getWidth(),ceny);



            return true;
        }
    }

    private static class MyEdgeListener implements GridPanel.EdgeListener
    {
        Board b;
        public MyEdgeListener(Board b) { this.b = b; }
        public EdgeDescriptor onBoundary() { return new EdgeDescriptor(Color.black,5);}
        public EdgeDescriptor toEast(int x, int y)
        {
            char cur = b.getRegionId(x,y);
            char other = b.getRegionId(x+1,y);

            EdgeType et = b.getEdge(x,y, Direction.EAST);
            Color c = null;
            switch(et)
            {
                case PATH: c = Color.GREEN; break;
                case WALL: c = Color.RED; break;
                case UNKNOWN: c = Color.black; break;
            }

            return new EdgeDescriptor(c,(cur == other) ? 1 : 5);
        }

        @Override
        public EdgeDescriptor toSouth(int x, int y)
        {
            char cur = b.getRegionId(x,y);
            char other = b.getRegionId(x,y+1);

            EdgeType et = b.getEdge(x,y, Direction.SOUTH);
            Color c = null;
            switch(et)
            {
                case PATH: c = Color.GREEN; break;
                case WALL: c = Color.RED; break;
                case UNKNOWN: c = Color.black; break;
            }


            return new EdgeDescriptor(c,(cur == other) ? 1 : 5);
        }
    }





    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Bad command line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        Solver s = new Solver(b);

        b.setEdgePath(0,1,Direction.SOUTH);
        b.setEdgePath(0,6,Direction.SOUTH);
        b.setEdgePath(1,9,Direction.SOUTH);
        b.setEdgePath(0,12,Direction.EAST);
        b.setEdgePath(0,14,Direction.EAST);
        b.setEdgePath(3,1,Direction.SOUTH);
        b.setEdgePath(2,3,Direction.SOUTH);
        b.setEdgePath(2,9,Direction.SOUTH);
        b.setEdgePath(2,11,Direction.SOUTH);
        b.setEdgePath(2,14,Direction.SOUTH);
        b.setEdgePath(2,16,Direction.EAST);
        b.setEdgePath(5,3,Direction.EAST);
        b.setEdgePath(13,0,Direction.EAST);
        b.setEdgePath(11,5,Direction.EAST);
        b.setEdgePath(14,1,Direction.SOUTH);
        b.setEdgePath(5,10,Direction.SOUTH);
        b.setEdgePath(15,5,Direction.SOUTH);

        s.Solve(b);

        System.out.println("# Solutions: " + s.GetSolutions().size());
        b = s.GetSolutions().iterator().next();

        PathManager.Path theP = b.getPathManager().paths.iterator().next();
        Vector<Point> points = theP.cells;
 //       Collections.reverse(points);
  // when you reverse, swap definitions of inext and iprev so we go the right way.
  // reverse gets you the real answer, unreversed gets you an easter egg.

        int inext = points.indexOf(new Point(19,0));
        int icur = points.indexOf(new Point(20,0));
        int iprev = points.indexOf(new Point(20,1));
        int imax = points.size();

        int origprev = iprev;

        StringBuffer rights = new StringBuffer();
        StringBuffer straights = new StringBuffer();
        StringBuffer lefts = new StringBuffer();
        System.out.println("Pr:" + iprev + " cur: " + icur + " next: " + inext);

        while(icur != origprev)
        {
            Point cur = points.get(icur);
            switch(Turns.makeTurn(points.get(iprev),cur,points.get(inext)))
            {
                case LEFT: lefts.append(b.getLetter(cur.x,cur.y)); break;
                case RIGHT: rights.append(b.getLetter(cur.x,cur.y)); break;
                case STRAIGHT: straights.append(b.getLetter(cur.x,cur.y));  break;
            }

            ++iprev; if (iprev == imax) iprev = 0;
            ++icur;  if (icur == imax) icur = 0;
            ++inext; if (inext == imax) inext = 0;
        }

        System.out.println("Lefts: " + lefts);
        System.out.println("Rights: " + rights);
        System.out.println("Straights: " + straights);




        GridFrame gf = new GridFrame("Adalogical Aenigma #02 Solver",
        1200,800,new MyListener(b),new MyEdgeListener(b));
    }

}
