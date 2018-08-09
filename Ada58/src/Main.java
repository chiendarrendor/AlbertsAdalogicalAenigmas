import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.GridPathCell;
import grid.puzzlebits.Path.GridPathContainer;
import grid.puzzlebits.Path.Path;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static class MyGridListener implements GridPanel.GridListener {
        Board b;
        String[] labels;

        public MyGridListener(Board b,String[] labels) { this.b = b; this.labels = labels; }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        @Override public String getAnswerText() {
            StringBuffer sb = new StringBuffer();
            sb.append("<html><font size=\"5\">");
            Arrays.stream(labels).forEach(line->sb.append(line).append("<br>"));
            sb.append("</font></html>");
            return sb.toString();
        }

        private static final int INSET = 16;
        private static final int DOWNSET = 3;

        public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (b.hasLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi, Color.GRAY,"" + b.getLetter(cx,cy));
            if (b.hasNumber(cx,cy)) {
                if (b.isStart(cx,cy)) {
                    g.setColor(Color.GRAY);
                    g.fillOval(INSET,INSET+DOWNSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                }
                g.setColor(Color.BLACK);
                g.drawOval(INSET,INSET+DOWNSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);

                Font curfont = g.getFont();
                Font newFont = curfont.deriveFont(Font.BOLD,16);
                g.setFont(newFont);
                GridPanel.DrawStringInCell(g,Color.BLACK,0,0,bi.getWidth(),bi.getHeight(),""+b.getNumber(cx,cy));
            }

            for (Direction d : Direction.orthogonals()) {
                switch(b.getEdge(cx,cy,d)) {
                    case WALL: GridPanel.DrawStringInCorner(bi,Color.RED,"X",d); break;
                    case PATH: GridPanel.DrawStringInCorner(bi,Color.GREEN,""+d.getSymbol(),d); break;
                }
            }




            return true;
        }
    }

    private static Path getPath(Board b, Point p) {
        GridPathCell gpc = b.paths.getCell(p.x,p.y);
        if (gpc.getTerminalPaths().size() > 0) return gpc.getTerminalPaths().get(0);
        else if (gpc.getInternalPaths().size() > 0) return gpc.getInternalPaths().get(0);
        return null;
    }


    private static class MyEdgeListener implements GridPanel.EdgeListener {
        Board b;
        EdgeContainer<EdgeState> tight;
        public MyEdgeListener(Board b) {
            this.b = b;
            tight = new EdgeContainer<EdgeState>(b.getWidth(),b.getHeight(),EdgeState.WALL,
                    (x,y,isV)->EdgeState.WALL,
                    (x,y,isV,old)->old);

            for (Path path : b.paths) {
                Point prev = null;
                for (Point p : path) {
                    if (prev != null) {
                        Direction d = Direction.fromTo(prev.x,prev.y,p.x,p.y);
                        tight.setEdge(prev.x,prev.y,d,EdgeState.PATH);
                    }
                    prev = p;
                }
            }
        }

        private EdgeDescriptor toDir(int x, int y, Direction d) {
            return new EdgeDescriptor(Color.BLACK,tight.getEdge(x,y,d) == EdgeState.PATH ? 1 : 5);


        }


        @Override public EdgeDescriptor onBoundary() { return new EdgeDescriptor(Color.BLACK,5); }
        @Override public EdgeDescriptor toEast(int x, int y) { return toDir(x,y,Direction.EAST); }
        @Override public EdgeDescriptor toSouth(int x, int y) { return toDir(x,y,Direction.SOUTH); }
    }


    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
	    Solver s = new Solver(b);
	    s.Solve(b);
	    System.out.println("# of Solutions: " + s.GetSolutions().size());

	    b = s.GetSolutions().get(0);

        Map<Path,String> pathstrings = new HashMap<>();
        for (Path path : b.paths) {
            StringBuffer sb = new StringBuffer();
            Point lp = path.endOne();
            if (b.getNumber(lp.x,lp.y) != b.getLowTerminal()) path.reverse();

            int offcount = 0;
            for (Point p : path) {
                if (b.hasLetter(p.x,p.y)) ++offcount;
            }
            for (Point p : path) {
                if (!b.hasLetter(p.x,p.y)) continue;
                sb.append(LetterRotate.Rotate(b.getLetter(p.x,p.y),offcount));
            }
            pathstrings.put(path,sb.toString());
            System.out.println("Path: " + sb.toString());
        }



	    StringBuffer sb = new StringBuffer();

	    for (int i = 0 ; i < b.getStartCount() ; ++i) {
	        Point sp = b.getStart(i);
	        Path path = getPath(b,sp);
	        sb.append(pathstrings.get(path));
        }





	    String[] stat = new String[] {
	            sb.toString(),
                b.gfr.getVar("SOLUTION"),
                b.gfr.getVar("SOLUTION2"),
                b.gfr.getVar("SOLUTION3")
	    };

	    GridFrame gf = new GridFrame("Ada #58 Solver",1100,800,
                new MyGridListener(b,stat),new MyEdgeListener(b));

    }


}
