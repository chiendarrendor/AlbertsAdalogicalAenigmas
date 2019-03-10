import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static class MyListener implements GridPanel.MultiGridListener,GridPanel.EdgeListener {
        List<Board> bar;
        String[] lines;
        int curidx = 0;

        public MyListener(List<Board> bar,String[] lines) { this.bar = bar; this.lines = lines; }

        @Override public boolean hasNext() { return curidx < bar.size() - 1; }
        @Override public void moveToNext() { ++curidx; }
        @Override public boolean hasPrev() { return curidx > 0; }
        @Override public void moveToPrev() { --curidx; }

        private Board b() { return bar.get(curidx); }

        @Override public int getNumXCells() { return b().getWidth(); }
        @Override public int getNumYCells() { return b().getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            Region r = b().getRegion(cx,cy);

            if (r == null ) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                return true;
            }

            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+r.getId(),Direction.SOUTHWEST);
            if (b().isSpecial(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.RED,"O",Direction.NORTHWEST);
            GridPanel.DrawStringInCell(bi,Color.BLACK,
                    b().getCellSet(cx,cy).stream().map(i->i.toString()).collect(Collectors.joining("")));




            return true;
        }



        private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static final EdgeDescriptor OPEN = new EdgeDescriptor(Color.BLACK,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        private boolean sameRegion(int x,int y, Direction d) {
            Point op = d.delta(new Point(x,y),1);
            return b().getRegion(x,y) == b().getRegion(op.x,op.y);
        }

        @Override public EdgeDescriptor toEast(int x, int y) {
            return sameRegion(x,y,Direction.EAST) ? OPEN : WALL;
        }

        @Override public EdgeDescriptor toSouth(int x, int y) {
            return sameRegion(x,y,Direction.SOUTH) ? OPEN : WALL;
        }

    }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad command line" );
            System.exit(1);
        }
        Board b = new Board(args[0]);
        Solver s = new Solver(b);
        List<Board> bar = new ArrayList<>();

        bar.add(b);
        b = new Board(b);

        s.Solve(b);




        System.out.println("Number of solutions: " + s.GetSolutions().size());
        bar.addAll(s.GetSolutions());

        Board solution = s.GetSolutions().get(0);

        StringBuffer sb = new StringBuffer();
        for (int x = 0 ; x < solution.getWidth() ; ++x ) {
            int rot = 0;
            for (int y = 0 ; y < solution.getHeight() ; ++y) {
                if (!solution.isSpecial(x,y)) continue;
                rot += solution.getCellSet(x,y).theNumber();
            }
            if (rot > 0) sb.append(LetterRotate.Rotate('Z',rot));
        }




        String[] lines = new String[] { sb.toString(), b.gfr.getVar("SOLUTION") };

        MyListener mylistener = new MyListener(bar,lines);
        GridFrame gf = new GridFrame("XOXO qq Chalk Solver",1200,800,mylistener,mylistener);
    }
}
