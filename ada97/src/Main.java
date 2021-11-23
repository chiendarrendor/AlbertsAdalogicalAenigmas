import grid.copycon.Deep;
import grid.puzzlebits.Direction;
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

    private static class MyListener implements GridPanel.GridListener, GridPanel.EdgeListener{
        Board b;
        String[] lines;
        public MyListener(Board b,String[] lines) { this.b = b; this.lines = lines; }

        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static EdgeDescriptor INSIDE = new EdgeDescriptor(Color.BLACK,1);

        private EdgeDescriptor getED(int x, int y, Direction d) {
            Point op = d.delta(x,y,1);
            return b.getRegion(x,y) == b.getRegion(op.x,op.y) ? INSIDE : WALL;
        }

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return getED(x,y,Direction.EAST); }
        @Override public EdgeDescriptor toSouth(int x, int y) { return getED(x,y,Direction.SOUTH); }

        private void drawPath(BufferedImage bi,int x,int y, Direction d) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            int cenx = bi.getWidth()/2;
            int ceny = bi.getHeight()/2;

            EdgeState es = b.getEdge(x,y,d);

            int ox = -1;
            int oy = -1;

            switch(d) {
                case NORTH: ox = cenx; oy = 0; break;
                case EAST: ox = bi.getWidth(); oy = ceny; break;
                case SOUTH: ox = cenx; oy = bi.getHeight(); break;
                case WEST: ox = 0; oy = ceny ; break;
            }

            switch(es) {
                case UNKNOWN: return;
                case PATH:
                    g.setColor(Color.BLUE);
                    g.setStroke(new BasicStroke(5));
                    g.drawLine(cenx,ceny,ox,oy);
                    break;
                case WALL:
                    GridPanel.DrawStringInCorner(bi,Color.RED,"X",d);
                    break;
            }
        }



        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {


            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy),Direction.NORTHWEST);
            if (b.hasNumber(cx,cy)) {
                Graphics2D g = (Graphics2D)bi.getGraphics();
                g.setColor(Color.BLACK);
                g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 3.0f));
                GridPanel.DrawStringInCorner(g,0,0,bi.getWidth(),bi.getHeight(),""+b.getNumber(cx,cy),Direction.NORTHWEST);
            }

            drawPath(bi,cx,cy,Direction.NORTH);
            drawPath(bi,cx,cy,Direction.SOUTH);
            drawPath(bi,cx,cy,Direction.WEST);
            drawPath(bi,cx,cy,Direction.EAST);


            return true;
        }

    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        String[] lines = new String[] { "", "Adalogical Aenigma", "#97 solver"};
        lines[0] = b.getTitle();

        Solver s = new Solver(b);

        s.Solve(b);





        if (s.GetSolutions().size() == 1) {
            System.out.println("Solution Found");
            b = s.GetSolutions().get(0);

            Path p = b.getPaths().iterator().next();
            Point start = new Point(0,0);
            Path.Cursor cursor = p.getCursor(0,0);
            if (!cursor.getNext().equals(new Point(1,0))) {
                p.reverse();
                cursor = p.getCursor(0,0);
            }

            StringBuffer sb = new StringBuffer();
            for( ; !cursor.getNext().equals(start) ; cursor.next()) {
                Point cp = cursor.get();
                if (!b.hasLetter(cp.x,cp.y)) continue;
                if (Turns.makeTurn(cursor.getPrev(),cursor.get(),cursor.getNext()) != Turns.RIGHT) continue;
                sb.append(b.getLetter(cp.x,cp.y));
            }

            lines[1] = sb.toString();
            lines[2] = b.getSolution();

        }


        MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #97 solver",1200,800,myl,myl);

    }
}
