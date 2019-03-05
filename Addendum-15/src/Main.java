import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.logic.flatten.FlattenLogicer;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static class MyGridListener implements GridPanel.MultiGridListener {
        List<Board> bar;
        int curidx = 0;
        String[] lines;
        MyEdgeListener mel;

        public MyGridListener(List<Board> bar, String[] result) { this.bar = bar; lines = result; mel = new MyEdgeListener(); }

        private Board b() { return bar.get(curidx); }
        public MyEdgeListener getMel() {
            return mel;
        }

        @Override public int getNumXCells() { return b().getWidth(); }
        @Override public int getNumYCells() { return b().getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (b().isGray(cx,cy)) {
                g.setColor(Color.PINK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            Region r = b().getRegionByCell(cx,cy);
            if (r == null) return true;

            g.setColor(r.isDone() ? Color.GREEN : Color.BLACK);
            g.setFont(g.getFont().deriveFont(14.0f));
            String content = "" + r.getSize() + ( r.getActualSize() == -1 ? "" : "(" + r.getActualSize() + ")");
            GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getWidth(), content);

            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+r.getId(), Direction.SOUTHWEST);

            return true;
        }

        @Override public boolean hasNext() { return curidx < bar.size() - 1; }
        @Override public void moveToNext() { ++curidx; }
        @Override public boolean hasPrev() { return curidx > 0; }
        @Override public void moveToPrev() { --curidx; }



        public class MyEdgeListener implements GridPanel.EdgeListener {
            private final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
            private final EdgeDescriptor INTL = new EdgeDescriptor(Color.GREEN,1);
            private final EdgeDescriptor UNKN = new EdgeDescriptor(Color.BLACK,1);

            @Override public EdgeDescriptor onBoundary() { return WALL; }

            private EdgeDescriptor adjCell(int x,int y, int ox,int oy) {
                if (b().getRegionByCell(x,y) == null && b().getRegionByCell(ox,oy) == null) return UNKN;
                if (b().getRegionByCell(x,y) == b().getRegionByCell( ox,oy)) return INTL;

                if (b().getRegionByCell(x,y) != null)
                    return b().canExtendInto(b().getRegionByCell(x,y).getId(),new Point(ox,oy)) ? UNKN : WALL;
                return b().canExtendInto(b().getRegionByCell(ox,oy).getId(),new Point(x,y)) ? UNKN : WALL;
            }



            @Override public EdgeDescriptor toEast(int x, int y) {
                return adjCell(x,y,x+1,y);
            }

            @Override public EdgeDescriptor toSouth(int x, int y) {
                return adjCell(x,y,x,y+1);
            }
        }

    }





    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad command line");
            System.exit(1);
        }
        Board b = new Board(args[0]);
        FlattenLogicer<Board> s = new Solver(b);
        List<Board> bar = new ArrayList<>();

        try {
            s.Solve(b);
        } catch (UnhandledEmptyException uhe) {
            System.out.println("UnhandledEmptyException Caught");
            bar.add(uhe.b);
        }

        System.out.println("# of solutions: " + s.GetSolutions().size());

        if (s.GetSolutions().size() != 1) {
            System.out.println("Non-singular solution...exiting...");
            System.exit(1);
        }

        Board fb = s.GetSolutions().get(0);
        bar.add(fb);

        StringBuffer solution = new StringBuffer();
        String raw = fb.gfr.getVar("LETTERS");

        for (int x = 0 ; x < fb.getWidth() ; ++x) {
            int ctr = 0;
            for (int y = 0 ; y < fb.getHeight() ; ++y) {
                if (!fb.isGray(x,y)) continue;
                ctr += fb.getRegionByCell(x,y).getActualSize();
            }
            solution.append(LetterRotate.Rotate(raw.charAt(x),ctr));
        }






        String[] result = new String[] { solution.toString(),b.gfr.getVar("SOLUTION")};

        MyGridListener mgl = new MyGridListener(bar,result);
        GridFrame gf = new GridFrame("Adalogical Addenda #15 Solver",1200,800,mgl,mgl.getMel());
    }


}
