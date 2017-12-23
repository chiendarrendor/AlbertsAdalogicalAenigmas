import grid.graph.GridGraph;

import java.awt.*;

/**
 * Created by chien on 6/13/2017.
 */
public class DiagonalGridGraph
{
    BoardCore b;
    GridGraph gg;
    boolean useLetters;

    private class MyReference implements GridGraph.GridReference
    {
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isIncludedCell(int x, int y)
        {
            if (useLetters)
            {
                return !b.getLetter(x,y).equals(".");
            }
            else
            {
                return b.getCount(x, y) > 0;
            }
        }
        public boolean edgeExitsEast(int x, int y)
        {
            return ! b.isBlocked(x,y,Direction.EAST);
        }

        public boolean edgeExitsSouth(int x, int y)
        {
            return ! b.isBlocked(x,y,Direction.SOUTH);
        }
    }

    private Direction[] diags = { Direction.SOUTHEAST,Direction.SOUTHWEST};

    public DiagonalGridGraph(BoardCore b)
    {
        this(b,false);
    }



    public DiagonalGridGraph(BoardCore b,boolean useLetters)
    {
        this.useLetters = useLetters;
        this.b = b;
        this.gg = new GridGraph(new MyReference());

        for (int x = 0 ; x < b.getWidth() ; ++x)
        {
            for(int y = 0 ; y < b.getHeight() ; ++y)
            {
                for (Direction dir : diags)
                {
                    if (b.isBlocked(x,y,dir)) continue;
                    Point p = new Point(x,y);
                    Point op = new Point(x+dir.DX(),y+dir.DY());
                    gg.addEdge(p,op);
                }
            }
        }
    }

    public boolean isConnected() { return gg.isConnected(); }
}
