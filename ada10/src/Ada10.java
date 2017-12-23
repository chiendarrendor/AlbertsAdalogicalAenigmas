
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

public class Ada10
{
    public static class MyListener implements GridPanel.GridListener
    {
        private Board theBoard;
        public MyListener(Board theBoard) { this.theBoard = theBoard; }

        public int getNumXCells() { return theBoard.getWidth(); }
        public int getNumYCells() { return theBoard.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        public boolean drawCellContents(int cx,int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            switch(theBoard.getCell(cx,cy))
            {
                case TREE:
                    g.setColor(Color.green);
                    g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
                    break;
                case EMPTY:
                    g.setColor(Color.yellow);
                    g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
                    break;
            }

            if (theBoard.onPath(cx,cy))
            {
                g.setColor(Color.red);
                int itemsize = 10;
                int cenx = bi.getWidth()/2;
                int ceny = bi.getHeight()/2;

                g.fillRect(cenx-itemsize/2,ceny-itemsize/2,itemsize,itemsize);
            }



            if (theBoard.hasLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi,Color.black,""+theBoard.getLetter(cx,cy));
            if (theBoard.hasNumber(cx,cy)) GridPanel.DrawStringInCell(bi,Color.black,""+theBoard.getNumber(cx,cy));

            return true;
        }
    }

    public static class MyEdgeListener implements GridPanel.EdgeListener
    {
        Board theBoard;
        public MyEdgeListener(Board theBoard) { this.theBoard = theBoard;}
        
        public EdgeDescriptor onBoundary() { return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,5);}

        public EdgeDescriptor toEast(int x,int y)
        {
            return new EdgeDescriptor(Color.black, theBoard.getRegionId(x,y) == theBoard.getRegionId(x+1,y) ? 1 : 5);
        }

        public EdgeDescriptor toSouth(int x,int y)
        {
            return new EdgeDescriptor(Color.black, theBoard.getRegionId(x,y) == theBoard.getRegionId(x,y+1) ? 1 : 5);
        }       
    }
    
    



    public static void main(String[] args)
    {
	    if (args.length != 1)
        {
            System.err.println("Bad Command Line");
            System.exit(1);
        }
        GridFileReader gfr = new GridFileReader(args[0]);
	    Board mb = new Board(gfr);



	    Solver solver = new Solver(mb);
	    List<Board> solutions = solver.GetSolutions();
	    System.out.println("# of solutions: " + solutions.size());



        AStar astar = new AStar(solutions);
        System.out.println("# of paths of length " + astar.reslength + ": " + astar.results.size());

        Regions reg = new Regions(mb);
        int minregions = 1000;
        for (AStarPath asp : astar.results)
        {
            int numnum = asp.numberedRegionCount(reg);
            if (numnum < minregions) minregions = numnum;
        }

        System.out.println("minimal # of numbered regions traversed: " + minregions);

        Vector<AStarPath> prune1 = new Vector<>();
        for (AStarPath asp : astar.results)
        {
            if (asp.numberedRegionCount(reg) == minregions)
            {
                prune1.add(asp);
            }
        }

        System.out.println("# of paths minimizing traversal of numbered regions: " + prune1.size());
        // so we know that all these paths have the same length.
        List<AStarPath> prune2 = TurnDeterminer.run(prune1);

        System.out.println("# of paths making turns as late as possible: " + prune2.size());


        Board aSolution = null;

        for (AStarPath asp : prune2)
        {
            Board b = asp.b;
            StringBuffer sb = new StringBuffer();
            for (Point p : asp.path)
            {
                if (b.hasLetter(p.x,p.y))
                {
                    Region r = reg.get(b.getRegionId(p.x,p.y));
                    int delta = r.TreeCount(b);
                    sb.append(LetterRotate.Rotate(b.getLetter(p.x,p.y),delta));
                }
            }
            String res = sb.toString();
            System.out.println("Solution: " + res);
            aSolution = b;
            aSolution.setPath(asp.path);
        }

        GridFrame gridFrame = new GridFrame("Adalogical Aenigma #10", 1300, 768,
                new MyListener(aSolution),new MyEdgeListener(aSolution));
    }
}
