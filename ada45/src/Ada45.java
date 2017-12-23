import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Ada45
{

    private static class MyListener implements GridPanel.GridListener
    {
        private Board b;
        private SolutionOverlay so;

        public MyListener(Board b, SolutionOverlay so)
        {
            this.b = b;this.so = so;
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

        public boolean drawCellContents(int cx,int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            if (b.hasLetter(cx,cy))
            {
                GridPanel.DrawStringUpperLeftCell(bi,Color.black,""+b.getLetter(cx,cy));
            }

            if (b.isRabbit(cx,cy))
            {
                g.setColor(Color.green);
                g.fillOval(0,0,bi.getWidth(),bi.getHeight());
                GridPanel.DrawStringInCell(bi,Color.black,""+b.getRabbitSize(cx,cy));
            }

            if (b.inRegion(cx,cy))
            {
                g.setColor(Color.red);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                char rid = b.getRegionId(cx,cy);
                int rsize = b.getRegionSize(rid);

                String s = rsize < 0 ? "--" : (""+rsize);
                GridPanel.DrawStringInCell(bi,Color.black,s);
            }

            g.setColor(Color.BLUE);

            if (so.isTerminal[cx][cy])
            {
                g.drawOval(0,0,bi.getWidth(),bi.getHeight());
            }

            if (so.isNorth[cx][cy])
            {
                g.drawLine(bi.getWidth()/2,0,bi.getWidth()/2,bi.getHeight()/2);
            }

            if (so.isSouth[cx][cy])
            {
                g.drawLine(bi.getWidth()/2,bi.getHeight()/2,bi.getWidth()/2,bi.getHeight());
            }

            if (so.isEast[cx][cy])
            {
                g.drawLine(bi.getWidth()/2,bi.getHeight()/2,bi.getWidth(),bi.getHeight()/2);
            }

            if (so.isWest[cx][cy])
            {
                g.drawLine(0,bi.getHeight()/2,bi.getWidth()/2,bi.getHeight()/2);
            }




            return true;
        }
    }

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Bad Command Line");
            System.exit(1);
        }
        Board b = new Board(args[0]);

        Solver s = new Solver(b);
        s.Solve(b);
        System.out.println("# of solutions: " + s.GetSolutions().size());

        Board solution = s.GetSolutions().get(0);
        SolutionOverlay so = new SolutionOverlay(solution);


        new GridFrame("Ada-44 Solver",1300,768,new MyListener(solution,so));
    }
}
