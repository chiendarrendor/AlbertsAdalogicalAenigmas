import grid.file.GridFileReader;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by chien on 6/7/2017.
 */
public class BoardCore
{
    GridFileReader gfr;
    private Edges edges[][];
    int termcount = 0;
    Point startp;
    Point endp;

    public BoardCore(String fname)
    {
        gfr = new GridFileReader(fname);
        edges = new Edges[getEdgeWidth()][getEdgeHeight()];
        for (int x = 0 ; x < getEdgeWidth() ; ++x)
        {
            for (int y = 0 ; y < getEdgeHeight() ; ++y)
            {
                if (x%2 == 0 && y%2 == 0)
                {
                    edges[x][y] = Edges.ISLAND;
                }
                else if (x%2 == 1 && y%2 == 1)
                {
                    edges[x][y] = Edges.UNKNOWNANGLE;
                    ++termcount;
                }
                else
                {
                    edges[x][y] = Edges.UNKNOWNLINK;
                    ++termcount;
                }
            }
        }
        startp = new Point(
                Integer.parseInt(gfr.getVar("STARTX")),
                Integer.parseInt(gfr.getVar("STARTY"))
        );
        endp = new Point(
                Integer.parseInt(gfr.getVar("ENDX")),
                Integer.parseInt(gfr.getVar("ENDY"))
        );
    }

    public Point getStart() { return startp; }
    public Point getEnd() { return endp; }


    public BoardCore(BoardCore right)
    {
        gfr = right.gfr;
        edges = new Edges[getEdgeWidth()][getEdgeHeight()];
        termcount = right.termcount;
        startp = right.startp;
        endp = right.endp;

        for (int x = 0 ; x < getEdgeWidth() ; ++x)
        {
            for (int y = 0; y < getEdgeHeight(); ++y)
            {
                edges[x][y] = right.edges[x][y];
            }
        }
    }



    // this class calculates, given a cell (in cell coordinates) and a direction
    // a) the coordinates of the cell in edge coordinates
    // b) whether that direction is off the board
    // c) the coordinates of the edge in edge coordinates
    // d) the coordinates of the adjacent cell in that direction
    // e) if the direction is orthogonal or diagonal
    // f) if diagonal, is the direction a slash or a backslash

    // designed as a reusable class to cut down on memory use.
    // (I expect this will be called A LOT)
    private class DirInfo
    {
        int myx;
        int myy;
        int ex = -1;
        int ey = -1;
        int ox = -1;
        int oy = -1;
        boolean offBoard = true;
        boolean isOrtho = false;
        boolean isSlash = false;
        Edges e = Edges.ISLAND;

        public DirInfo()
        {

        }

        public DirInfo(int x,int y,Direction d)
        {
            set(x,y,d);
        }

        public void set(int x,int y,Direction d)
        {
            // complete reset of all fields.
            ex = -1; ey = -1 ; ox = -1; oy = -1;
            offBoard = true; isOrtho = false; isSlash = false;
            e = Edges.ISLAND;
            // except these two! :-)
            myx = 2*x;
            myy = 2*y;
            int cox = x+d.DX();
            int coy = y+d.DY();
            if (cox < 0 || coy < 0 || cox >= getWidth() || coy >= getHeight()) return;
            // if we're here, the other is valid;
            offBoard = false;
            ox = 2*cox;
            oy = 2*coy;
            ex = myx + d.DX();
            ey = myy + d.DY();
            e = edges[ex][ey];
            switch (d)
            {
                case NORTH:     isOrtho = true;  isSlash = false; break;
                case SOUTH:     isOrtho = true;  isSlash = false; break;
                case EAST:      isOrtho = true;  isSlash = false; break;
                case WEST:      isOrtho = true;  isSlash = false; break;
                case NORTHEAST: isOrtho = false; isSlash = true;  break;
                case SOUTHEAST: isOrtho = false; isSlash = false; break;
                case NORTHWEST: isOrtho = false; isSlash = false; break;
                case SOUTHWEST: isOrtho = false; isSlash = true;  break;
            }
        }

        public void setEdge(Edges ed) { e = edges[ex][ey] = ed; }
    }

    private DirInfo diri = new DirInfo();

    public boolean isBlocked(int x,int y,Direction dir)
    {
        diri.set(x,y,dir);
        if (diri.offBoard) return true;
        if (diri.isOrtho)
        {
            return diri.e == Edges.NOTLINK;
        }
        // if we get here, our edge is not ortho.
        if (diri.e == Edges.NOTANGLE) return true;
        if (diri.isSlash && (diri.e == Edges.NOTSLASH || diri.e == Edges.BACKSLASH)) return true;
        if (!diri.isSlash && (diri.e == Edges.NOTBACKSLASH || diri.e == Edges.SLASH)) return true;
        return false;
    }

    public boolean pathPossible(int x,int y,Direction dir)
    {
        if (isBlocked(x,y,dir)) return false;
        if (diri.e == Edges.LINK || diri.e == Edges.SLASH || diri.e == Edges.BACKSLASH) return false;
        return true;
    }

    public boolean isLinked(int x,int y,Direction dir)
    {
        if (isBlocked(x,y,dir)) return false;
        if (diri.e == Edges.LINK || diri.e == Edges.SLASH || diri.e == Edges.BACKSLASH) return true;
        return false;
    }

    public void setBlock(int x,int y,Direction dir)
    {
        if (!pathPossible(x,y,dir)) throw new RuntimeException("setBlock called on a not possible");

        if (diri.e == Edges.UNKNOWNLINK)
        {
            diri.setEdge(Edges.NOTLINK);
            --termcount;
        }
        else if (diri.e == Edges.UNKNOWNANGLE)
        {
            diri.setEdge(diri.isSlash ? Edges.NOTSLASH : Edges.NOTBACKSLASH);
        }
        else
        {
            diri.setEdge(Edges.NOTANGLE);
            --termcount;
        }
    }

    public void setEdge(int x,int y,Direction dir)
    {
        if (!pathPossible(x, y, dir)) throw new RuntimeException("setEdge called on a not possible");
        --termcount;

        if (diri.e == Edges.UNKNOWNLINK) diri.setEdge(Edges.LINK);
        else diri.setEdge(diri.isSlash ? Edges.SLASH : Edges.BACKSLASH);
    }


    public void linkToBlock(int x,int y,Direction dir)
    {
        if (!isLinked(x,y,dir)) throw new RuntimeException("linkToBlock only works on links");
        if (diri.isOrtho) diri.setEdge(Edges.NOTLINK);
        else diri.setEdge(Edges.NOTANGLE);
    }

    public void linkToUnknown(int x,int y, Direction dir)
    {
        if (!isLinked(x,y,dir)) throw new RuntimeException("linkToUnknown only works on links");
        ++termcount;
        if (diri.isOrtho) diri.setEdge(Edges.UNKNOWNLINK);
        else if (diri.isSlash) diri.setEdge(Edges.NOTBACKSLASH);
        else diri.setEdge(Edges.NOTSLASH);
    }










    public int getWidth() { return gfr.getWidth();}
    public int getHeight() { return gfr.getHeight(); }

    public int getEdgeWidth() { return 2 * getWidth() - 1; }
    public int getEdgeHeight() { return 2 * getHeight() - 1; }
    public Edges getEdges(int x,int y) { return edges[x][y]; }
    public void setEdge(int x,int y, Edges e) { edges[x][y] = e; }

    public int getCount(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]);}
    public String getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y];}

    public void decrementTermCount() { --termcount; }
    public int getTermCount() { return termcount; }



}
