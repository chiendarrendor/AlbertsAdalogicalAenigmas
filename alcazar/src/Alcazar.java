import grid.logic.flatten.FlattenLogicer;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class Alcazar
{
    private static class MyGridListener implements GridPanel.GridListener
    {
        Board b;

        public MyGridListener(Board b)
        {
            this.b = b;
        }
        public MyGridListener() { this.b = null; }

        public int getNumXCells() { return b.getWidth(); }
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
            return false;
        }
        public boolean drawBoundary()
        {
            return false;
        }

        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Board.CellInfo ci = b.getCI(cx,cy);
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if ((cx+cy) % 2 == 0)
            {
                g.setColor(Color.WHITE);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (ci.isOptional)
            {
                g.setColor(Color.YELLOW);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            int RADIUS = 10;
            for (Direction dir : Direction.orthogonals())
            {
                Board.CellInfo aci = ci.getAdjacent(dir);

                if (aci == null || b.isOutside(aci))
                {
                    g.setColor(Color.BLACK);
                    switch(dir)
                    {
                        case NORTH: g.fillRect(0,0,bi.getWidth(),RADIUS); break;
                        case SOUTH: g.fillRect(0,bi.getHeight()-RADIUS,bi.getWidth(),RADIUS); break;
                        case WEST: g.fillRect(0,0,RADIUS,bi.getHeight()); break;
                        case EAST: g.fillRect(bi.getWidth()-RADIUS,0,RADIUS,bi.getHeight()); break;
                    }
                }

                if (aci != null && b.isOutside(aci))
                {
                    g.setColor(Color.black);
                    int dcx = -1;
                    int dcy = -1;
                    switch(dir)
                    {
                        case NORTH: dcx = bi.getWidth()/2; dcy = RADIUS; break;
                        case SOUTH: dcx = bi.getWidth()/2; dcy = bi.getHeight()-RADIUS; break;
                        case EAST: dcx = bi.getWidth()-RADIUS ; dcy = bi.getHeight()/2; break;
                        case WEST: dcx = RADIUS; dcy = bi.getHeight()/2; break;
                    }

                    g.fillOval(dcx-RADIUS,dcy-RADIUS,RADIUS*2,RADIUS*2);
                }

                EdgeState es = b.getEdge(cx,cy,dir);
                if(es == EdgeState.PATH)
                {
                    g.setColor(Color.green);
                    g.setStroke(new BasicStroke(5));
                    switch(dir)
                    {
                        case NORTH: g.drawLine(bi.getWidth()/2,bi.getHeight()/2,bi.getWidth()/2,0); break;
                        case SOUTH: g.drawLine(bi.getWidth()/2,bi.getHeight()/2,bi.getWidth()/2,bi.getHeight()); break;
                        case WEST: g.drawLine(bi.getWidth()/2,bi.getHeight()/2,0,bi.getHeight()/2); break;
                        case EAST: g.drawLine(bi.getWidth()/2,bi.getHeight()/2,bi.getWidth(),bi.getHeight()/2); break;
                    }
                }
                int INSET = 15;
                int INDENT = 15;
                int w = bi.getWidth();
                int h = bi.getHeight();


                if (es == EdgeState.WALL && aci != null)
                {
                    g.setColor(Color.RED);
                    g.setStroke(new BasicStroke(5));
                    switch(dir)
                    {
                        case NORTH: g.drawLine(0+INDENT,0+INSET,w-INDENT,0+INSET); break;
                        case SOUTH: g.drawLine(0+INDENT,h-INSET, w-INDENT,h-INSET); break;
                        case WEST: g.drawLine(0+INSET,0+INDENT,0+INSET,h-INDENT); break;
                        case EAST: g.drawLine(w-INSET,0+INDENT,w-INSET,h-INDENT); break;
                    }
                }




            }
            return true;
        }
    }

    public static class MyMultiListener extends MyGridListener implements GridPanel.MultiGridListener
    {
        List<LogicBoard> solutions;
        int min = 0;
        int max;
        int cur = 0;

        public MyMultiListener(List<LogicBoard> solutions)
        {
            this.solutions = solutions;
            max = this.solutions.size();
            setBoard();
        }

        private void setBoard()
        {
            b = solutions.get(cur).getBoard();
        }

        @Override
        public boolean hasNext()
        {
            return cur < max-1;
        }

        @Override
        public void moveToNext()
        {
            ++cur;
            setBoard();
        }

        @Override
        public boolean hasPrev()
        {
            return cur > min;
        }

        @Override
        public void moveToPrev()
        {
            --cur;
            setBoard();
        }


    }




    public static void main(String[] args)
    {
        Board b = new Board(args[0]);
        LogicBoard lb = new LogicBoard(b);
        Solver s = new Solver(lb);


        try
        {
            s.Solve(lb);
            System.out.println("Solution count: " + s.GetSolutions().size());
        }
        catch (AlcazarRuntimeException are)
        {
            System.out.println(are);
            b = are.b;

            for (PathManager.Path path : b.getPathManager().getAllActivePaths())
            {
                System.out.println(path.toString());
            }
        }

        if (s.GetSolutions().size() == 0)
        {
            System.out.println("no solutions");
            System.exit(0);
        }


//	    GridFrame gf = new GridFrame("Alcazar Solver",700,1000,new MyGridListener(b));
        GridFrame gf = new GridFrame("Alcazar Solver",700,1000,new MyMultiListener(s.GetSolutions()));


    }


}
