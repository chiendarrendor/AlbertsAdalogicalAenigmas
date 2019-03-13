import grid.letter.LetterRotate;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {

    public static void main(String[] args) {
	    if (args.length != 1) {
            System.out.println("bad command line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
	    Solver s = new Solver(b);
	    s.Solve(b);

	    System.out.println("# of Solutions: " + s.GetSolutions().size());

	    Board sol = s.GetSolutions().get(0);

	    StringBuffer sb = new StringBuffer();
	    for (int x = 0 ; x < sol.getWidth() ; ++x) {
	        int count = 0;
	        for (int y = 0 ; y < sol.getHeight() ; ++y) {
	            if (!sol.isSpecial(x,y)) continue;
	            count += sol.getCellSet(x,y).theNumber();
            }
            sb.append(LetterRotate.Rotate(sol.getClue().charAt(x),count));
        }


	    String[] lines = new String[] { sb.toString() , sol.getSolution() };
	    MyListener myl = new MyListener(b,lines);
	    GridFrame gf = new GridFrame("Adalogical Aenigma #65 Solver",1400,800,myl,myl);

    }

    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyListener(Board b,String[] lines) { this.b = b; this.lines = lines; }
        private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static final EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return b.getRegionId(x,y) == b.getRegionId(x+1,y) ? PATH : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) {return b.getRegionId(x,y) == b.getRegionId(x,y+1) ? PATH : WALL; }

        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.isSpecial(cx,cy)) {
                g.setColor(Color.PINK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            GridPanel.DrawStringInCell(bi,Color.BLACK,b.getCellSet(cx,cy).toString());
            return true;
        }


    }
}
