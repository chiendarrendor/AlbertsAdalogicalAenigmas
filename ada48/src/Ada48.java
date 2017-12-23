import grid.letter.LetterRotate;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Ada48
{
    public static void main(String[] args)
    {
	    if (args.length != 1)
        {
            System.out.println("Bad command line");
            System.exit(1);
        }

        Board b = new Board(args[0]);

	    Solver s = new Solver(b);
	    s.Solve(b);
	    System.out.println("Solution size: " + s.GetSolutions().size());

	    for (int y = 0 ; y < b.getHeight() ; ++y)
        {
            for (int x = 0 ; x < b.getWidth() ; ++x)
            {
                char rid = b.getCell(x,y).getPossibles().iterator().next();
                Region reg = b.getRegion(rid);
                Rectangle rect = reg.possibles.iterator().next();

                int extrcount = 0;
                if (x == rect.x || x == rect.getMaxX()-1) ++extrcount;
                if (y == rect.y || y == rect.getMaxY()-1) ++extrcount;
                int mdim = (int) Math.max(rect.getHeight(),rect.getWidth());
                if (extrcount != 1) continue;
                if (b.getLetter(x,y) == '.') continue;
                System.out.print(""+ LetterRotate.Rotate(b.getLetter(x,y),mdim));
            }
        }
        System.out.println("");




        GridFrame gf = new GridFrame("Ada48",1200,900,new MyListener(b),new EdgeListener(b));
    }

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
            if (b.getLetter(cx,cy) != '.') GridPanel.DrawStringUpperLeftCell(bi, Color.BLACK,""+b.getLetter(cx,cy));
            if (b.getFrag(cx,cy) != '.') GridPanel.DrawStringUpperLeftCell(bi, Color.RED,""+b.getFrag(cx,cy));

            Cell cc = b.getCell(cx,cy);
            Color col = cc.isFixed() ? Color.RED : Color.BLUE;
            String str = "";

            for(char poss : cc.getPossibles()) { str += poss; }
            if (str.length() > 4) str = "" + str.length();

            GridPanel.DrawStringInCell(bi,col,str);



            return true;
        }
    }

    private static class EdgeListener implements GridPanel.EdgeListener
    {
        Board b;
        public EdgeListener(Board b)
        {
            this.b = b;
        }

        @Override
        public EdgeDescriptor onBoundary()
        {
            return new EdgeDescriptor(Color.BLACK,5);
        }

        @Override
        public EdgeDescriptor toEast(int x, int y)
        {
            Cell cme = b.getCell(x,y);
            Cell coth = b.getCell(x+1,y);

            if (cme.isFixed() && coth.isFixed() &&
                    cme.getPossibles().iterator().next() != coth.getPossibles().iterator().next())
            {
                return new EdgeDescriptor(Color.black,5);
            }

            return new EdgeDescriptor(Color.black,1);
        }

        @Override
        public EdgeDescriptor toSouth(int x, int y)
        {
            Cell cme = b.getCell(x,y);
            Cell coth = b.getCell(x,y+1);

            if (cme.isFixed() && coth.isFixed() &&
                    cme.getPossibles().iterator().next() != coth.getPossibles().iterator().next())
            {
                return new EdgeDescriptor(Color.black,5);
            }

            return new EdgeDescriptor(Color.black,1);
        }
    }
}
