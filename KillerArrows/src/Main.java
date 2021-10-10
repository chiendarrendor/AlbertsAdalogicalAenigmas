import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyListener implements GridPanel.GridListener, GridPanel.EdgeListener {
        Board b;
        public MyListener(Board b) { this.b = b; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return new String[] { "Killer Arrows Solver", "puzzle URL: https://app.crackingthecryptic.com/sudoku/RmhNHMBJGg" }; }

        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return (x%3 == 2) ? WALL : PATH; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return (y%3 == 2) ? WALL : PATH; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {

            if (b.hasArrow(cx,cy)) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getArrow(cx,cy), Direction.SOUTH);
            }

            Cage cage = b.getCageInfo(cx,cy);
            if (cage != null) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+cage.id,Direction.NORTH);
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+cage.size,Direction.NORTHEAST);
            }


            Cell c = b.getCell(cx,cy);


            StringBuffer sb = new StringBuffer();
            for (int i = 1 ; i <= 9 ; ++i) {
                if (c.has(i)) sb.append(i);
            }

            GridPanel.DrawStringInCell(bi,Color.BLACK,sb.toString());
            return true;
        }
    }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        Board b = new Board (args[0]);

        Solver s = new Solver(b);

        s.Solve(b);

        if (s.GetSolutions().size() == 1) {
            System.out.println("Unique Solution Found!");
            b = s.GetSolutions().get(0);
        }


        MyListener myl = new MyListener(b);
        GridFrame gf = new GridFrame("Killer Arrows",1200,800,myl,myl);
    }
}
