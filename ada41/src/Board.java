import grid.file.GridFileReader;
import grid.logic.LogicStatus;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chien on 2/26/2017.
 */
public class Board
{
    GridFileReader gfr;
    Point startPoint;

    public enum CellType
    {
        EMPTY(null),
        BLACK(Color.black),
        WHITE(Color.white),
        GRAY(Color.DARK_GRAY);

        private Color mycolor;
        private CellType(Color c) { mycolor = c;}
        public Color getColor() { return mycolor; }
    }

    ;

    public enum ArrowDir
    {
        NORTH, // 0
        EAST, // 1
        SOUTH, // 2
        WEST; // 3
        public static final ArrowDir[] values = values();
        // assumes that
        // A) there are 4 directions, 0,1,2,3
        // B) NORTH and SOUTH are even, and EAST and WEST are odd.
        public ArrowDir opposite() { return values[(this.ordinal()+2)%4]; }

        // assuming that the path comes FROM the given direction, what is the TO direction for a left.
        public ArrowDir left() { return values[(this.ordinal()+1)%4];}
    }

    ;

    public class ArrowInfo
    {
        ArrowDir dir;
        int count;

        public ArrowInfo(ArrowDir dir, int count)
        {
            this.dir = dir;
            this.count = count;
        }
    }

    private ArrowInfo[][] arrows;
    private CellType[][] celltypes;
    private CellType[][] quadcells;

