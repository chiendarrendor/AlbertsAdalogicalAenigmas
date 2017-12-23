import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Ada46
{
    private static class MyListener implements GridPanel.GridListener
    {
        Board b;

        public MyListener(Board b) { this.b = b; }

        public int getNumXCells()
        {
            return b.getWidth();
        }
        public int getNumYCells()
        {
            return b.getHeight();
        }
        public boolean drawGridNumbers()
        {
            return true;
        }
        public boolean drawGridLines()
        {
            return true;
        }
        public boolean drawBoundary()
        {
            return true;
        }

        @Override
        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi,Color.DARK_GRAY,"" + b.getLetter(cx,cy));
            if (b.hasArrow(cx,cy))
            {
                g.setColor(Color.black);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                GridPanel.DrawStringInCell(bi,Color.WHITE,""+b.getArrow(cx,cy).getSymbol());
            }
            if (b.hasRegion(cx,cy))
            {
                Color c;
                String s;

                Board.IntegerSet is = b.getPossibles(cx, cy);
                if (is.size() == 0) { c = Color.RED; s = "X"; }
                else if (is.size() == 1) { c = Color.BLACK; s = "" + is.getSingular(); }
                else { c = Color.BLUE; s = "?"; }

                GridPanel.DrawStringInCell(bi,c,s);
            }




            return true;
        }
    }

    private static class MyEdgeListener implements GridPanel.EdgeListener
    {
        Board b;
        public MyEdgeListener(Board b)
        {
            this.b = b;
        }

        @Override
        public EdgeDescriptor onBoundary()
        {
            return new EdgeDescriptor(Color.black,5);
        }

        @Override
        public EdgeDescriptor toEast(int x, int y)
        {
            return new EdgeDescriptor(Color.black, b.regionId(x,y) == b.regionId(x+1,y) ? 1 : 5);
        }

        @Override
        public EdgeDescriptor toSouth(int x, int y)
        {
            return new EdgeDescriptor(Color.black, b.regionId(x,y) == b.regionId(x,y+1) ? 1 : 5);
        }
    }


    public static void main(String[] args) {
        LogicBoard b = new LogicBoard("ada46.txt");

        Solver s = new Solver(b);

        s.Solve(b);

        System.out.println("# of Solutions: " + s.GetSolutions().size());

        for (int i = 1 ; i < 8 ; ++i)
        {
            SolutionScanner.scan(b,i,true,true);
            SolutionScanner.scan(b,i,true,false);
        }



        GridFrame gf = new GridFrame("Aenigma #46 solver",1200,800,new MyListener(b),new MyEdgeListener(b));


    }


}
