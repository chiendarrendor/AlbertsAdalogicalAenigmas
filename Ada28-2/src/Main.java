import grid.copycon.Deep;
import grid.graph.GridGraph;
import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.PointAdjacency;
import grid.puzzlebits.Turns;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    private static class MyListener implements GridPanel.GridListener {
        Board b;
        String[] lines;
        List<Point> path;
        public MyListener(Board b,List<Point> path, String[] lines) { this.b = b; this.lines = lines; this.path = path; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            CellType ct = b.getCell(cx,cy);
            Color c = null;

            switch(ct) {
                case UNKNOWN: break;
                case PATH: c = new Color(203,65,84); break;
                case TREE: c = Color.GREEN; break;
            }

            if (c != null) {
                g.setColor(c);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+b.getLetter(cx,cy));
            if (b.isVista(cx,cy)) GridPanel.DrawStringInCell(bi,Color.WHITE,""+b.vistaNumber(cx,cy));

            int index = path.indexOf(new Point(cx,cy));
            if (index >= 0) {
                int revi = path.size() - index - 1;
                GridPanel.DrawStringInCorner(bi, Color.GREEN, "" + revi, Direction.SOUTHEAST);
            }


            return true;
        }
    }

    private static List<Point> findGreatestExtent(Board b) {
        List<Point> vistas = new ArrayList<>();
        b.forEachCell((x,y)->{
            if (b.isVista(x,y)) vistas.add(new Point(x,y));
        });

        class MyReference implements GridGraph.GridReference {
            @Override public int getWidth() { return b.getWidth(); }
            @Override public int getHeight() { return b.getHeight(); }
            @Override public boolean isIncludedCell(int x, int y) { return b.getCell(x,y) == CellType.PATH; }
            @Override public boolean edgeExitsEast(int x, int y) { return true; }
            @Override public boolean edgeExitsSouth(int x, int y) { return true; }
        }

        GridGraph gg = new GridGraph(new MyReference());
        int v1 = 0;
        int v2 = 0;
        int length = -1;
        Point mp1 = null;
        Point mp2 = null;


        for (int i = 0 ; i < vistas.size() ; ++i ) {
            Point p1 = vistas.get(i);
            for (int j = i + 1; j < vistas.size(); ++j) {
                Point p2 = vistas.get(j);

                List<Point> path = gg.shortestPathBetween(p1, p2);
                if (mp1 == null || path.size() > length) {
                    v1 = b.vistaNumber(p1.x, p1.y);
                    v2 = b.vistaNumber(p2.x, p2.y);
                    length = path.size();
                    mp1 = p1;
                    mp2 = p2;
                    System.out.println("Best: " + length + " between " + mp1 + " and " + mp2);
                } else if (mp1 != null && path.size() == length) {
                    int mv1 = b.vistaNumber(p1.x,p1.y);
                    int mv2 = b.vistaNumber(p2.x,p2.y);

                    if (v1 + v2 > mv1 + mv2) {
                        v1 = mv1;
                        v2 = mv2;
                        mp1 = p1;
                        mp2 = p2;
                        System.out.println("Smaller numbers than best: " + length + " between " + p1 + " and " + p2);
                    }
                }
            }
        }
        List<Point> result = new ArrayList<>();
        if (v1 < v2) {
            result.add(mp1);
            result.add(mp2);
        } else {
            result.add(mp2);
            result.add(mp1);
        }
        return result;
    }


    private static List<Point> findBestPath(Board b, Point start, Point end) {
        CellContainer<Integer> cc = new CellContainer<Integer>(b.getWidth(),b.getHeight(),(x,y)->-1);
        List<Point> queue = new ArrayList<>();

        queue.add(end);
        cc.setCell(end.x,end.y,0);

        while(queue.size() > 0) {
            Point qp = queue.remove(0);
            for (Direction d : Direction.orthogonals()) {
                Point np = d.delta(qp,1);
                if (!b.onBoard(np)) continue;
                if (cc.getCell(np.x,np.y) > -1) continue;
                if (b.getCell(np.x,np.y) == CellType.TREE) continue;

                cc.setCell(np.x,np.y,cc.getCell(qp.x,qp.y) + 1);
                queue.add(np);
            }
        }

        List<Point> result = new ArrayList<>();
        Point cur = start;
        Point prev = null;

        while(true) {
            result.add(cur);
            int curval = cc.getCell(cur.x,cur.y);
            if (curval == 0) break;

            Set<Direction> dirs = new HashSet<>();
            Point straightpoint = null;
            Point apoint = null;
            for (Direction d : Direction.orthogonals()) {
                Point dp = d.delta(cur,1);
                if (!b.onBoard(dp)) continue;
                if (curval - 1 == cc.getCell(dp.x,dp.y)) {
                    dirs.add(d);

                    if (prev != null && Turns.makeTurn(prev,cur,dp) == Turns.STRAIGHT) straightpoint = dp;
                    apoint = dp;
                }
            }

            if (dirs.size() == 0) throw new RuntimeException("How did I get into an oubliette?");
            if (prev == null && dirs.size() > 1)  throw new RuntimeException("Have to make a first-space choice!");
            if (straightpoint == null && dirs.size() > 1) throw new RuntimeException("Choice with no straight!");

            Point next = null;
            if (straightpoint != null) next = straightpoint;
            else next = apoint;


            prev = cur;
            cur = next;
        }

        return result;
    }






    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        String infile = args[0];
        String outfile = args[1];

        Board b = new Board(infile);

        if( new File(outfile).exists()) {
            b.fillFrom(outfile);
        } else {
            Solver s = new Solver(b);
            s.Solve(b);
            System.out.println("# of Solutions: " + s.GetSolutions().size());
            b = s.GetSolutions().get(0);
            b.writeTo(outfile);
        }

        List<Point> pathpoints = b.pathHints;
        if (pathpoints.size() == 0) {
            System.out.println("Manual Creation of pathing hints");
            pathpoints = findGreatestExtent(b);
        }

        List<List<Point>> pathparts = new ArrayList<>();


        for (int i = 0 ; i < pathpoints.size() - 1 ; ++i) {
            Point start = pathpoints.get(i);
            Point end = pathpoints.get(i+1);
            pathparts.add(findBestPath(b,start,end));
        }

        List<Point> finalpath = pathparts.get(0);
        pathparts.remove(0);
        for (List<Point> pathpart : pathparts) {
            pathpart.remove(0);
            finalpath.addAll(pathpart);
        }


        StringBuffer sb = new StringBuffer();
        int curnum = 0;
        for (Point p : finalpath) {
            if (b.isVista(p.x,p.y)) curnum = b.vistaNumber(p.x,p.y);
            else sb.append(LetterRotate.Rotate(b.getLetter(p.x,p.y),curnum));
        }







        String[] lines = new String[] {sb.toString(),b.gfr.getVar("SOLUTION")};
        GridFrame gf = new GridFrame("Adalogical Aenigma #28 Solver: " + infile,1200,800,new MyListener(b,finalpath,lines));
    }
}
