import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
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
	    String[] lines = new String[] { "Adalogical","Aenigma #48", "Solver"};

	    Solver s = new Solver(b);
	    s.Solve(b);
	    System.out.println("Solution size: " + s.GetSolutions().size());

	    b = s.GetSolutions().get(0);

	    StringBuffer sb = new StringBuffer();
	    for (int y = 0 ; y < b.getHeight() ; ++y)
        {
            for (int x = 0 ; x < b.getWidth() ; ++x)
            {

                if (b.gfr.getVar("CLUETYPE").equals("ADA48")) {
                    char rid = b.getCell(x, y).getPossibles().iterator().next();
                    Region reg = b.getRegion(rid);
                    Rectangle rect = reg.possibles.iterator().next();

                    int extrcount = 0;
                    if (x == rect.x || x == rect.getMaxX() - 1) ++extrcount;
                    if (y == rect.y || y == rect.getMaxY() - 1) ++extrcount;
                    int mdim = (int) Math.max(rect.getHeight(), rect.getWidth());
                    if (extrcount != 1) continue;
                    if (b.getLetter(x, y) == '.') continue;
                    sb.append(LetterRotate.Rotate(b.getLetter(x, y), mdim));
                } else if (b.gfr.getVar("CLUETYPE").equals("ADDENDUM34")) {
                    if (b.getLetter(x,y) == '.') continue;
                    char myrid = b.getCell(x, y).getPossibles().iterator().next();
                    int othercount = 0;
                    for (Direction d : Direction.orthogonals()) {
                        Point np = d.delta(x,y,1);
                        if (!b.inBounds(np)) continue;
                        char nrid = b.getCell(np.x,np.y).getPossibles().iterator().next();
                        if (nrid == myrid) continue;
                        ++othercount;
                    }
                    if (othercount != 2) continue;
                    sb.append(LetterRotate.Rotate(b.getLetter(x,y),2));

                } else {
                    throw new RuntimeException("Unknown/missing CLUETYPE");
                }

            }
        }
        lines[0] = b.gfr.getVar("NAME");
        lines[1] = sb.toString();
	    lines[2] = b.gfr.getVar("SOLUTION");




        GridFrame gf = new GridFrame("Ada48",1200,900,new MyListener(b,lines),new EdgeListener(b));
    }

    private static class MyListener implements GridPanel.GridListener
    {
        Board b;
        String[] lines;
        public MyListener(Board b,String[] lines) { this.b = b; this.lines = lines;}

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
        public String[] getAnswerLines() { return lines; }

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
