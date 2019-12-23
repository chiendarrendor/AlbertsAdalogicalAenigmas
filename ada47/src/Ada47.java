import grid.letter.LetterRotate;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Turns;
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
        String[] lines;

        public MyListener(Board b,String[] lines) { this.b = b; this.lines = lines;}


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
        public String[] getAnswerLines() { return lines; }


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


            if (b.getSolverID() == 74) {
                if (b.hasNumericClue(cx,cy)) {
                    GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getNumericClue(cx,cy),Direction.SOUTHEAST);
                }
            }



            return true;
        }
    }

    public static String getClue47(Board b) {
        StringBuffer lsb = new StringBuffer();
        StringBuffer rsb = new StringBuffer();
        StringBuffer ssb = new StringBuffer();

        Path p = b.ps.paths.iterator().next();
        Vector<Point> mycells = new Vector<Point>();
        mycells.addAll(p.cells);

        mycells.remove(0);
        int sidx = mycells.indexOf(new Point(0,11));
        int nidx = mycells.indexOf(new Point(0,10));
        if (sidx > nidx) Collections.reverse(mycells);
        while(mycells.indexOf(new Point(0,11)) > 0)
        {
            mycells.add(mycells.remove(0));
        }

        sidx = mycells.indexOf(new Point(0,11));
        nidx = mycells.indexOf(new Point(0,10));

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
        return rsb.toString();
    }

    public static String getClue74(Board b) {
        StringBuffer sb = new StringBuffer();
        Path p = b.ps.paths.iterator().next();
        p.cells.remove(0);
        PathCursor pc = new PathCursor(p,0,0);
        Point next = pc.getNext();
        if (next.x != 1 || next.y != 0) {
            p.reverse();
            pc = new PathCursor(p,0,0);
        }
        pc.next();

        while(true) {
            Point curp = pc.getCur();
            if (b.hasLetter(curp.x,curp.y)) {
                Turns t = Turns.makeTurn(pc.getPrev(), pc.getCur(), pc.getNext());

                if (t == Turns.RIGHT) {
                    int sum = -1;
                    for (Direction d : Direction.orthogonals()) {
                        if (b.getEdge(curp.x, curp.y, d) != EdgeType.PATH) continue;
                        sum += b.getStraightMinMax(curp.x, curp.y, d).max;
                    }

                    sb.append(LetterRotate.Rotate(b.getLetter(curp.x, curp.y), sum));
                }
            }

            if (pc.atEnd()) break;
            pc.next();
        }







        return sb.toString();
    }



    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println("Bad command line");
            System.exit(1);
        }


        LogicBoard b = new LogicBoard(args[0]);

        Solver s = new Solver(b);

        s.Solve(b);
        System.out.println("# of Solutions: " + s.GetSolutions().size());
        if (s.GetSolutions().size() != 1) System.exit(1);
        b = s.GetSolutions().iterator().next();

        String rawclue = null;
        switch(b.getSolverID()) {
            case 47:
                rawclue = getClue47(b);
                break;
            case 74:
                rawclue = getClue74(b);
                break;
            default:
                throw new RuntimeException("Unknown clue type " + b.getSolverID());
        }







        String[] lines = new String[] {  rawclue, b.gfr.getVar("SOLUTION") };
        GridFrame gf = new GridFrame("Adalogical Aenigma #" + b.getSolverID(),1200,900,new MyListener(b,lines));
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
