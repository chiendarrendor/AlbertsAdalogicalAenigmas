import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class Ada43
{
    public static class MyListener implements GridPanel.GridListener
    {
        private Board b;
        private BranchingPath bp;
        public MyListener(Board b
                          ,BranchingPath bp
        )
        {
            this.b = b;
            this.bp = bp;
        }

        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        public boolean drawCellContents(int cx,int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            switch (b.getCell(cx,cy))
            {
                case BRICK:
                    g.setColor(Color.red);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                    break;
                case FLOWERS:
                    g.setColor(Color.green);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                    break;
                case UNKNOWN:
                    break;
            }

            if (bp.hasPoint(new Point(cx,cy)))
            {
                g.setColor(Color.yellow);
                g.drawOval(0,0,bi.getWidth(),bi.getHeight());
            }


            GridPanel.DrawStringInCell(bi, Color.black, "" + b.getLetter(cx, cy));

            return true;
        }
    }

    public static class MyEdgeListener implements GridPanel.EdgeListener
    {
        Board b;
        public MyEdgeListener(Board b) { this.b = b; }

        public EdgeDescriptor onBoundary() { return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,5);}

        public EdgeDescriptor toEast(int x,int y)
        {
            return new EdgeDescriptor(Color.black, b.getRegionId(x,y) == b.getRegionId(x+1,y) ? 1 : 5);
        }

        public EdgeDescriptor toSouth(int x,int y)
        {
            return new EdgeDescriptor(Color.black, b.getRegionId(x,y) == b.getRegionId(x,y+1) ? 1 : 5);
        }
    }

    public static void walkSolution(Board b,int x,int y)
    {
        System.out.println("Starting from ("+x+","+y+")");
        BranchingPathProcessor bpp = new BranchingPathProcessor(new Point(x,y),b);
        BranchingPath bestPath = bpp.bestSolution;
        System.out.println("Longest Path: Length " + bestPath.length() + ", depth "+bestPath.depth());
        bestPath.walkPath(b);
    }

    public static void main(String[] args)
    {
        Board b = new Board("ada43.txt");

        Solver s = new Solver(b);

        System.out.println("# of Solutions: " + s.GetSolutions().size());
        b = s.GetSolutions().get(0);

        for (int y = 0 ; y < b.getHeight() ; ++y)
        {
            for (int x = 0 ; x < b.getWidth() ; ++x)
            {
                switch (b.getCell(x,y))
                {
                    case BRICK: System.out.print("B"); break;
                    case FLOWERS: System.out.print("F"); break;
                    case UNKNOWN: System.out.print("U"); break;
                }
            }
            System.out.println("");
        }

        walkSolution(b,0,6);
        walkSolution(b,17,0);
        walkSolution(b,20,2);
        walkSolution(b,14,11);
        walkSolution(b,20,9);


        BranchingPathProcessor bpp = new BranchingPathProcessor(new Point(0,6),b);
        BranchingPath bestPath = bpp.bestSolution;

        new GridFrame("Adalogical Aenigma #43", 1300, 768,
                new MyListener(b,bestPath), new MyEdgeListener(b));


    }
}
