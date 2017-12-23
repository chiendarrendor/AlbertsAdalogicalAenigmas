import grid.letter.LetterRotate;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Ada44
{
    private static class MyListener implements GridPanel.GridListener
    {
        private BoardCore b;

        public MyListener(BoardCore b)
        {
            this.b = b;
        }

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

        private double EIGHTH = Math.PI / 4.0;

        private double rotation(Direction dir)
        {
            switch (dir)
            {
                case NORTH:
                    return 0 * EIGHTH;
                case NORTHEAST:
                    return 1 * EIGHTH;
                case EAST:
                    return 2 * EIGHTH;
                case SOUTHEAST:
                    return 3 * EIGHTH;
                case SOUTH:
                    return 4 * EIGHTH;
                case SOUTHWEST:
                    return 5 * EIGHTH;
                case WEST:
                    return 6 * EIGHTH;
                case NORTHWEST:
                    return 7 * EIGHTH;
            }
            throw new RuntimeException("How did we get here?");
        }


        private int HALFWIDTH = 15;
        private int RADIUS = 30;

        private void drawBlocker(Direction dir, Graphics2D g, int cw, int ch)
        {
            AffineTransform oldat = g.getTransform();
            AffineTransform center = AffineTransform.getTranslateInstance(cw / 2, ch / 2);
            AffineTransform offset = AffineTransform.getTranslateInstance(0, -RADIUS);
            AffineTransform rotate = AffineTransform.getRotateInstance(rotation(dir));

            center.concatenate(rotate);
            center.concatenate(offset);
            g.setTransform(center);
            g.setColor(Color.RED);
            g.drawLine(-HALFWIDTH, 0, HALFWIDTH, 0);
            g.setTransform(oldat);
        }

        private void drawConnector(Direction dir, Graphics2D g, int cw, int ch)
        {
            g.setColor(Color.GREEN);
            int x1 = cw/2;
            int y1 = ch/2;

            int x2 = 0;
            int y2 = 0;

            switch (dir)
            {
                case NORTH: x2 = cw/2; y2 = 0; break;
                case SOUTH: x2 = cw/2; y2 = ch ; break;
                case EAST: x2 = cw ; y2 = ch/2 ; break;
                case WEST: x2 = 0 ; y2 = ch/2 ; break;
                case NORTHEAST: x2 = cw ; y2 = 0; break;
                case SOUTHEAST: x2 = cw ; y2 = ch; break;
                case NORTHWEST: x2 = 0 ; y2 = 0; break;
                case SOUTHWEST: x2 = 0; y2 = ch; break;
            }
            g.drawLine(x1,y1,x2,y2);
        }



        int DOTR = 10;

        public boolean drawCellContents(int cx,int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            for (Direction dir : Direction.values())
            {
                if (b.isBlocked(cx,cy,dir)) drawBlocker(dir,g,bi.getWidth(),bi.getHeight());
                if (b.isLinked(cx,cy,dir)) drawConnector(dir,g,bi.getWidth(),bi.getHeight());
            }

            Point sp = b.getStart();
            Point ep = b.getEnd();
            int cenx = bi.getWidth() / 2;
            int ceny = bi.getHeight() / 2;

            if(cx == sp.x && cy == sp.y)
            {
                g.setColor(Color.WHITE);
                g.fillOval(cenx-DOTR,ceny-DOTR,DOTR*2,DOTR*2);
            }

            if (cx == ep.x && cy == ep.y)
            {
                g.setColor(Color.YELLOW);
                g.fillOval(cenx-DOTR,ceny-DOTR,DOTR*2,DOTR*2);
            }

            if (b.getLetter(cx,cy).equals("."))
            {
                g.setColor(Color.RED);
                g.fillOval(cenx-DOTR,ceny-DOTR,DOTR*2,DOTR*2);
            }

            GridPanel.DrawStringInCell(bi,Color.black,"" + b.getCount(cx,cy));

            if (!b.getLetter(cx,cy).equals(".")) GridPanel.DrawStringUpperLeftCell(bi,Color.black,b.getLetter(cx,cy));



            return true;
        }
    }


    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Bad command line");
            System.exit(1);
        }
        BoardCore bc = new BoardCore(args[0]);
        Board b = new Board(bc);


        Solver s = new Solver(b);
        s.Solve(b);

        BoardCore solvebc = b.getBoardCore();
        BoardCore blockbc = new BoardCore(solvebc);

        for(int x = 0 ; x < blockbc.getWidth() ; ++x)
        {
            for (int y = 0 ; y < blockbc.getHeight() ; ++y)
            {
                for(Direction dir : Direction.values())
                {
                    if (!blockbc.isLinked(x,y,dir)) continue;
                    int ox = x+dir.DX();
                    int oy = y+dir.DY();
                    if (blockbc.getLetter(ox,oy).equals(".") ||
                            blockbc.getLetter(x,y).equals(".")) blockbc.linkToBlock(x,y,dir);
                    else blockbc.linkToUnknown(x,y,dir);
                }
            }
        }

        Board pboard = new Board(blockbc);
        PathSolver ps = new PathSolver(pboard);
        ps.Solve(pboard);


        BoardCore dispbc = blockbc;
        new GridFrame("Ada-44 Solver",1300,768,new MyListener(dispbc));

        Point p = blockbc.getStart();
        Point prev = null;
        int bcount = 0;



        while(true)
        {
            char letter = blockbc.getLetter(p.x,p.y).charAt(0);
            System.out.print(LetterRotate.Rotate(letter,bcount%10));
            if (p.x == blockbc.getEnd().x && p.y == blockbc.getEnd().y) break;

            for (Direction dir : Direction.values())
            {
                if (blockbc.isBlocked(p.x,p.y,dir)) continue;
                Point np = new Point(p.x+dir.DX(),p.y+dir.DY());
                if (prev != null && np.x == prev.x && np.y == prev.y) continue;

                prev = p;
                p = np;
                ++bcount;
                break;
            }




        }

        System.out.println("");





    }
}
