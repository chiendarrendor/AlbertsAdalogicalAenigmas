import grid.lambda.CellLambda;
import grid.puzzlebits.Direction;

import java.awt.*;

/**
 * Created by chien on 10/22/2017.
 */
public class PathAnalyzer
{
    public class CellInfo
    {
        boolean noPath = false;
        boolean unknownPath = false;
        boolean isTerminal = false;
        boolean isNorth = false;
        boolean isSouth = false;
        boolean isWest = false;
        boolean isEast = false;
        boolean isLoop = false;

        public void setDir(Direction d)
        {
            switch(d)
            {
                case NORTH: isNorth = true; break;
                case SOUTH: isSouth = true; break;
                case EAST: isEast = true; break;
                case WEST: isWest = true; break;
                default: throw new RuntimeException("non-orthogonal direction!");
            }
        }


    }




    private CellInfo[][] cells;

    public CellInfo getCellInfo(int x,int y) { return cells[x][y]; }


    public PathAnalyzer(Board b)
    {
        PathManager pm = b.getPathManager();
        cells = new CellInfo[b.getWidth()][b.getHeight()];
        CellLambda.forEachCell(b.getWidth(),b.getHeight(),(x,y)-> {
            cells[x][y] = new CellInfo();
            PathManager.PathPointer pp = pm.pathgrid[x][y];
            if (pp == null) { cells[x][y].noPath = true; return; }
            if (pp.isUnknown == true) cells[x][y].unknownPath = true;
        });

        for (PathManager.Path p : pm.paths)
        {
            for(int i = 0 ; i < p.cells.size() ; ++i)
            {
                Point p1 = p.cells.elementAt(i);

                cells[p1.x][p1.y].isLoop = p.isClosed;

                if (i == 0)
                {
                    cells[p1.x][p1.y].isTerminal = true;
                }

                if (i != p.cells.size()-1)
                {
                    Point p2 = p.cells.elementAt(i+1);
                    Direction d = Direction.fromTo(p1.x,p1.y,p2.x,p2.y);
                    cells[p1.x][p1.y].setDir(d);
                    cells[p2.x][p2.y].setDir(d.getOpp());
                }
                else
                {
                    cells[p1.x][p1.y].isTerminal = true;
                }
            }
        }
    }
}
