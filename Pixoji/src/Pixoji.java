import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Pixoji
{
    private static class MyGridListener implements GridPanel.GridListener {
        Board b;
        public MyGridListener(Board b) { this.b = b; }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        private void DrawInCell(BufferedImage bi,Clue clue)
        {
            for (int i = 0 ; i < clue.stringCount() ; ++i) GridPanel.DrawStringInCell(bi,clue.stringColor(i),clue.string(i));
        }

        private void DrawInCorner(BufferedImage bi,Clue clue,Direction d)
        {
            for (int i = 0 ; i < clue.stringCount() ; ++i) GridPanel.DrawStringInCorner(bi,clue.stringColor(i),clue.string(i),d);
        }



        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.getCell(cx,cy) == CellType.BLACK)
            {
                g.setColor(Color.GREEN);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.getCell(cx,cy) == CellType.WHITE)
            {
                g.setColor(Color.WHITE);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.getRegionId(cx,cy) != '.')
            {
                g.setColor(Color.red);
                g.fillRect(0,0,20,20);
                GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+b.getRegionId(cx,cy));
            }

            if (cx == 0 && b.getLeftClue(cy) != null) DrawInCorner(bi,b.getLeftClue(cy),Direction.SOUTHWEST);
            if (cy == 0 && b.getTopClue(cx) != null) DrawInCorner(bi,b.getTopClue(cx),Direction.NORTHEAST);
            if (b.getClue(cx,cy) != null) DrawInCell(bi,b.getClue(cx,cy));


            return true;
        }
    }

    private static class MyEdgeListener implements GridPanel.EdgeListener
    {
        Board b;
        public MyEdgeListener(Board b) { this.b = b; }
        public EdgeDescriptor onBoundary() { return new EdgeDescriptor(Color.BLACK,5); }
        public EdgeDescriptor toEast(int x, int y) { return new EdgeDescriptor(Color.black,b.getRegionId(x,y) == b.getRegionId(x+1,y) ? 1 : 5); }
        public EdgeDescriptor toSouth(int x, int y) { return new EdgeDescriptor(Color.black,b.getRegionId(x,y) == b.getRegionId(x,y+1) ? 1 : 5); }
    }


    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("Bad command line");
            System.exit(1);
        }
        Board b = new Board(args[0]);


        Solver s = new Solver(b);
        s.Solve(b);



        if (s.GetSolutions().size() == 0)
        {
            System.out.println("No solutions :-(");
        }

        if (s.GetSolutions().size() >= 1)
        {
           if (s.GetSolutions().size() > 1) System.out.println("Multiple solutions! Picking one of " + s.GetSolutions().size());
           b = s.GetSolutions().get(0);
        }


        GridFrame gf = new GridFrame("Pixoji Solver " + args[0],700,700,new MyGridListener(b),new MyEdgeListener(b));
    }
}
