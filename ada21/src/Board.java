import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.GridPathContainer;
import grid.puzzlebits.Path.Path;
import grid.solverrecipes.singleloopflatten.BadMergeException;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Board implements MultiFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep EdgeContainer<EdgeState> edges;
    @Deep GridPathContainer paths;
    @Deep CellContainer<Boolean> articulating;
    @Shallow int unknowns;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        LambdaInteger unk = new LambdaInteger(0);

        articulating = new CellContainer<Boolean>(getWidth(),getHeight(),(x,y)->false);

        edges = new EdgeContainer<EdgeState>(getWidth(),getHeight(),EdgeState.WALL,
                (x,y,isV) -> { unk.inc(); return EdgeState.UNKNOWN; },
                (x,y,isV,old) -> old);

        unknowns = unk.get();

        paths = new GridPathContainer(getWidth(),getHeight(),
                (x,y,cell)-> {
                    // if we are here, it is because this cell has a new terminal path since were last here.
                    if (cell.getTerminalPaths().size() == 1) return;
                    if (cell.getInternalPaths().size() > 0) throw new BadMergeException("merging with middle of other path!");
                    if (isDot(x,y)) return; // we are building up the board out of paths between dots.  paths at dots don't join

                    // if we're not a dot, it's the same-ol thing of linking exactly two sub-paths
                    if (cell.getTerminalPaths().size() > 2) throw new BadMergeException("merging more than two!");

                    Path p1 = cell.getTerminalPaths().get(0);
                    Path p2 = cell.getTerminalPaths().get(1);

                    if (p1 == p2) throw new BadMergeException("no loops!");
                    else cell.merge(p1,p2);
                });

    }



    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public String getVar(String name) { return gfr.getVar(name); }
    public boolean isDot(int x, int y) { return !gfr.getBlock("DOTS")[x][y].equals("."); }

    public int getNumber(int x, int y) {
        return Integer.parseInt(gfr.getBlock("DOTS")[x][y].substring(0,1));
    }

    public char getLetter(int x, int y) {
        return gfr.getBlock("DOTS")[x][y].charAt(1);
    }

    public EdgeState getEdge(int x,int y, Direction d) { return edges.getEdge(x,y,d); }
    public EdgeState getEdge(int x,int y,boolean isV) { return edges.getEdge(x,y,isV); }

    public void setEdge(int x,int y,boolean isV,EdgeState es) {
        EdgeContainer.CellCoord cc = EdgeContainer.getCellCoord(x,y,isV);
        setEdge(cc.x,cc.y,cc.d,es);
    }

    public void setEdge(int x,int y, Direction d,EdgeState es) {
        if (es == EdgeState.UNKNOWN) throw new RuntimeException("no facility to unset!");
        if (getEdge(x,y,d) != EdgeState.UNKNOWN) throw new RuntimeException("don't set an unknown edge!");

        edges.setEdge(x,y,d,es);
        --unknowns;
        if (es == EdgeState.PATH) {
            Point me = new Point(x, y);
            Point op = d.delta(me, 1);
            paths.link(me, op);
        }
    }

    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean articulates(int x,int y) { return articulating.getCell(x,y); }
    public void setArticulates(int x,int y) { articulating.setCell(x,y,true); }
    public GridPathContainer getPaths() {
        return paths;
    }



    // FlattenSolvable stuff

    @Override public boolean isComplete() { return unknowns == 0; }

    private class MyMove {
        int x;
        int y;
        Direction d;
        EdgeState es;
        public MyMove(int x,int y,Direction d,EdgeState es) {this.x = x; this.y = y; this.es = es; this.d = d;}
        public boolean applyMove(Board b) {
            EdgeState cures = b.getEdge(x,y,d);
            if (cures != EdgeState.UNKNOWN) return es == cures;
            b.setEdge(x,y,d,es);
            return true;
        }
    }


    @Override public boolean applyMove(Object o) {
        return ((MyMove) o).applyMove(this);
    }

    private void getTupleForCellAndDirection(List<FlattenSolvableTuple<Board>> list, int x,int y, Direction d) {
        if (getEdge(x,y,d) != EdgeState.UNKNOWN) return;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,d,EdgeState.WALL);
        MyMove mm2 = new MyMove(x,y,d,EdgeState.PATH);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        list.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
    }


    @Override public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        getTupleForCellAndDirection(result,x,y,Direction.EAST);
        getTupleForCellAndDirection(result,x,y,Direction.SOUTH);
        return result;
    }



}
