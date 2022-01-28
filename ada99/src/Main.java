import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyListener implements GridPanel.GridListener,GridPanel.EdgeListener {
        private Board b;
        private String[] lines;

        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static final EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);
        private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private EdgeDescriptor whichEdge(int x,int y,Direction d) {
            Point op = d.delta(x,y,1);
            return (b.getRegionId(x,y) == b.getRegionId(op.x,op.y)) ? PATH : WALL;
        }
        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return whichEdge(x,y,Direction.EAST); }
        @Override public EdgeDescriptor toSouth(int x, int y) { return whichEdge(x,y,Direction.SOUTH); }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            CellState cs = b.getCell(cx,cy);
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (cs == CellState.SHADED) {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            } else if (cs == CellState.UNSHADED) {
                g.setColor(Color.GREEN);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }
            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy),Direction.NORTHWEST);
            if (b.hasNumber(cx,cy)) GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getNumber(cx,cy));

            return true;
        }




    }

    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }
        Board b = new Board(args[0]);
	    String[] lines = new String[] { b.getName(),"#99","Solver"};
	    Solver s = new Solver(b);

	    s.Solve(b);

	    if (s.GetSolutions().size() == 1) {
	        System.out.println("Unique Solution Found");
	        b = s.GetSolutions().get(0);
	        StringBuffer sb = new StringBuffer();
	        for (int y = 0 ; y < b.getHeight() ; ++y) {
	            for (int x = 0 ; x < b.getWidth() ; ++x) {
	                if (b.getCell(x,y) != CellState.UNSHADED) continue;
	                if (!b.hasLetter(x,y)) continue;
	                if (b.shadedCount(x,y) != 2) continue;
	                sb.append(LetterRotate.Rotate(b.getLetter(x,y),2));
                }
            }
            lines[1] = sb.toString();
	        lines[2] = b.getSolution();
        }



	    MyListener myl = new MyListener(b,lines);
	    GridFrame gf = new GridFrame("Adalogical Aenigma #99 Solver",1200,800,myl,myl);
    }


}
