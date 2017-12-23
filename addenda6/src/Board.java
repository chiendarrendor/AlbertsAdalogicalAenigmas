import grid.file.GridFileReader;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.Collection;
import java.util.Vector;

/**
 * Created by chien on 8/17/2017.
 */
public class Board
{
    GridFileReader gfr;
    int[][] corners;

    CellState[][] cells;
    int unknowncount;


    public Board(String fname)
    {
        gfr = new GridFileReader(fname);

        corners = new int[getWidth()+1][getHeight()+1];
        cells = new CellState[getWidth()][getHeight()];
        unknowncount = getWidth() * getHeight();


        for (int x = 0 ; x <= getWidth() ; ++x)
        {
            for (int y = 0 ; y <= getHeight() ; ++y)
            {
                corners[x][y] = -1;
            }
        }

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                cells[x][y] = CellState.UNKWNOWN;

                String des = gfr.getBlock("NUMBERS")[x][y];
                if (des.equals(".")) continue;

                Direction d = Direction.fromShort(des.substring(0,2));
                int val = Integer.parseInt(des.substring(2));

                try
                {
                    setCorner(x, y, d, val);
                }
                catch(Exception ex)
                {
                    throw new RuntimeException(ex + " at " + x + " " + y + " due to " + des.substring(0,2) + " " + d);
                }
            }
        }
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        corners = right.corners;
        cells = new CellState[getWidth()][getHeight()];
        unknowncount = right.unknowncount;

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                cells[x][y] = right.cells[x][y];
            }
        }
    }

    public CellState getCell(int x,int y) { return cells[x][y]; }

    public void setCellRiver(int x,int y)
    {
        if (cells[x][y] != CellState.UNKWNOWN) throw new BadBoardRuntimeException("Only can set unknown cells " + x + " " + y,this);
        cells[x][y] = CellState.RIVER;
        --unknowncount;
    }

    public void setCellLand(int x,int y)
    {
        if (cells[x][y] != CellState.UNKWNOWN) throw new BadBoardRuntimeException("Only can set unknown cells " + x + " " + y,this);
        cells[x][y] = CellState.LAND;
        --unknowncount;
    }

    // given a corner identifier in cell space, return the corner in corner space
    public Point getCornerPoint(int x,int y,Direction d)
    {
        int dx = 0;
        int dy = 0;
        switch(d)
        {
            case NORTHWEST: break;
            case NORTHEAST: dx = 1; break;
            case SOUTHWEST: dy = 1; break;
            case SOUTHEAST: dx = 1; dy = 1; break;
            default: throw new RuntimeException("Corners have to be diagonals!");
        }
        return new Point(x+dx,y+dy);
    }



    // returns a corner in cell space
    public int getCorner(int x,int y,Direction d)
    {
        Point p = getCornerPoint(x,y,d);
        return corners[p.x][p.y];
    }

    // sets a corner in cell space
    public void setCorner(int x,int y, Direction d,int val)
    {
        Point p = getCornerPoint(x,y,d);
        corners[p.x][p.y] = val;
    }

    // returns a corner in corner space
    public int getCorner(int x,int y)
    {
        return corners[x][y];
    }

    // gets adjacent points (in cell space) given a corner in corner space
    public Collection<Point> getAdjacents(int x,int y)
    {
        Vector<Point> points = new Vector<>();
        AddIfIn(points,x,y);
        AddIfIn(points,x-1,y);
        AddIfIn(points,x,y-1);
        AddIfIn(points,x-1,y-1);
        return points;
    }

    private void AddIfIn(Vector<Point> points, int x, int y)
    {
        if (!gfr.inBounds(x,y)) return;
        points.add(new Point(x,y));
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }


}
