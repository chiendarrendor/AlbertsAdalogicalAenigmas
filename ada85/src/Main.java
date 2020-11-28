import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Main {
    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public String[] getAnswerLines() { return lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }

        private static EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);
        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return b.sameRegion(x,y,Direction.EAST) ? PATH : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return b.sameRegion(x,y, Direction.SOUTH) ? PATH : WALL; }


        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy),Direction.NORTHWEST);
            GridPanel.DrawStringInCell(bi,Color.BLACK,b.getCell(cx,cy).toString());
            return true;
        }

    }

    private static boolean isPrime(int p) { return p == 2 || p == 3 || p == 5 || p == 7 || p == 11 || p == 13 || p == 17; }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }
        Board b = new Board(args[0]);
        String[] lines = new String[] { "Adalogical Aenigma", "#85 Solver"};
        Solver s = new Solver(b);

        //s.testRecursion(b);
        s.Solve(b);

        if (s.GetSolutions().size() == 1) {
            // second stage clue solving goes here
            StringBuffer sb = new StringBuffer();
            Board fb = s.GetSolutions().get(0);
            fb.forEachCell((x,y)-> {
                if (!fb.hasLetter(x,y)) return;
                CellContents cc = fb.getCell(x,y);
                int v = cc.getPossibles().iterator().next();
                if (!isPrime(v)) return;
                sb.append(LetterRotate.Rotate(fb.getLetter(x,y),v));
            });

            lines[0] = sb.toString();
            lines[1] = fb.gfr.getVar("SOLUTION");

            b = fb;
        } else if (s.GetSolutions().size() == 0){
            System.out.println("no solutions found");
        } else {
            System.out.println("multiple solutions found");
        }

        MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #85 Solver",1200,800,myl,myl);
    }


}
