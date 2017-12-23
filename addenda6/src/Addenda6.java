import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;

public class Addenda6
{
    private static class MyListener implements GridPanel.GridListener
    {
        Board b;
        public MyListener(Board b) { this.b = b; }

        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (b.getCell(cx,cy) == CellState.RIVER)
            {
                g.setColor(Color.CYAN);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.getCell(cx,cy) == CellState.LAND)
            {
                g.setColor(Color.green);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }



            GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getLetter(cx,cy));
            for (Direction d: Direction.diagonals())
            {
                int val = b.getCorner(cx,cy,d);
                if (val == -1) continue;
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+val,d);
            }

            return true;
        }
    }


    public static void main(String[] args)
    {
        LogicBoard b = new LogicBoard("Addenda6.txt");
        Solver s = new Solver(b);

        s.Solve(b);
        System.out.println("# of solutions: " + s.GetSolutions().size());
        b = s.GetSolutions().get(0);

        for (int y = 0 ; y < b.getHeight() ; ++y)
        {
            for (int x = 0 ; x < b.getWidth() ; ++x)
            {
                if (b.getCell(x,y) != CellState.RIVER) continue;
                boolean doPrint = false;

                for (Direction d : Direction.diagonals())
                {
                    Point cp = b.getCornerPoint(x,y,d);
                    if (b.getCorner(cp.x,cp.y) != -1) continue;
                    // if a corner of this cell has 3 adjacent lands, this is a printable.
                    int landcount = 0;
                    for (Point p : b.getAdjacents(cp.x,cp.y))
                    {
                        if (b.getCell(p.x,p.y) == CellState.LAND) ++landcount;
                    }
                    if (landcount == 3) doPrint = true;
                }

                if (doPrint) System.out.print(b.getLetter(x,y));
            }
        }
        System.out.println("");






        GridFrame gf = new GridFrame("Addenda #6 Solver",1200,800,new MyListener(b));
    }


}
