import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class Main {
    private static class MyReference implements GridPanel.EdgeListener, GridPanel.GridListener {
        String[] lines;
        Board b;
        public MyReference(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        private EdgeDescriptor inDir(int x,int y,Direction d) {
            Point np = d.delta(x,y,1);
            return b.getRegion(x,y) == b.getRegion(np.x,np.y) ? PATH : WALL;
        }

        @Override public EdgeDescriptor toEast(int x, int y) { return inDir(x,y,Direction.EAST); }
        @Override public EdgeDescriptor toSouth(int x, int y) { return inDir(x,y,Direction.SOUTH); }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy),Direction.NORTHWEST);
            if (b.hasNumber(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getNumber(cx,cy),Direction.NORTHEAST);

            Graphics2D g = (Graphics2D)bi.getGraphics();
            g.setColor(Color.GREEN);
            g.setStroke(new BasicStroke(5.0f));
            switch(b.getCell(cx,cy)) {
                case HORIZONTAL:
                    g.drawLine(0,bi.getHeight()/2,bi.getWidth(),bi.getHeight()/2);
                    break;
                case VERTICAL:
                    g.drawLine(bi.getWidth()/2,0,bi.getWidth()/2,bi.getHeight());
                    break;
                case UNKNOWN:
                    break;
            }


            return true;
        }


    }
    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
        Solver s = new Solver(b);
        s.Solve(b);
        System.out.println("# of solutions: " + s.GetSolutions().size());

        final Board fb = s.GetSolutions().get(0);

        Set<Integer> evenrows = new HashSet<>();

        for (int y = 0 ; y < fb.getHeight() ; ++y) {
            int horcount = 0;
            for (int x = 0 ; x < fb.getWidth() ; ++x) {
                if (fb.getCell(x,y) == CellState.HORIZONTAL) ++horcount;
            }
            if (horcount % 2 == 0) evenrows.add(y);
        }

        StringBuffer sb = new StringBuffer();

        for (int y = 0 ; y < fb.getHeight() ; ++y) {
            if (!evenrows.contains(y)) continue;
            for (int x = 0 ; x < fb.getWidth() ; ++x) {
                if (!fb.hasLetter(x,y)) continue;
                sb.append(LetterRotate.Rotate(fb.getLetter(x,y),evenrows.size()));
            }
        }



	    String[] lines = new String[] { sb.toString(),fb.gfr.getVar("SOLUTION") };
	    MyReference myr = new MyReference(fb,lines);
	    GridFrame gf = new GridFrame("Adalogical Aenigma #69 Solver",1200,800,myr,myr);
    }


}
