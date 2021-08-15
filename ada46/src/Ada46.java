import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Ada46
{
    private static class MyListener implements GridPanel.GridListener, GridPanel.EdgeListener
    {
        Board b;
        String[] lines;

        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }

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
        @Override public String[] getAnswerLines() { return lines; }

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

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }


        LogicBoard b = new LogicBoard(args[0]);

        Solver s = new Solver(b);
        String[] lines = new String[] { "Adalogical Aenigma" , "#46 Solver"};

        s.Solve(b);

        System.out.println("# of Solutions: " + s.GetSolutions().size());

        for (int i = 1 ; i < 8 ; ++i)
        {
            System.out.println(SolutionScanner.scan(b,i,true,true));
            System.out.println(SolutionScanner.scan(b,i,true,false));
        }

        lines[0] = SolutionScanner.scan(b,3,true,true);
        lines[1] = b.gfr.getVar("SOLUTION");



        MyListener myl = new MyListener(b,lines);


        GridFrame gf = new GridFrame("Aenigma #46 solver",1200,800,myl,myl);


    }


}
