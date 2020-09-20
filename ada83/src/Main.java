import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import grid.spring.ListMultiListener;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static class MyListener extends ListMultiListener<Board> implements GridPanel.EdgeListener, GridPanel.GridListener {

        String[] lines;


        public MyListener(List<Board> boards, String[] lines) { super(boards);  this.lines = lines; }
        @Override public int getNumXCells() { return b().getWidth(); }
        @Override public int getNumYCells() { return b().getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);
        private boolean sameRegion(int x,int y,Direction d) {
            Point op = d.delta(x,y,1);
            return b().getRegionId(x,y) == b().getRegionId(op.x,op.y);
        }



        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return sameRegion(x,y,Direction.EAST) ? PATH : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return sameRegion(x,y,Direction.SOUTH) ? PATH : WALL; }


        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            if (b().isClue(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.RED,""+b().getClue(cx,cy),Direction.NORTHEAST);
            if (b().isLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b().getLetter(cx,cy),Direction.NORTHWEST);

            for (Direction d : Direction.orthogonals()) {
                switch(b().getEdge(cx,cy,d)) {
                    case PATH:
                        GridPanel.DrawStringInCorner(bi,Color.GREEN,"@",d);
                        break;
                    case WALL:
                        GridPanel.DrawStringInCorner(bi,Color.RED,"X",d);
                        break;
                    case UNKNOWN:
                        break;
                }
            }


            return true;
        }
    }


    public static void main(String[] args) {
        if (args.length  != 1) {
            System.out.println("Bad command line");
        }

        Board b = new Board(args[0]);
        String[] lines = new String[] { "Adological" , "Aenigma" };

        Solver s = new Solver(b);

        s.Solve(b);
        System.out.println("# of solutions: " + s.GetSolutions().size());

        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);

            StringBuffer sb = new StringBuffer();
            Board fb = b;
            fb.forEachCell((x,y)->{
                if (!fb.isLetter(x,y)) return;
                Board.CellProcessor cp = fb.processCell(x,y,CellType.UNKNOWN);
                if (cp.ct != CellType.INTERNAL) return;
                int size = fb.getPathContainer().getCell(x,y).getInternalPaths().get(0).size();
                sb.append(LetterRotate.Rotate(fb.getLetter(x,y),size));
            });
            lines[0] = sb.toString();
            lines[1] = fb.gfr.getVar("SOLUTION");



        }




        MyListener myl = new MyListener(s.GetSolutions(),lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #83 Solver",1200,800,myl,myl);
    }


}
