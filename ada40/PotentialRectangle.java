
import grid.logic.LogicStatus;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class PotentialRectangle
{
	Vector<Rectangle> rectangles = new Vector<Rectangle>();
	int size;
	int ox;
	int oy;
	int rectid;
	int bwidth;
	int bheight;

	public PotentialRectangle(PotentialRectangle right)
    {
        size = right.size;
        ox = right.ox;
        oy = right.oy;
        rectid = right.rectid;
        bwidth = right.bwidth;
        bheight = right.bheight;

        for (Rectangle r : right.rectangles) { rectangles.add(r); }

    }


	public PotentialRectangle(int cx, int cy, int size,int rectid,
                              RectangleList rlist,int bwidth,int bheight)
	{
	    this.size = size;
	    this.ox = cx;
	    this.oy = cy;
	    this.rectid = rectid;
        this.bwidth = bwidth;
        this.bheight = bheight;


		for (Dimension r : rlist.rectanglesOfSize(size))
		{
			for (int x = 0 ; x < r.width; ++x)
			{
				for (int y = 0 ; y < r.height ; ++y)
				{
				    int nx = cx-x;
				    int ny = cy-y;
				    int lrx = nx+r.width-1;
				    int lry = ny+r.height-1;
				    if (nx < 0 || ny < 0 || nx >= bwidth || ny >= bheight)
                    {
                        continue;
                    }

                    // if upper left point isn't upper left out of bounds, neither will lower right
                    if (lrx >= bwidth || lry >= bheight)
                    {
                        continue;
                    }

					rectangles.add(new Rectangle(cx-x,cy-y,r.width,r.height));
				}
			}
		}
	}

	private Vector<Point> getCellPoints(Rectangle r)
    {
        Vector<Point> result = new Vector<Point>();
        for (int x = r.x ; x < r.x + r.width ; ++x)
        {
            for (int y = r.y ; y < r.y + r.height ; ++y)
            {
                result.add(new Point(x,y));
            }
        }
        return result;
    }

    private Vector<Point> getWallPoints(Rectangle r)
	{
        Vector<Point> result = new Vector<Point>();
	    for (int x = r.x ; x < r.x + r.width ; ++x)
        {
            if (r.y > 0) result.add(new Point(x,r.y-1));
            if (r.y + r.height < bheight) result.add(new Point(x,r.y+r.height));
        }
        for (int y = r.y ; y < r.y+r.height ; ++y)
        {
            if (r.x > 0) result.add(new Point(r.x-1,y));
            if (r.x + r.width < bwidth) result.add(new Point(r.x+r.width,y));
        }
        return result;
	}


	// 1) for each Rectangle still possible
    // 2) make sure that it still fits on the board
    //    2a) each space on rectangle must be either
    //        UNKNOWN
    //        RECTANGLE (with our rectid)
    //        EMPTY
    //    2b) every space orthogonally adjacent to rectangle that is on the board must be UNKNOWN or WALL
    // 3) remove from list any Rectangle that does not fit.
    // 4) if all rectangles are removed, return CONTRADICTION
    // 5) for each legal rectangle, compile what it does to the board
    // 6) find intersection of all legal rectangles (all rectangles do the same thing to the board)
    // 7) update board with that intersection... if anything changed, returned LOGICED, else STYMIED

    private class RectInfoHolder
    {
        Vector<Point> insides;
        Vector<Point> walls;
        Rectangle r;
    }


	public LogicStatus updateSelfAndBoard(Board b)
    {
        Map<Point,CellIntersector> cells = new HashMap<Point,CellIntersector>();
        Vector<RectInfoHolder> infos = new Vector<RectInfoHolder>();
        Vector<Rectangle> rectanglesLeft = new Vector<Rectangle>();

        for (Rectangle r : rectangles)
        {
            RectInfoHolder rih = new RectInfoHolder();
            rih.r = r;
            rih.insides = getCellPoints(r);
            rih.walls = getWallPoints(r);

            boolean fail = false;
            for (Point in : rih.insides)
            {
                if (!cells.containsKey(in)) cells.put(in, new CellIntersector(b.getCellInfo(in.x, in.y)));
                CellIntersector ci = cells.get(in);

                if (!ci.canMakeRectangle(rectid))
                {
                    fail = true;
                    break;
                }
            }
            if (fail) continue;

            for (Point in : rih.walls)
            {
                if (!cells.containsKey(in)) cells.put(in, new CellIntersector(b.getCellInfo(in.x, in.y)));
                CellIntersector ci = cells.get(in);

                if (!ci.canMakeWall())
                {
                    fail = true;
                    break;
                }
            }
            if (fail) continue;

            // okay, if we get here, this rectangle is legal for the walls and cells.
            rectanglesLeft.add(r);
            // put them into the cellInterectors
            for (Point in : rih.insides)
            {
                cells.get(in).makeRectangle(rectid);
            }
            for (Point in : rih.walls)
            {
                cells.get(in).makeWall();
            }
            // and put the rectangle int to the list
            infos.add(rih);
        }


        // regardless of outcome, we should modify the set of legal rectangles appropriately.
        rectangles = rectanglesLeft;

        // now we are here, we have the set of rectangles that are still legal
        // if zero, we're contractidtory
        if (infos.size() == 0) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;
        for (CellIntersector ci : cells.values())
        {
            if (ci.changesBoard(infos.size()))
            {
                result = LogicStatus.LOGICED;
                ci.changeBoard();
            }
        }


        return result;
    }



	public void show()
    {
        for(Rectangle r : rectangles)
        {
            System.out.println(r.toString());
        }
    }

}