package grid.solverrecipes.singleloopflatten;

import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.GridPathContainer;
import grid.puzzlebits.Path.Path;
import java.awt.Point;

public abstract class SingleLoopBoard<T> implements FlattenSolvable<T> {
    @Deep private EdgeContainer<EdgeState> edges;
    @Deep private GridPathContainer paths;
    @Shallow private int unknowns;

    public SingleLoopBoard() {}


    protected void init() {
        LambdaInteger liunk = new LambdaInteger(0);
        edges = new EdgeContainer<EdgeState>(getWidth(),getHeight(),EdgeState.WALL,
                (x,y,isV)->{ liunk.inc(); return EdgeState.UNKNOWN; },
                (x,y,isV,r)->r);
        unknowns = liunk.get();
        paths = new GridPathContainer(getWidth(),getHeight(),
                (x,y,cell)-> {
                    if (cell.getInternalPaths().size() > 0) throw new BadMergeException("merging with middle of other path!");
                    if (cell.getTerminalPaths().size() > 2) throw new BadMergeException("merging more than two!");
                    if (cell.getTerminalPaths().size() == 1) return;

                    Path p1 = cell.getTerminalPaths().get(0);
                    Path p2 = cell.getTerminalPaths().get(1);

                    if (p1 == p2) cell.closeLoop(p1);
                    else cell.merge(p1,p2);
                });
    }

    protected void init(SingleLoopBoard right) {
        CopyCon.copy(this,right);
    }

    public Iterable<Path> getPaths() { return paths; }
    public void cleanPaths() { paths.clean(); }

    public EdgeState getEdge(int x, int y, Direction d) { return edges.getEdge(x,y,d); }
    public EdgeState getEdge(int x, int y, boolean isV) { return edges.getEdge(x,y,isV); }

    public void setEdge(int x,int y,Direction d,EdgeState es) {
        if (es == EdgeState.UNKNOWN) throw new RuntimeException("unsetting edge is unsupported");
        if (getEdge(x,y,d) != EdgeState.UNKNOWN) throw new RuntimeException("setting already set edge is unsupported");

        --unknowns;
        edges.setEdge(x,y,d,es);
        Point p1 = new Point(x,y);
        Point p2 = d.delta(p1,1);
        if (es == EdgeState.PATH) paths.link(p1,p2);
    }

    public void setEdge(int x,int y,boolean isV,EdgeState es) {
        EdgeContainer.CellCoord cc = EdgeContainer.getCellCoord(x,y,isV);
        setEdge(cc.x,cc.y,cc.d,es);
    }

    public int getUnknownCount() { return unknowns; }
    public void forEachEdge(EdgeContainer.Operator<EdgeState> op) { edges.forEachEdge(op); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }


    // required methods for class to work
    public abstract int getWidth();
    public abstract int getHeight();

}
