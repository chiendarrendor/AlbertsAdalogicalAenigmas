import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyListener(Board b,String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static final EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);
        private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);

        private boolean sameRegion(int x,int y, Direction d) {
            Point p1 = d.delta(x,y,1);
            return b.getRegionId(x,y) == b.getRegionId(p1.x,p1.y);
        }

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return sameRegion(x,y,Direction.EAST) ? PATH : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return sameRegion(x,y,Direction.SOUTH) ? PATH : WALL; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (b.hasArrow(cx,cy)) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy),Direction.NORTHWEST);
            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getRegionId(cx,cy),Direction.SOUTHWEST);

            Color c = Color.BLACK;
            char str = '?';
            Cell cell = b.getCell(cx,cy);

            if (cell.size() == 0) {
                c = Color.RED;
                str = '!';
            } else if (cell.size() == 1) {
                Direction d = cell.getSolo();
                if (d == null) {
                    str = '-';
                } else {
                    str = d.getSymbol();
                }
            }
            GridPanel.DrawStringInCell(bi,c,""+str);

            return true;
        }
    }


    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }
        Board b = new Board(args[0]);
        String[] lines = new String[] { "Adalogical Aenigma","#88 Solver"};

        Solver s = new Solver(b);
        s.Solve(b);

        System.out.println("# of Solutions Found: " + s.GetSolutions().size());
        if (s.GetSolutions().size() > 0) {
            b = s.GetSolutions().get(0);
            Board fb = b;

            CellContainer<Integer> counts = new CellContainer<Integer>(b.getWidth(),b.getHeight(),(x,y)->0);
            b.forEachCell((x,y)-> {
                Cell c = fb.getCell(x,y);
                if (c.getSolo() == null) return;
                Direction d = c.getSolo();
                for(int i = 1 ; ; ++i) {
                    Point op = d.delta(x,y,i);
                    Cell oc = fb.getCell(op.x,op.y);
                    if (oc.getSolo() != null) break;
                    counts.setCell(op.x,op.y,counts.getCell(op.x,op.y)+1);
                }
            });

            StringBuffer sb = new StringBuffer();
            b.forEachCell((x,y)-> {
                int count = counts.getCell(x,y);
                if (count != 2) return;
                sb.append(LetterRotate.Rotate(fb.getLetter(x,y),2));
            });
            lines[0] = sb.toString();
            lines[1] = b.gfr.getVar("SOLUTION");

        }




        MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #88 Solver",1200,800,myl,myl);
    }
}
