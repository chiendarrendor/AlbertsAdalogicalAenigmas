import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.graph.GridGraph;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.PointAdjacency;
import grid.puzzlebits.newpath.PathContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Board implements FlattenSolvable<Board>,PathContainer.PathContainerAssistant {
    @Shallow GridFileReader gfr;
    @Deep private EdgeContainer<EdgeState> edges;
    @Deep private PathContainer paths;
    @Shallow int unknowncount;
    @Shallow List<Set<Point>> iceregions;
    @Shallow Point entrance = null;
    @Shallow Point exit = null;

    private class MyListener implements GridGraph.GridReference {
        @Override public int getWidth() { return Board.this.getWidth(); }
        @Override public int getHeight() { return Board.this.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return hasIce(x,y); }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }




    public Board(String fname) {
        gfr = new GridFileReader(fname);
        LambdaInteger liunk = new LambdaInteger(0);
        edges = new EdgeContainer<EdgeState>(getWidth(),getHeight(),EdgeState.WALL,
                (x,y,isV)->{
                    liunk.inc();
                    return EdgeState.UNKNOWN;
                },
                (x,y,isV,r)->r
        );
        unknowncount = liunk.get();
        paths = new PathContainer(this);

        GridGraph gg = new GridGraph(new MyListener());
        iceregions = gg.connectedSets();

        forEachCell((x,y)-> {
            switch(getArrow(x,y)) {
                case '.': return;
                case 'I': entrance = new Point(x,y); return;
                case 'O': exit = new Point(x,y); return;
                case 'N':
                case 'S':
                case 'E':
                case 'W':
                    Direction d = Direction.fromShort(""+getArrow(x,y));
                    Point p = new Point(x,y);
                    Point op = d.delta(p,1);
                    edges.setEdge(x,y,d,EdgeState.PATH);
                    --unknowncount;
                    paths.newPair(p,op,false);
                    return;
                default:
                    throw new RuntimeException("Can't parse arrow type " + getArrow(x,y));
            }
        });

        if (entrance == null || exit == null) throw new RuntimeException("Board must have both entrance and exit");
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public char getLetter(int x, int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x, int y)  { return getLetter(x,y) != '.'; }
    public char getArrow(int x,int y) { return gfr.getBlock("ARROWS")[x][y].charAt(0); }
    public boolean hasIce(int x,int y) { return gfr.getBlock("ICE")[x][y].charAt(0) == 'X'; }
    public List<Set<Point>> getIceRegions() { return iceregions; }
    public Point getEntrance() { return entrance; }
    public Point getExit() { return exit; }
    public PathContainer getPaths() { return paths; }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public EdgeState getEdge(int x,int y,boolean isV) { return edges.getEdge(x,y,isV); }
    public EdgeState getEdge(int x,int y, Direction d) { return edges.getEdge(x,y,d); }

    public void setWall(int x,int y,boolean isV) {
        EdgeContainer.CellCoord cc = EdgeContainer.getCellCoord(x,y,isV);
        setWall(cc.x,cc.y,cc.d);
    }

    public void setPath(int x,int y,boolean isV) {
        EdgeContainer.CellCoord cc = EdgeContainer.getCellCoord(x,y,isV);
        setPath(cc.x,cc.y,cc.d);
    }


    public void setWall(int x,int y, Direction d) {
        edges.setEdge(x,y,d,EdgeState.WALL);
        --unknowncount;
    }

    private void terminusCheckSetPath(Point p1,Point p2) {
        boolean reversible = true;
        boolean doswap = false;

        if (p1.equals(entrance)) { reversible = false; }
        if (p2.equals(entrance)) { reversible = false; doswap = true; }
        if (p1.equals(exit)) { reversible = false; doswap = true; }
        if (p2.equals(exit)) { reversible = false; }

        if (doswap) { Point t = p1; p1 = p2; p2 = t; }
        paths.newPair(p1,p2,reversible);
    }



    public void setPath(int x,int y,Direction d) {
        edges.setEdge(x,y,d,EdgeState.PATH);
        --unknowncount;
        Point np = d.delta(x,y,1);
        terminusCheckSetPath(new Point(x,y),np);
    }

    //PathContainerAssistant
    @Override public boolean isLinkable(PathContainer.Port porta, PathContainer.Port portb) {
        if (hasIce(porta.getLocation().x,porta.getLocation().y)) {
            return (porta.getDirection() == portb.getDirection().getOpp());
        }
        return true;
    }

    @Override public int maxPorts(int x, int y) { return hasIce(x,y) ? 4 : 2;  }
    @Override public int maxUnlinked(int x, int y) { return hasIce(x,y) ? 2 : 0; }



    @Override public boolean isComplete() { return unknowncount == 0; }
    private static class MyMove {
        int x;
        int y;
        boolean isV;
        EdgeState es;
        public MyMove(int x,int y,boolean isV,EdgeState es){ this.x = x; this.y = y; this.isV = isV ; this.es = es; }
        public boolean applyMove(Board b) {
            if (b.getEdge(x,y,isV) != EdgeState.UNKNOWN) return b.getEdge(x,y,isV) == es;
            if (es == EdgeState.WALL) b.setWall(x,y,isV);
            else b.setPath(x,y,isV);
            return true;
        }
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    private List<FlattenSolvableTuple<Board>> getTuples(boolean getAll) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        edges.forEachEdge((x,y,isV,old)->{
            if (!getAll && result.size() > 0) return;
            if (old != EdgeState.UNKNOWN) return;
            Board b1 = new Board(this);
            Board b2 = new Board(this);
            MyMove mm1 = new MyMove(x,y,isV,EdgeState.PATH);
            MyMove mm2 = new MyMove(x,y,isV,EdgeState.WALL);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
        });
        return result;
    }

    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() { return getTuples(true); }
    @Override public List<Board> guessAlternatives() { return getTuples(false).get(0).choices; }
}
