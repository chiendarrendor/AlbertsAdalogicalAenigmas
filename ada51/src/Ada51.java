import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Ada51
{

    private static class MyGridListener implements GridPanel.GridListener {
        private Board b;
        public MyGridListener(Board b) { this.b = b;  }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }
        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            switch(b.getCell(cx,cy))
            {
                case ONPATH: g.setColor(new Color(152,251,152)); g.fillRect(0,0,bi.getWidth(),bi.getHeight()); break;
                case NOTPATH: g.setColor(new Color(255,182,193)); g.fillRect(0,0,bi.getWidth(),bi.getHeight()); break;
                case UNKNOWN: break;
            }

            char c = b.getLetter(cx,cy);
            if (c != '.') GridPanel.DrawStringInCorner(bi,Color.BLACK,""+c, Direction.NORTHWEST);
            if (cx == 0) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getHClue(cy),Direction.SOUTHWEST);
            if (cy == 0) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getVClue(cx), Direction.NORTHEAST);

            g.setColor(Color.RED);
            if (b.getEdge(cx,cy,Direction.NORTH) == EdgeType.NOTPATH) g.drawLine(0,1,bi.getWidth(),1);
            if (b.getEdge(cx,cy,Direction.SOUTH) == EdgeType.NOTPATH) g.drawLine(0,bi.getHeight()-2,bi.getWidth(),bi.getHeight()-2);
            if (b.getEdge(cx,cy,Direction.WEST) == EdgeType.NOTPATH) g.drawLine(1,0,1,bi.getHeight());
            if (b.getEdge(cx,cy,Direction.EAST) == EdgeType.NOTPATH) g.drawLine(bi.getWidth()-2,0,bi.getWidth()-2,bi.getHeight());

            g.setColor(Color.GREEN);
            g.setStroke(new BasicStroke(5));
            int cenx = bi.getWidth()/2;
            int ceny = bi.getHeight()/2;
            if (b.getEdge(cx,cy,Direction.NORTH) == EdgeType.PATH) g.drawLine(cenx,ceny,cenx,0);
            if (b.getEdge(cx,cy,Direction.SOUTH) == EdgeType.PATH) g.drawLine(cenx,ceny,cenx,bi.getHeight());
            if (b.getEdge(cx,cy,Direction.WEST) == EdgeType.PATH) g.drawLine(cenx,ceny,0,ceny);
            if (b.getEdge(cx,cy,Direction.EAST) == EdgeType.PATH) g.drawLine(cenx,ceny,bi.getWidth(),ceny);



            return true;
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
        s.Solve(b);

        System.out.println("# of Solutions: " + s.GetSolutions().size());


        Board sol = s.GetSolutions().get(0);

        Point topTerminal = new Point(-1,-1);
        sol.forEachCell((x,y)-> {
            if (topTerminal.x != -1) return;
            if (!sol.isTerminal(x,y)) return;
            topTerminal.x = x;
            topTerminal.y = y;
        });

        Path solpath = sol.gpc.iterator().next();
        if (!topTerminal.equals(solpath.endOne())) solpath.reverse();

        for (int i = 0 ; i < 2 ; ++i) {
            Point cur;
            Point prev = null;
            Point prevprev = null;

            StringBuffer lefts = new StringBuffer();
            StringBuffer straights = new StringBuffer();
            StringBuffer rights = new StringBuffer();

            for (Point pt : solpath) {
                if (prev != null && prevprev != null && sol.getLetter(prev.x, prev.y) != '.') {
                    switch (Turns.makeTurn(prevprev, prev, pt)) {
                        case LEFT:
                            lefts.append(sol.getLetter(prev.x, prev.y));
                            break;
                        case RIGHT:
                            rights.append(sol.getLetter(prev.x, prev.y));
                            break;
                        case STRAIGHT:
                            straights.append(sol.getLetter(prev.x, prev.y));
                            break;
                    }
                }

                prevprev = prev;
                prev = pt;
            }
            System.out.println("LEFTS: " + lefts.toString());
            System.out.println("RIGHTS: " + rights.toString());
            System.out.println("STRAIGHTS: " + straights.toString());
            solpath.reverse();
        }


        GridFrame gf = new GridFrame("Aenigma #51 Solver",1200,800,new MyGridListener(sol));


    }


}
