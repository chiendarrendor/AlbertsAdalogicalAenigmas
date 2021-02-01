import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.graph.GridGraph;
import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;
import grid.puzzlebits.CanonicalPointSet;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pattern {
    // size of Board
    @Shallow int gridwidth;
    @Shallow int gridheight;
    // these are all specified in pattern coordinates.
    @Shallow int minx;
    @Shallow int miny;
    @Shallow int maxx;
    @Shallow int maxy;
    @Shallow int centerx;
    @Shallow int centery;
    @Deep CellContainer<PatternCell> cells;
    @Shallow int unknowns;
    @Shallow int patternwidth;
    @Shallow int patternheight;

    @Shallow int finalsize = -1;
    @Shallow CanonicalPointSet cps = null;

    public Pattern(int gwidth,int gheight) {
        gridwidth = gwidth;
        gridheight = gheight;
        centerx = gridwidth - 1;
        centery = gridheight - 1;
        unknowns = (2*gridwidth-1)*(2*gridheight-1) - 1;
        patternwidth = 2*gridwidth-1;
        patternheight = 2*gridheight - 1;
        cells = new CellContainer<PatternCell>(patternwidth,patternheight,
                (x,y)-> (x==centerx && y == centery) ? PatternCell.INSIDE : PatternCell.UNKNOWN);
        minx = 0;
        miny = 0;
        maxx = 2 * gridwidth - 2;
        maxy = 2 * gridheight - 2;
    }

    public Pattern(Pattern right) {
        CopyCon.copy(this,right);
    }

    public void setCell(int x,int y, PatternCell pc) {
        if (cells.getCell(x,y) == PatternCell.UNKNOWN) --unknowns;
        cells.setCell(x,y,pc);
    }

    public PatternCell getCell(int x,int y) { return cells.getCell(x,y); }



    // given a location in board coordinates bx,by
    // virtually overlay that board on the pattern so that bx,by maps to centerx,centery
    // and mark all cells outside the board as OUTSIDE, and adjust min/max x,y appropriately.
    public void clearEdges(int bx,int by) {
        // edges of board in pattern coordinates
        int leftx = centerx - bx;
        int rightx = centerx + gridwidth - bx - 1;
        int topy = centery - by;
        int bottomy = centery + gridheight - by - 1;
        boolean marked = false;

        for(int x = minx ; x < leftx ; ++x ) {
            for (int y = 0 ; y < cells.getHeight() ; ++y) {
                setCell(x,y,PatternCell.OUTSIDE);
                marked = true;
            }
        }

        marked = false;
        for (int x = rightx+1 ; x <= maxx ; ++x) {
            for (int y =  0 ; y < cells.getHeight() ; ++y) {
                setCell(x,y,PatternCell.OUTSIDE);
                marked = true;
            }
        }

        marked = false;
        for (int y = miny ; y < topy ; ++y) {
            for (int x = 0 ; x < cells.getWidth() ; ++x) {
                setCell(x,y,PatternCell.OUTSIDE);
                marked = true;
            }
        }

        marked = false;
        for (int y = bottomy+1 ; y <= maxy ; ++y) {
            for (int x = 0 ; x < cells.getWidth() ; ++x) {
                setCell(x,y,PatternCell.OUTSIDE);
                marked = true;
            }
        }
    }



    public void show() {
        Set<Point> articulations = getArticulatingUnknowns();

        System.out.print("   ");
        for (int x = minx ; x <= maxx ; ++x ) {
            System.out.print((x / 10 > 0 ? x / 10 : " ") + " ");
        }
        System.out.println("");
        System.out.print("   ");
        for (int x = minx ; x <= maxx ; ++x ) {
            System.out.print(x%10+" ");
        }
        System.out.println("");
        for (int y = miny ; y <= maxy ; ++y) {
            System.out.print((y/10 > 0 ? y/10 : " "));
            System.out.print(y%10 + " ");
            for (int x = minx ; x <= maxx ; ++x) {
                switch(cells.getCell(x,y)) {
                    case UNKNOWN:
                        Point p = new Point(x,y);
                        if (articulations.contains(p)) {
                            System.out.print("# ");
                        } else {
                            System.out.print("? ");
                        }
                        break;
                    case INSIDE:
                        if (x == centerx && y == centery) System.out.print("@ ");
                        else System.out.print("O ");
                        break;
                    case OUTSIDE: System.out.print(". "); break;
                }
            }
            System.out.println("");
        }
    }

    public boolean isComplete() {
        if (unknowns > 0) return false;

        if (cps == null) {
            Set<Point> points = new HashSet<>();
            for (int y = miny; y <= maxy; ++y) {
                for (int x = minx; x <= maxx; ++x) {
                    if (cells.getCell(x, y) != PatternCell.INSIDE) continue;
                    points.add(new Point(x, y));
                }
            }
            cps = new CanonicalPointSet(points);
            finalsize = points.size();
        }

        return true;
    }

    public int getFinalSize() { return finalsize; }
    public CanonicalPointSet getCanonicalPointSet() { return cps; }

    // given an overlay of the board onto the pattern where rid.getX,rid.getY maps to centerx,centery
    // for each x,y where the RegionSet does not contain rid, pattern must be OUTSIDE there
    // for each x,y where the Region set _only_ contains rid, pattern must be INSIDE there
    // function returns CONTRADICTION if we have to change OUTSIDE to INSIDE or vice versa
    // function returns STYMIED if no cells were moved from UNKNOWN
    // function returns LOGICED if any cells were moved from UNKNOWN
    public LogicStatus updateFromBoard(Board thing, RegionId rid) {
        int rx = rid.getX();
        int ry = rid.getY();

        LogicStatus result = LogicStatus.STYMIED;


        // this x and y are in pattern space
        for (int y = miny ; y <= maxy ; ++y) {
            int by = ry - centery + y;
            for (int x = minx ; x <= maxx ; ++x) {
                int bx = rx - centerx + x;
                if (!thing.inBounds(bx,by)) continue;
                // bx and by have been shifted to board space
                RegionSet rs = thing.getRegionSet(bx,by);
                if (rs.size() == 0) return LogicStatus.CONTRADICTION;
                if (rs.hasRegion(rid) && rs.size() == 1) {
                    switch(cells.getCell(x,y)) {
                        case UNKNOWN:
                            result = LogicStatus.LOGICED;
                            setCell(x,y,PatternCell.INSIDE);
                            break;
                        case INSIDE: break;
                        case OUTSIDE: return LogicStatus.CONTRADICTION;
                    }
                } else if (!rs.hasRegion(rid)) {
                    switch(cells.getCell(x,y)) {
                        case UNKNOWN:
                            result = LogicStatus.LOGICED;
                            setCell(x,y,PatternCell.OUTSIDE);
                            break;
                        case INSIDE: return LogicStatus.CONTRADICTION;
                        case OUTSIDE: break;
                    }
                }
            }
        }

        return result;
    }

    private class ConRef implements PossibleConnectivityDetector.PossibleConnectivityReference {
        @Override public int getWidth() { return patternwidth; }
        @Override public int getHeight() { return patternheight; }
        @Override public boolean isConnectedCell(int x, int y) { return cells.getCell(x,y) == PatternCell.INSIDE; }
        @Override public boolean isPossibleCell(int x, int y) { return cells.getCell(x,y) == PatternCell.UNKNOWN; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }

    private class GridRef implements GridGraph.GridReference {
        @Override public int getWidth() { return patternwidth; }
        @Override public int getHeight() { return patternheight; }
        @Override public boolean isIncludedCell(int x, int y) { return cells.getCell(x,y) != PatternCell.OUTSIDE; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    public ConRef getConnectivityReference() { return new ConRef(); }
    public GridRef getGridReference() { return new GridRef(); }


    // given an overlay of the board onto the pattern where rid.getX,rid.getY maps to centerx,centery
    // for each x,y where the pattern is OUTSIDE, REegionSet CANNOT contain rid
    // for each x,y where the pattern is INSIDE, RegionSet MUST contain rid
    // function returns CONTRADICTION the above cannot be maintained
    // function returns LOGICED if any regionsets were changed
    // otherwise STYMIED
    public LogicStatus updateToBoard(Board thing, RegionId rid) {
        int rx = rid.getX();
        int ry = rid.getY();

        LogicStatus result = LogicStatus.STYMIED;

        // this x and y are in pattern space
        for (int y = miny ; y <= maxy ; ++y) {
            int by = ry - centery + y;
            for (int x = minx ; x <= maxx ; ++x) {
                int bx = rx - centerx + x;
                if (!thing.inBounds(bx,by)) continue;
                // bx and by have been shifted to board space
                RegionSet rs = thing.getRegionSet(bx,by);
                if (rs.size() == 0) return LogicStatus.CONTRADICTION;

                if (cells.getCell(x,y) == PatternCell.INSIDE) {
                    if (!rs.hasRegion(rid)) return LogicStatus.CONTRADICTION;
                    if (rs.size() > 1) {
                        result = LogicStatus.LOGICED;
                        rs.setRegion(rid);
                    }
                } if (cells.getCell(x,y) == PatternCell.OUTSIDE) {
                    if (rs.hasRegion(rid) && rs.size() == 1) return LogicStatus.CONTRADICTION;
                    if (rs.hasRegion(rid)) {
                        rs.clearRegion(rid);
                        result = LogicStatus.LOGICED;
                    }
                }
            }
        }


        return result;
    }


    public void tightenBounds() {
        int eastmost = Integer.MIN_VALUE;
        int westmost = Integer.MAX_VALUE;
        int northmost = Integer.MAX_VALUE;
        int southmmost = Integer.MIN_VALUE;

        for (int y = miny ; y <= maxy ; ++y) {
            for (int x = minx ; x <= maxx ; ++x) {
                if (cells.getCell(x,y) == PatternCell.OUTSIDE) continue;
                if (x > eastmost) eastmost = x;
                if (x < westmost) westmost = x;
                if (y < northmost) northmost = y;
                if (y > southmmost) southmmost = y;
            }
        }
        if (minx < westmost) minx = westmost;
        if (maxx > eastmost) maxx = eastmost;
        if (miny < northmost) miny = northmost;
        if (maxy > southmmost) maxy = southmmost;
    }

    public PointDist furthestPoint() {
        GridGraph gg = new GridGraph(getGridReference());
        PointDist pd = new PointDist(-1,null);
        Point center = new Point(centerx,centery);

        for (int y = miny ; y <= maxy ; ++y) {
            for (int x = minx ; x <= maxx ; ++x) {
                if (cells.getCell(x,y) != PatternCell.UNKNOWN) continue;
                Point curp = new Point(x,y);
                int distance = gg.shortestPathBetween(center,curp).size();
                if (distance > pd.distance) {
                    pd = new PointDist(distance,curp);
                }
            }
        }
        return pd;
    }

    Set<Point> getArticulatingUnknowns() {
        GridGraph gg = new GridGraph(new GridRef());
        Set<Point> result = new HashSet<>();

        if (gg.isConnected()) {
            Set<Point> articulations = gg.getArticulationPoints();

            for (Point p : articulations) {
                if (cells.getCell(p.x, p.y) == PatternCell.UNKNOWN) result.add(p);
            }
        }
        return result;
    }


}
