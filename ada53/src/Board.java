import com.sun.org.apache.xpath.internal.operations.Bool;
import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import javafx.geometry.Pos;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Board implements StandardFlattenSolvable<Board>
{
    GridFileReader gfr;
    CellContainer<CellState> cellstates;
    CellContainer<PossiblePaths> possiblepaths;
    CellContainer<Path> setpaths;
    Map<Point,Circle> circles = new HashMap<>();

    public Board(String fname)
    {
        gfr = new GridFileReader(fname);
        cellstates = new CellContainer<CellState>(
                getWidth(),getHeight(),
                (x,y) -> ".".equals(gfr.getBlock("CIRCLES")[x][y]) ? CellState.EMPTY : CellState.INITIAL,
                (x,y,r) -> r
        );
        setpaths = new CellContainer<Path>(getWidth(),getHeight(),(x,y)->null, (x,y,r)->r);
        possiblepaths = new CellContainer<>(getWidth(),getHeight(),(x,y)->new PossiblePaths(),(x,y,r)->new PossiblePaths(r));

        forEachCell((x,y)-> {
            if (!hasCircleNumber(x,y)) return;
            circles.put(new Point(x,y),new Circle(x,y,this,getCircleNumber(x,y)));
        });
    }

    public Board(Board right) {
        gfr = right.gfr;
        cellstates = new CellContainer<CellState>(right.cellstates);
        setpaths = new CellContainer<Path>(right.setpaths);
        possiblepaths = new CellContainer<PossiblePaths>(right.possiblepaths);
        right.circles.keySet().stream().forEach((x)-> {
            circles.put(x,new Circle(right.circles.get(x),this));
        });
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public boolean isComplete() { return circles.values().stream().allMatch((c)->c.isLocked()); }

    private static class MyMove {
        Point p;
        Path path;

        public MyMove(Point p,Path path) { this.p = p; this.path = path; }
        public void applyMove(Board b) { b.circles.get(p).removeOnePath(path);}
    }


    @Override
    public boolean applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        mm.applyMove(this);
        return true;
    }

    // so if we get here, we know that all logic is stable
    // and therefore either a circle is locked or it has more than one path
    // (no paths would have failed already, and a single unlocked path would have been locked)
    @Override
    public FlattenSolvableTuple<Board> getOneTuple(int x, int y)
    {
        Point p = new Point(x,y);
        if (!circles.containsKey(p)) return null;
        Circle c = circles.get(p);
        if (c.isLocked()) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<Board>();
        c.paths.stream().forEach((path)->{
            Board nb = new Board(this);
            Circle nc = nb.circles.get(p);
            nc.removeAllPathsBesides(path);
            nc.lock();
            fst.addTuple(nb,new MyMove(p,path));
        });

        return fst;
    }

    public boolean hasCircleNumber(int x,int y) { return !".".equals(gfr.getBlock("CIRCLES")[x][y]); }
    public int getCircleNumber(int x,int y) {
        if (!hasCircleNumber(x,y)) throw new RuntimeException("Why?");
        String str = gfr.getBlock("CIRCLES")[x][y];
        if ("@".equals(str)) return -1;
        return Integer.parseInt(str);
    }
    public String getLetter(int x, int y) { return gfr.getBlock("LETTERS")[x][y]; }
    public boolean hasLetter(int x, int y) { return !".".equals(getLetter(x,y));}
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public CellState getCellState(int x,int y) { return cellstates.getCell(x,y);}
    public void setCellState(int x,int y,CellState cs) { cellstates.setCell(x,y,cs);}
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl);}
    public PossiblePaths getPossiblePaths(int x,int y) { return possiblepaths.getCell(x,y); }
    public void setSetPath(int x,int y,Path p) { setpaths.setCell(x,y,p);}
    public Path getSetPath(int x,int y) { return setpaths.getCell(x,y); }
}
