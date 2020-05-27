import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import grid.spring.ListMultiListener;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static class MyListener extends ListMultiListener<Board> implements GridPanel.EdgeListener, GridPanel.GridListener  {
        List<Board> blist;
        String[] lines;

        public MyListener(List<Board> blist, String[] lines) { super(blist); this.lines = lines; }

        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);
        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return b().getRegionId(x,y) == b().getRegionId(x+1,y) ? PATH : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return b().getRegionId(x,y) == b().getRegionId(x,y+1) ? PATH : WALL; }
        @Override public int getNumXCells() { return b().getWidth(); }
        @Override public int getNumYCells() { return b().getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if ( b().getCell(cx,cy) == CellState.SHADED) {
                g.setColor(Color.GREEN);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }
            if (b().getCell(cx,cy) == CellState.UNSHADED) {
                g.setColor(Color.YELLOW);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }
            if (b().hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b().getLetter(cx,cy), Direction.NORTHEAST);
            if (b().hasClue(cx,cy)) GridPanel.DrawStringInCell(bi,Color.BLACK,""+b().getClue(cx,cy));


            return true;
        }

    }

    final static int NOITEM = 0;
    final static int GONEBAD = -1;

    public static void applyToWalls(Board b,Direction d,int sx,int sy,int count,CellContainer<Integer> walls) {
        if (!b.onBoard(sx,sy)) return;
        for (int i = 0 ; i < count ; ++i) {
            Point p = d.delta(sx,sy,i);
            if (walls.getCell(p.x,p.y) == NOITEM) {
                walls.setCell(p.x,p.y,count);
            } else {
                walls.setCell(p.x,p.y,GONEBAD);
            }
        }
    }

    public static void makerectangle(Board b,int ulx,int uly,int lrx,int lry) {
        for (int x = ulx ; x <= lrx ; ++x) {
            for (int y = uly ;  y <= lry ; ++y) {
                b.setCell(x,y,CellState.SHADED);
            }
        }
    }





    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("bad command line");
	        System.exit(1);
        }
        Board b = new Board(args[0]);
        String[] lines = new String[] { "Adalogical Aenigma", "#79 Solver"};




        Solver s = new Solver(b);
        s.Solve(b);

        List<Board> boards = new ArrayList<>();
        boards.add(b);


        System.out.println("# of Solutions: " + s.GetSolutions().size());
        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);
            Board fb = b;

            CellContainer<Integer> walls = new CellContainer<Integer>(b.getWidth(), b.getHeight(), (x, y) -> NOITEM);
            List<FillPartialRectangleLogicStep.Extent> extents = FillPartialRectangleLogicStep.getExtents(b);

            for (FillPartialRectangleLogicStep.Extent extent : extents) {
                applyToWalls(b,Direction.EAST,extent.minx,extent.miny-1,extent.maxx-extent.minx+1,walls);
                applyToWalls(b,Direction.EAST,extent.minx,extent.maxy+1,extent.maxx-extent.minx+1,walls);
                applyToWalls(b,Direction.SOUTH,extent.minx-1,extent.miny,extent.maxy-extent.miny+1,walls);
                applyToWalls(b,Direction.SOUTH,extent.maxx+1,extent.miny,extent.maxy-extent.miny+1,walls);
            }

            StringBuffer sb = new StringBuffer();
            walls.forEachCell((x,y)-> {
                if (walls.getCell(x,y) == GONEBAD) return;
                if (walls.getCell(x,y) == NOITEM) return;
                if (!fb.hasLetter(x,y)) return;
                sb.append(LetterRotate.Rotate(fb.getLetter(x,y),walls.getCell(x,y)));
            });
            lines[0] = sb.toString();
            lines[1] = b.gfr.getVar("SOLUTION");
            boards = s.GetSolutions();
        }


	    MyListener myl = new MyListener(boards,lines);
	    GridFrame gf = new GridFrame("Adalogical Aenigma #79 Solver",1200,800,myl,myl);
    }


}
