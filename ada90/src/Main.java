import grid.file.GridFileReader;
import grid.graph.GridGraph;
import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import javax.swing.plaf.ColorUIResource;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyListener implements GridPanel.GridListener {
        Board b;
        String[] lines;

        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth();  }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }


        private static final int INSET=5;

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D) bi.getGraphics();
            CellState cs = b.getCellState(cx,cy);
            if (cs == CellState.WALL) {
                g.setColor(ColorUIResource.RED);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            } else if (cs == CellState.PATH) {
                g.setColor(Color.GREEN);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            if (b.hasClue(cx,cy)) {
                g.setColor(Color.BLACK);
                g.drawRoundRect(INSET,INSET,
                        bi.getWidth()-2*INSET,bi.getHeight()-2*INSET,
                        INSET,INSET);

                if (b.getClue(cx,cy) > 0) {
                    GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getClue(cx,cy));
                }
            }
            return true;
        }
    }

    private static class MyGraphReference implements GridGraph.GridReference {
        Board b;
        public MyGraphReference(Board b) {this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return b.getCellState(x,y) == CellState.WALL; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }



    public static void main(String[] args) {
	    if (args.length < 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
	    String[] lines = new String[] { "Adalogical Aenigma","#90 Solver"};

	    if (args.length == 2) {
            GridFileReader solgrid = new GridFileReader(args[1]);
            b.forEachCell((x,y)->{
                switch(solgrid.getBlock("GRID")[x][y].charAt(0)) {
                    case '@': b.setCellState(x,y,CellState.WALL); break;
                    case '.': b.setCellState(x,y,CellState.PATH); break;
                    default: throw new RuntimeException("Unknown char in Solution Grid");
                }
            });
        } else {
	        // actual solver was never completed.
            Solver s = new Solver(b);
        }

        GridGraph gg = new GridGraph(new MyGraphReference(b));
	    StringBuffer sb = new StringBuffer();
	    b.forEachCell((x,y)-> {
	        if (b.getCellState(x,y) != CellState.PATH) return;
	        if (!b.hasLetter(x,y)) return;;
	        Point theblock = null;
	        for (Direction d : Direction.orthogonals()) {
	            Point np = d.delta(x,y,1);
	            if (!b.inBounds(np.x,np.y)) continue;
	            if (b.getCellState(np.x,np.y) != CellState.WALL) continue;
	            if (theblock != null) return;
	            theblock = np;
            }
            if (theblock == null) return;
	        sb.append(LetterRotate.Rotate(b.getLetter(x,y),gg.connectedSetOf(theblock).size()));
        });
        lines[0] = sb.toString();
        lines[1] = b.gfr.getVar("SOLUTION");



	    MyListener myl = new MyListener(b,lines);
	    GridFrame gf = new GridFrame("Adalogical Aenigma #90 Solver", 1200,800,myl);
    }


}
