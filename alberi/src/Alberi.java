import grid.spring.GridFrame;
import grid.spring.GridPanel;
import javafx.scene.control.Cell;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Alberi
{
    private static class MyListener implements GridPanel.GridListener
    {
        private Board b;
        public MyListener(Board b) { this.b = b; }

        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        public boolean drawCellContents(int cx,int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            if (b.getCell(cx,cy) == CellState.GRASS)
            {
                g.setColor(Color.darkGray);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }
            if (b.getCell(cx,cy) == CellState.TREE)
            {
                g.setColor(Color.green);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            return true;
        }
    }

    private static class MyEdgeListener implements GridPanel.EdgeListener
    {
        private Board myBoard = null;
        public MyEdgeListener(Board b) { myBoard = b; }

        public GridPanel.EdgeListener.EdgeDescriptor onBoundary()
        {
            return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,5);
        }

        public GridPanel.EdgeListener.EdgeDescriptor toEast(int x,int y)
        {
            int w = myBoard.getRegionId(x,y) == myBoard.getRegionId(x+1,y) ? 1 : 5;
            return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,w);
        }

        public GridPanel.EdgeListener.EdgeDescriptor toSouth(int x,int y)
        {
            int w = myBoard.getRegionId(x,y) == myBoard.getRegionId(x,y+1) ? 1 : 5;
            return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,w);
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 1) throw new RuntimeException("Bad Command Line");
        Board b = new Board(args[0]);

        //b.setCellTree(1,1);
        //new NoAdjacentLogicStep().apply(b);


        Solver s = new Solver(b);
        s.Solve(new Board(b));
        System.out.println("Solution Count: " + s.GetSolutions().size() );
        b = s.GetSolutions().get(0);


        new GridFrame("Alberi Solver",1300,768,new MyListener(b),new MyEdgeListener(b));

    }


}