    public Board(GridFileReader gfr)
    {
        this.gfr = gfr;
        celltypes = new CellType[getWidth()][getHeight()];
        arrows = new ArrowInfo[getWidth()][getHeight()];
        quadcells = new CellType[getWidth() * 2][getHeight() * 2];

        String spparts[] = gfr.getVar("STARTPOINT").split(" ");
        startPoint = new Point(Integer.parseInt(spparts[0]),Integer.parseInt(spparts[1]));


        Pattern arrowPattern = Pattern.compile("^(\\d+)(.)$");

        for (int x = 0; x < getWidth(); ++x)
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                switch (gfr.getBlock("COLORS")[x][y])
                {
                    case ".":
                        celltypes[x][y] = CellType.EMPTY;
                        break;
                    case "B":
                        celltypes[x][y] = CellType.BLACK;
                        break;
                    case "W":
                        celltypes[x][y] = CellType.WHITE;
                        break;
                    case "G":
                        celltypes[x][y] = CellType.GRAY;
                        break;
                    default:
                        throw new RuntimeException("Illegal char in COLORS block: " + gfr.getBlock("COLORS")[x][y]);
                }
                String s = gfr.getBlock("NUMBERS")[x][y];
                if (s.equals(".")) continue;

                Matcher m = arrowPattern.matcher(s);
                if (!m.matches()) throw new RuntimeException("Illegal Arrow Designator " + s);

                ArrowDir ad = ArrowDir.NORTH;
                switch (m.group(2).charAt(0))
                {
                    case '^':
                        ad = ArrowDir.NORTH;
                        break;
                    case 'v':
                        ad = ArrowDir.SOUTH;
                        break;
                    case '<':
                        ad = ArrowDir.WEST;
                        break;
                    case '>':
                        ad = ArrowDir.EAST;
                        break;
                    default:
                        throw new RuntimeException("Illegal dir character in NUMBERS: " + s);
                }
                arrows[x][y] = new ArrowInfo(ad, Integer.parseInt(m.group(1)));
            }
        }
        initEdges();
        initQuadCells();
        initOperantStack();


    }


    public int getWidth()
    {
        return gfr.getWidth();
    }

    public int getHeight()
    {
        return gfr.getHeight();
    }

    public boolean isOnBoard(int x, int y)
    {
        return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
    }


    public boolean hasLetter(int x, int y)
    {
        return !gfr.getBlock("LETTERS")[x][y].equals(".");
    }

    public char getLetter(int x, int y)
    {
        return gfr.getBlock("LETTERS")[x][y].charAt(0);
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public CellType getCellType(int x, int y)
    {
        return celltypes[x][y];
    }

    public ArrowInfo getArrowInfo(int x, int y)
    {
        return arrows[x][y];
    }

    public enum EdgeStatus
    {
        UNKNOWN, WALL, PATH
    }

    ;

    Vector<Vector<EdgeInfo>> operantStack = new Vector<Vector<EdgeInfo>>();
    Vector<EdgeInfo> operantEdges;
    int stackdepth;

    public int getCurDepth()
    {
        return stackdepth;
    }

    public boolean isCurDepthEmpty()
    {
        return operantEdges.size() == 0;
    }

    private void initOperantStack()
    {
        operantEdges = new Vector<EdgeInfo>();
        operantStack.add(operantEdges);
        stackdepth = 0;
    }

    public void increaseDepth()
    {
        operantEdges = new Vector<EdgeInfo>();
        operantStack.add(operantEdges);
        ++stackdepth;
    }

    public void decreaseDepth()
    {
        if (stackdepth == 0) throw new RuntimeException("Can't delete last level!");
        while (backUp()) ;
        operantStack.remove(stackdepth);
        operantEdges = operantStack.lastElement();
        --stackdepth;
    }


    public class EdgeInfo
    {
        private EdgeStatus pes;
        EdgeKey myKey;
        boolean isSpecial;
        int depth;

        public EdgeInfo(EdgeKey myKey)
        {
            pes = EdgeStatus.UNKNOWN;
            this.myKey = myKey;
        }

        public EdgeStatus getEdgeStatus()
        {
            return pes;
        }

        public void clearEdgeStatus()
        {
            pes = EdgeStatus.UNKNOWN;
            isSpecial = false;
            depth = -1;
        }


        public void setEdgeStatus(EdgeStatus es)
        {
            if (es == EdgeStatus.UNKNOWN) throw new RuntimeException("Can't setEdgeStatus to UNKNOWN");
            this.pes = es;
            depth = getCurDepth();
            if (depth > 0 && operantEdges.size() == 0) isSpecial = true;
            operantEdges.add(this);
        }
    }

    public boolean backUp()
    {
        if (operantEdges.size() == 0) return false;
        EdgeInfo doomed = operantEdges.remove(operantEdges.size() - 1);
        doomed.clearEdgeStatus();
        return true;
    }


    private class EdgeKey
    {
        private Point p;
        private boolean isVertical;

        public Point getPoint()
        {
            return p;
        }

        public boolean isVertical()
        {
            return isVertical;
        }

        @Override
        public int hashCode()
        {
            int result = p.hashCode();
            result *= isVertical ? 17 : 23;
            return result;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            EdgeKey oek = (EdgeKey) o;
            if (!getPoint().equals(oek.getPoint())) return false;
            if (isVertical != oek.isVertical()) return false;
            return true;
        }

        public EdgeKey(Point p, boolean isVertical)
        {
            this.p = p;
            this.isVertical = isVertical;
        }

        public EdgeKey(int x, int y, boolean isVertical)
        {
            this.p = new Point(x, y);
            this.isVertical = isVertical;
        }


    }

    private Map<EdgeKey, EdgeInfo> edges = new HashMap<>();


    private void initEdges()
    {
        for (int x = 0; x < getWidth(); ++x)
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                CellType ct = getCellType(x, y);
                if (ct != CellType.EMPTY) continue;
                // East
                if (x != getWidth() - 1)
                {
                    ct = getCellType(x + 1, y);
                    if (ct == CellType.EMPTY)
                    {
                        EdgeKey ek = new EdgeKey(x, y, true);
                        EdgeInfo ei = new EdgeInfo(ek);
                        edges.put(ek, ei);
                    }
                }
                // South
                if (y != getHeight() - 1)
                {
                    ct = getCellType(x, y + 1);
                    if (ct == CellType.EMPTY)
                    {
                        EdgeKey ek = new EdgeKey(x, y, false);
                        EdgeInfo ei = new EdgeInfo(ek);
                        edges.put(ek, ei);
                    }
                }
            }
        }
    }

    public EdgeInfo getEastEdgeInfo(int x, int y)
    {
        return edges.get(new EdgeKey(x, y, true));
    }

    public EdgeInfo getWestEdgeInfo(int x, int y)
    {
        return edges.get(new EdgeKey(x - 1, y, true));
    }

    public EdgeInfo getNorthEdgeInfo(int x, int y)
    {
        return edges.get(new EdgeKey(x, y - 1, false));
    }

    public EdgeInfo getSouthEdgeInfo(int x, int y)
    {
        return edges.get(new EdgeKey(x, y, false));
    }

    public EdgeInfo getEdgeInfo(int x, int y, ArrowDir dir)
    {
        switch (dir)
        {
            case NORTH:
                return getNorthEdgeInfo(x, y);
            case SOUTH:
                return getSouthEdgeInfo(x, y);
            case EAST:
                return getEastEdgeInfo(x, y);
            case WEST:
                return getWestEdgeInfo(x, y);
        }
        throw new RuntimeException("Shouldn't get here!");
    }


    private void expandCellToQuads(int ox, int oy, CellType color)
    {
        int ulx = 2 * ox;
        int uly = 2 * oy;

        for (int x = ulx - 1; x <= ulx + 2; ++x)
        {
            for (int y = uly - 1; y <= uly + 2; ++y)
            {
                int hx = x / 2;
                int hy = y / 2;
                if (x < 0 || y < 0 || x >= getWidth() * 2 || y >= getHeight() * 2) continue;
                if (getCellType(hx, hy) != CellType.EMPTY) continue;
                quadcells[x][y] = color;
            }
        }
    }

    private void initQuadCells()
    {
        int qw = getWidth() * 2;
        int qh = getHeight() * 2;

        for (int x = 0; x < qw; ++x)
        {
            for (int y = 0; y < qh; ++y)
            {
                if (x == 0 || y == 0 || x == qw - 1 || y == qh - 1)
                {
                    quadcells[x][y] = CellType.BLACK;
                }
                else
                {
                    quadcells[x][y] = CellType.EMPTY;
                }
            }
        }

        for (int x = 0; x < getWidth(); ++x)
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                if (getCellType(x, y) == CellType.EMPTY) continue;
                if (getCellType(x,y) == CellType.GRAY) continue;
                expandCellToQuads(x, y, getCellType(x, y));
            }
        }


    }

    public CellType getULQuadCell(int x, int y)
    {
        return quadcells[2 * x][2 * y];
    }

    public CellType getURQuadCell(int x, int y)
    {
        return quadcells[2 * x + 1][2 * y];
    }

    public CellType getLLQuadCell(int x, int y)
    {
        return quadcells[2 * x][2 * y + 1];
    }

    public CellType getLRQuadCell(int x, int y)
    {
        return quadcells[2 * x + 1][2 * y + 1];
    }

    private LogicStatus oneQuad(CellType t1, CellType t2, EdgeInfo ei)
    {
        if (t1 == CellType.EMPTY || t2 == CellType.EMPTY) return LogicStatus.STYMIED;
        if (ei == null) return LogicStatus.STYMIED;
        if (t1 == t2)
        {
            switch (ei.getEdgeStatus())
            {
                case UNKNOWN:
                    ei.setEdgeStatus(EdgeStatus.WALL);
                    return LogicStatus.LOGICED;
                case WALL:
                    return LogicStatus.STYMIED;
                case PATH:
                    return LogicStatus.CONTRADICTION;
            }
        }
        else
        {
            switch (ei.getEdgeStatus())
            {
                case UNKNOWN:
                    ei.setEdgeStatus(EdgeStatus.PATH);
                    return LogicStatus.LOGICED;
                case PATH:
                    return LogicStatus.STYMIED;
                case WALL:
                    return LogicStatus.CONTRADICTION;
            }
        }
        return LogicStatus.CONTRADICTION;
    }


    public LogicStatus scanQuads()
    {
        LogicStatus result = LogicStatus.STYMIED;

        for (int x = 0; x < getWidth(); ++x)
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                CellType ul = getULQuadCell(x, y);
                CellType ur = getURQuadCell(x, y);
                CellType ll = getLLQuadCell(x, y);
                CellType lr = getLRQuadCell(x, y);
                LogicStatus lstat;

                lstat = oneQuad(ul, ur, getNorthEdgeInfo(x, y));
                if (lstat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (lstat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

                lstat = oneQuad(ul, ll, getWestEdgeInfo(x, y));
                if (lstat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (lstat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

                lstat = oneQuad(ur, lr, getEastEdgeInfo(x, y));
                if (lstat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (lstat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

                lstat = oneQuad(ll, lr, getSouthEdgeInfo(x, y));
                if (lstat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (lstat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
        }
        return result;
    }

    public boolean validateArrow(int x, int y)
    {
        ArrowInfo ai = getArrowInfo(x, y);
        if (ai == null) throw new RuntimeException("No Arrow Info Here!");

        int dx;
        int dy;

        switch (ai.dir)
        {
            case NORTH:
                dx = 0;
                dy = -1;
                break;
            case SOUTH:
                dx = 0;
                dy = 1;
                break;
            case EAST:
                dx = 1;
                dy = 0;
                break;
            case WEST:
                dx = -1;
                dy = 0;
                break;
            default:
                throw new RuntimeException("Illegal value for ai.dir!");
        }

        int pathcount = 0;
        int holecount = 0;
        while (true)
        {
            x += dx;
            y += dy;
            if (!isOnBoard(x, y)) break;

            EdgeInfo ei = getEdgeInfo(x, y, ai.dir);
            if (ei == null) continue;
            if (ei.getEdgeStatus() == EdgeStatus.PATH) ++pathcount;
            if (ei.getEdgeStatus() == EdgeStatus.UNKNOWN) ++holecount;
        }
        if (pathcount > ai.count) return false;
        if (pathcount + holecount < ai.count) return false;
        return true;
    }

    private int dX(ArrowDir dir)
    {
        if (dir == ArrowDir.EAST) return 1;
        if (dir == ArrowDir.WEST) return -1;
        return 0;
    }
    private int dY(ArrowDir dir)
    {
        if (dir == ArrowDir.NORTH) return -1;
        if (dir == ArrowDir.SOUTH) return 1;
        return 0;
    }


    // starts lower left (0,11)
    // CCW starts east
    public void walkPath(int sx,int sy,ArrowDir sdir)
    {
        StringBuffer sbleft = new StringBuffer();
        StringBuffer sbright = new StringBuffer();
        StringBuffer sbstraight = new StringBuffer();

        int cx = sx;
        int cy = sy;
        ArrowDir curdir = sdir;
        while(true)
        {
            cx += dX(curdir);
            cy += dY(curdir);
            if (cx == sx && cy == sy) break;

            ArrowDir fromdir = curdir.opposite();

            ArrowDir todir = fromdir.left();
            EdgeInfo ei = getEdgeInfo(cx,cy,todir);
            if (ei != null && ei.getEdgeStatus() == EdgeStatus.PATH)
            {
                sbleft.append(getLetter(cx,cy));
                curdir = todir;
                continue;
            }

            todir = todir.left();
            ei = getEdgeInfo(cx,cy,todir);
            if (ei != null && ei.getEdgeStatus() == EdgeStatus.PATH)
            {
                sbstraight.append(getLetter(cx,cy));
                curdir = todir;
                continue;
            }

            todir = todir.left();
            ei = getEdgeInfo(cx,cy,todir);
            if (ei != null && ei.getEdgeStatus() == EdgeStatus.PATH)
            {
                sbright.append(getLetter(cx,cy));
                curdir = todir;
                continue;
            }
        }
        System.out.println("lefts: " + sbleft.toString());
        System.out.println("rights: " + sbright.toString());
        System.out.println("straights: " + sbstraight.toString());
    }



}
