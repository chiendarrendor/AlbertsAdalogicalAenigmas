import grid.assistant.AssistantBoard;
import grid.assistant.AssistantMove;
import grid.file.GridFileReader;
import grid.graph.GridGraph;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Path.Path;
import org.omg.CORBA.UNKNOWN;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board implements StandardFlattenSolvable<Board>, AssistantBoard<Board> {
    Set<Point> circleSet;
    Set<Point> triangleSet;
    GridFileReader gfr;
    CellContainer<Character> regions;
    CellContainer<CellShape> shapes;
    CellContainer<CellType> cells;
    CellContainer<PathStatus> onpaths;
    int unknowns;
    int unknownpaths;
    Point startCell;
    Point endCell;



    public Board(String fname) {
        gfr = new GridFileReader(fname);

        char[] nextchar = new char[1];
        nextchar[0] = 0x100;

        regions = new CellContainer<Character>(getWidth(), getHeight(),
                (x, y) -> gfr.getBlock("REGIONS")[x][y].charAt(0) == '.' ? nextchar[0]++ : gfr.getBlock("REGIONS")[x][y].charAt(0)
        );

        validateRegions();

        circleSet = new HashSet<Point>();
        triangleSet = new HashSet<Point>();

        shapes = new CellContainer<CellShape>(getWidth(), getHeight(),
                (x, y) -> {
                    switch (gfr.getBlock("SYMBOLS")[x][y].charAt(0)) {
                        case 'T':
                            triangleSet.add(new Point(x,y));
                            return CellShape.TRIANGLE;
                        case 'C':
                            circleSet.add(new Point(x,y));
                            return CellShape.CIRCLE;
                        case '.':
                            return CellShape.NONE;
                        default:
                            throw new RuntimeException("Illegal character in SYMBOLS");
                    }
                }
        );
        startCell = makePointFromVar("START");
        endCell = makePointFromVar("END");

        LambdaInteger upath = new LambdaInteger(0);
        onpaths = new CellContainer<PathStatus>(getWidth(),getHeight(),
                (x,y)->{
                    if (getShape(x,y)==CellShape.TRIANGLE) return PathStatus.NOTONPATH;
                    if (getShape(x,y)==CellShape.CIRCLE) return PathStatus.ONPATH;
                    if (isStart(x,y)) return PathStatus.PATH_TERMINAL;
                    if (isEnd(x,y)) return PathStatus.PATH_TERMINAL;
                    upath.inc();
                    return PathStatus.UNKNOWN;
                });
        unknownpaths = upath.get();

        LambdaInteger unknowncount = new LambdaInteger(0);
        cells = new CellContainer<CellType>(getWidth(),getHeight(),
                (x,y)-> {
                    if (isEnd(x,y) || isStart(x,y) || getShape(x,y) != CellShape.NONE) {
                        return CellType.PATH;
                    }
                    unknowncount.inc();
                    return CellType.UNKNOWN;
                }
        );
        unknowns = unknowncount.get();
    }



    public Board(Board right) {
        circleSet = right.circleSet;
        triangleSet = right.triangleSet;
        gfr = right.gfr;
        regions = right.regions;
        shapes = right.shapes;
        startCell = right.startCell;
        endCell = right.endCell;
        cells = new CellContainer<CellType>(right.cells);
        onpaths = new CellContainer<PathStatus>(right.onpaths);
        unknowns = right.unknowns;
        unknownpaths = right.unknownpaths;
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public String getSolution() { return gfr.getVar("SOLUTION"); }
    public boolean inBounds(Point p) { return gfr.inBounds(p); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }


    public boolean isStart(int x,int y) { return x == startCell.x && y == startCell.y; }
    public boolean isEnd(int x,int y) { return x == endCell.x && y == endCell.y; }
    public Point getStart() { return startCell; }
    public Point getEnd() { return  endCell; }
    public char getRegionId(int x,int y) { return regions.getCell(x,y); }
    public CellShape getShape(int x,int y) { return shapes.getCell(x,y); }
    public CellType getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y,CellType ct) {
        if (ct != CellType.UNKNOWN && cells.getCell(x,y) == CellType.UNKNOWN) --unknowns;
        if (ct == CellType.UNKNOWN && cells.getCell(x,y) != CellType.UNKNOWN) ++unknowns;

        if (ct == CellType.WALL) {
            setPath(x,y,PathStatus.PATH_WALL);
        }

        if (ct != CellType.WALL) {
            setPath(x,y,PathStatus.UNKNOWN);
        }

        cells.setCell(x,y,ct);
    }
    public Set<Point> getCircleSet() { return circleSet; }
    public Set<Point> getTriangleSet() { return triangleSet; }

    public void setPath(int x,int y,PathStatus ps) {
        if (ps != PathStatus.UNKNOWN && onpaths.getCell(x,y) == PathStatus.UNKNOWN) --unknownpaths;
        if (ps == PathStatus.UNKNOWN && onpaths.getCell(x,y) != PathStatus.UNKNOWN) ++unknownpaths;

        onpaths.setCell(x,y,ps);
    }
    public PathStatus getPath(int x,int y) { return onpaths.getCell(x,y); }
    public boolean isOnPath(int x,int y) {
        return
                onpaths.getCell(x,y) == PathStatus.ONPATH ||
                onpaths.getCell(x,y) == PathStatus.PATH_TERMINAL
                ;
    }
    public void setOnPath(int x,int y) { setPath(x,y,PathStatus.ONPATH);}
    public boolean notOnPath(int x,int y) { return onpaths.getCell(x,y) == PathStatus.NOTONPATH; }
    public void setNotOnPath(int x,int y) { setPath(x,y,PathStatus.NOTONPATH); }


    private Point makePointFromVar(String varname) {
        String[] coords = gfr.getVar(varname).split("\\s+");
        return new Point(Integer.parseInt(coords[0]),Integer.parseInt(coords[1]));
    }

    private class ValRef implements GridGraph.GridReference {
        public int getWidth() { return Board.this.getWidth(); }
        public int getHeight() { return Board.this.getHeight(); }
        public boolean isIncludedCell(int x,int y) { return true; }
        public boolean edgeExitsEast(int x,int y) { return getRegionId(x,y) == getRegionId(x+1,y); }
        public boolean edgeExitsSouth(int x,int y) { return getRegionId(x,y) == getRegionId(x,y+1); }
    }



    private void validateRegions() {
        Set<Character> regionIds = new HashSet<>();
        GridGraph gg = new GridGraph(new ValRef());
        // by definition, every conset of this graph has cells with the same region id
        List<Set<Point>> consets = gg.connectedSets();

        consets.stream().forEach(s-> {
            Point apoint = s.iterator().next();
            char rid = getRegionId(apoint.x,apoint.y);
            if (regionIds.contains(rid)) throw new RuntimeException("Duplicate Region Id!");
            regionIds.add(rid);
        });

    }

    // AssistantBoard methods
    public Board clone() { return new Board(this); }

    // StandardFlattenSolvable methods
    public boolean isComplete() { return unknowns == 0 && unknownpaths == 0; }

    public static class MyMove implements AssistantMove<Board> {
        int x;
        int y;
        boolean isPath;
        CellType ct;
        PathStatus ps;


        public MyMove(int x,int y,PathStatus ps) {this.x = x; this.y = y; isPath = true; this.ps = ps;}
        public MyMove(int x,int y,CellType ct) { this.x = x; this.y = y; isPath = false; this.ct = ct; }
        public boolean applyMove(Board b) {
            if (isPath) {
                if (b.getCell(x,y) != CellType.PATH) return false;
                if (b.getPath(x,y) != PathStatus.UNKNOWN) return b.getPath(x,y) == ps;
                b.setPath(x,y,ps);
                return true;
            } else {
                if (b.getCell(x, y) != CellType.UNKNOWN) return b.getCell(x, y) == ct;
                b.setCell(x, y, ct);
                return true;
            }
        }
    }

    public boolean applyMove(Object o) {
        MyMove mm = (MyMove)o;
        return mm.applyMove(this);
    }



    public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        CellType ct = this.getCell(x,y);
        if (ct == CellType.WALL) return null;
        if (ct == CellType.PATH && this.getPath(x,y) != PathStatus.UNKNOWN) return null;

        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1;
        MyMove mm2;

        if (ct == CellType.UNKNOWN) {
            mm1 = new MyMove(x,y,CellType.WALL);
            mm2 = new MyMove(x,y, CellType.PATH);
        } else {
            mm1 = new MyMove(x,y,PathStatus.NOTONPATH);
            mm2 = new MyMove(x,y,PathStatus.ONPATH);
        }

        mm1.applyMove(b1);
        mm2.applyMove(b2);

        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }
}
