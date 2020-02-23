import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight();  }
        @Override public boolean drawGridNumbers() {  return true;  }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines;  }

        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return b.getRegion(x,y) == b.getRegion(x+1,y) ? PATH : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return b.getRegion(x,y) == b.getRegion(x,y+1) ? PATH : WALL; }


        private static int INSET=4;
        private static int SUPERINSET=9;
        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            Cell cell = b.getCell(cx,cy);
            if (cell.hasPossible(CellShape.BLANK)) {
                g.setColor(Color.PINK);
                g.fillRect(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
            }
            if (cell.hasPossible(CellShape.SQUARE)) {
                g.setColor(Color.CYAN);
                g.fillRect(SUPERINSET,SUPERINSET,bi.getWidth()-2*SUPERINSET,bi.getHeight()-2*SUPERINSET);
            }
            if (cell.hasPossible(CellShape.CIRCLE)) {
                g.setColor(Color.YELLOW);
                g.fillOval(SUPERINSET,SUPERINSET,bi.getWidth()-2*SUPERINSET,bi.getHeight()-2*SUPERINSET);
            }
            if (cell.hasPossible(CellShape.TRIANGLE)) {
                g.setColor(Color.GREEN);
                g.fillPolygon(
                        new int[] { bi.getWidth()/2,bi.getWidth()-SUPERINSET,SUPERINSET },
                        new int[] { SUPERINSET, bi.getHeight()-SUPERINSET,bi.getHeight()-SUPERINSET},
                        3
                );
            }

            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getLetter(cx,cy));
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
        String[] lines = new String[] { "Adalogical", "Aenigma #76" };

        //System.out.println("RAL: " + s.recursiveApplyLogic(b));

        s.Solve(b);
        System.out.println("# of Solutions: " + s.GetSolutions().size());
        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);
            String clue = ClueProcessor.Process(b);
            lines[0] = clue;
            lines[1] = b.gfr.getVar("SOLUTION");
        }








        MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #76 solver",1200,800,myl,myl);
    }


}
