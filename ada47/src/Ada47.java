import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Vector;

public class Ada47
{
    private static class MyListener implements GridPanel.GridListener
    {
        Board b;

        public MyListener(Board b) { this.b = b;}


        public int getNumXCells()
        {
            return b.getWidth();
        }
        public int getNumYCells()
        {
            return b.getHeight();
        }
        public boolean drawGridNumbers()
        {
            return true;
        }
        public boolean drawGridLines()
        {
            return true;
        }
        public boolean drawBoundary()
        {
            return true;
        }
        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if(b.hasLetter(cx,cy))  GridPanel.DrawStringInCorner(bi, Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(5));
            int cenx = bi.getWidth()/2;
            int ceny = bi.getHeight()/2;

            if (b.hasPath(cx,cy,Direction.NORTH)) g.drawLine(cenx,ceny,cenx,0);
            if (b.hasPath(cx,cy,Direction.SOUTH)) g.drawLine(cenx,ceny,cenx,bi.getHeight());
            if (b.hasPath(cx,cy,Direction.WEST)) g.drawLine(cenx,ceny,0,ceny);
            if (b.hasPath(cx,cy,Direction.EAST)) g.drawLine(cenx,ceny,bi.getWidth(),ceny);

            g.setColor(Color.red);
            g.setStroke(new BasicStroke(1));
            int INSET = 5;
            int unxi = bi.getWidth() - INSET;
            int unyi = bi.getHeight() - INSET;
            if (b.hasWall(cx,cy,Direction.NORTH)) g.drawLine(INSET,INSET,unxi,INSET);
            if (b.hasWall(cx,cy,Direction.SOUTH)) g.drawLine(INSET,unyi,unxi,unyi);
            if (b.hasWall(cx,cy,Direction.EAST)) g.drawLine(unxi,INSET,unxi,unyi);
            if (b.hasWall(cx,cy,Direction.WEST)) g.drawLine(INSET,INSET,INSET,unyi);






            return true;
        }
    }


    public static void main(String[] args)
    {
        LogicBoard b = new LogicBoard("ada47.txt");

        Solver s = new Solver(b);

        s.Solve(b);
        System.out.println("# of Solutions: " + s.GetSolutions().size());
        if (s.GetSolutions().size() != 1) System.exit(1);

        b = s.GetSolutions().iterator().next();

        Path p = b.ps.paths.iterator().next();
        Vector<Point> mycells = new Vector<Point>();
        mycells.addAll(p.cells);

        mycells.remove(0);
        int sidx = mycells.indexOf(new Point(0,11));
        int nidx = mycells.indexOf(new Point(0,10));
        System.out.println("Start: " + sidx + " next: " + nidx);
        if (sidx > nidx) Collections.reverse(mycells);
        while(mycells.indexOf(new Point(0,11)) > 0)
        {
            mycells.add(mycells.remove(0));
        }

        sidx = mycells.indexOf(new Point(0,11));
        nidx = mycells.indexOf(new Point(0,10));
        System.out.println("Start: " + sidx + " next: " + nidx);

        StringBuffer lsb = new StringBuffer();
        StringBuffer rsb = new StringBuffer();
        StringBuffer ssb = new StringBuffer();

        for (int i = 0 ; i < mycells.size() ; ++i)
        {
            int bi = (i==0) ? mycells.size()-1 : i-1;
            int ni = (i == mycells.size()-1) ? 0 : i+1;

            Point bp = mycells.elementAt(bi);
            Point cp = mycells.elementAt(i);
            Point np = mycells.elementAt(ni);
            if (!b.hasLetter(cp.x,cp.y)) continue;
            char letter = b.getLetter(cp.x,cp.y);

            if (isRight(bp,cp,np)) rsb.append(letter);
            else if (isLeft(bp,cp,np)) lsb.append(letter);
            else ssb.append(letter);
        }

        System.out.println("Right: " + rsb.toString());
        System.out.println("Left: " + lsb.toString());
        System.out.println("Straight: " + ssb.toString());

        System.out.println("");



        GridFrame gf = new GridFrame("Adalogical Aenigma #47",1200,900,new MyListener(b));
	}

    private static boolean isRight(Point bp, Point cp, Point np)
    {
        int dx = cp.x - bp.x;
        int dy = cp.y - bp.y;

        if (dx == -1 && np.x == cp.x && np.y == cp.y - 1) return true;
        if (dx == 1 && np.x == cp.x && np.y == cp.y + 1) return true;
        if (dy == -1 && np.x == cp.x + 1 && np.y == cp.y) return true;
        if (dy == 1 && np.x == cp.x - 1 && np.y == cp.y) return true;
        return false;
    }

    private static boolean isLeft(Point bp, Point cp, Point np)
    {
        int dx = cp.x - bp.x;
        int dy = cp.y - bp.y;

        if (dx == -1 && np.x == cp.x && np.y == cp.y + 1) return true;
        if (dx == 1 && np.x == cp.x && np.y == cp.y - 1) return true;
        if (dy == -1 && np.x == cp.x - 1 && np.y == cp.y) return true;
        if (dy == 1 && np.x == cp.x + 1 && np.y == cp.y) return true;
        return false;
    }
}
