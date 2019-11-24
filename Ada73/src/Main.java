import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class Main {

    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] text;
        public MyListener(Board b, String[] text) { this.b = b; this.text = text; }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }
        public String[] getAnswerLines() { return text; }

        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,0);
        private static EdgeDescriptor UNKNOWN = new EdgeDescriptor(Color.BLACK,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }

        private EdgeDescriptor toDirection(int x,int y,Direction d) {
            Point ocell = d.delta(x,y,1);
            Set<Tile> ourset = b.getCell(x,y).set;
            Set<Tile> otherset = b.getCell(ocell.x,ocell.y).set;

            if (ourset.size() == 1 && otherset.size() == 1 && ourset.iterator().next() == otherset.iterator().next()) {
                return PATH;
            }

            Set<Tile> tset = new HashSet<>();
            tset.addAll(ourset);
            tset.retainAll(otherset);

            if (tset.size() == 0) return WALL;



            return UNKNOWN;
        }


        @Override public EdgeDescriptor toEast(int x, int y) { return toDirection(x,y,Direction.EAST); }
        @Override public EdgeDescriptor toSouth(int x, int y) { return toDirection(x,y,Direction.SOUTH); }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            }
            if (b.hasNumber(cx,cy)) {
                GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getNumber(cx,cy));
            }

            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getCell(cx,cy).set.size(),Direction.SOUTHEAST);

            return true;
        }


    }

    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad command line\n");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
	    Solver s = new Solver(b);

	    s.Solve(b);
	    System.out.println("# of Solutions: " + s.GetSolutions().size());

	    b = s.GetSolutions().get(0);
	    StringBuffer sb = new StringBuffer();
        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0 ; x < b.getWidth() ; ++x) {
                Tile t = b.getCell(x,y).set.iterator().next();
                if (b.isNumbered(t)) continue;
                if (t.size() % 2 == 0) continue;
                sb.append(LetterRotate.Rotate(b.getLetter(x,y),t.size()));
            }
        }





	    String[] text = new String[] { sb.toString(),b.gfr.getVar("SOLUTION")};
        MyListener myl = new MyListener(b,text);
        GridFrame gf = new GridFrame("Adalogical Aenigma #73 Solver",1200,800,myl,myl);
    }


}
