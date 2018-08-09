import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.GridPathContainer;
import grid.puzzlebits.Path.Path;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Board implements MultiFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow Point[] starts;
    @Deep EdgeContainer<EdgeState> edges;
    @Deep GridPathContainer paths;
    @Shallow int unknownedges;
    @Shallow int lowterminal;
    @Shallow int highterminal;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        int scount = Integer.parseInt(gfr.getVar("LINECOUNT"));
        starts = new Point[scount];
        for (int i = 0 ; i < scount ; ++i) {
            String vname = "LINE" + (i+1);
            String[] cpair = gfr.getVar(vname).split(" ");
            starts[i] = new Point(Integer.parseInt(cpair[0]),Integer.parseInt(cpair[1]));
        }

        LambdaInteger unk = new LambdaInteger(0);
        edges = new EdgeContainer<EdgeState>(getWidth(),getHeight(), EdgeState.WALL,
                (x,y,isV)-> { unk.inc(); return EdgeState.UNKNOWN; },
                (x,y,isV,old) -> old);
        unknownedges = unk.get();

        paths = new GridPathContainer(getWidth(),getHeight(),
                (x,y,cell)-> {
                    if (cell.getInternalPaths().size() > 0) throw new BadMergeException("merging with middle of other path!");
                    if (cell.getTerminalPaths().size() > 2) throw new BadMergeException("merging more than two!");
                    if (cell.getTerminalPaths().size() == 1) return;

                    Path p1 = cell.getTerminalPaths().get(0);
                    Path p2 = cell.getTerminalPaths().get(1);
                    if (p1 == p2) throw new BadMergeException("loops are not allowed");

                    cell.merge(p1,p2);
                });

        lowterminal = Integer.parseInt(gfr.getVar("LOWTERMINAL"));
        highterminal = Integer.parseInt(gfr.getVar("HIGHTERMINAL"));



    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasNumber(int x,int y) { return gfr.getBlock("NUMBERS")[x][y].charAt(0) != '.'; }
    public int getNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }

    public boolean isTerminal(int x,int y) {
        return hasNumber(x, y) && (getNumber(x, y) == lowterminal || getNumber(x, y) == highterminal);
    }
    public int getLowTerminal() { return lowterminal; }

    public int getStartCount() { return starts.length; }
    public Point getStart(int idx) { return starts[idx]; }
    public boolean isStart(int x,int y) { return Arrays.stream(starts).anyMatch(p->p.x==x && p.y==y);}

    public EdgeState getEdge(int x,int y,Direction d) { return edges.getEdge(x,y,d); }
    public void setEdge(int x,int y, Direction d, EdgeState es) {
        EdgeState ces = getEdge(x,y,d);

        if (ces == EdgeState.UNKNOWN && es != EdgeState.UNKNOWN) --unknownedges;
        if (ces != EdgeState.UNKNOWN && es == EdgeState.UNKNOWN) ++unknownedges;
        edges.setEdge(x,y,d,es);

        if (es == EdgeState.PATH) {
            Point p1 = new Point(x,y);
            Point p2 = d.delta(p1,1);
            paths.link(p1,p2);
        }

    }

    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }




    @Override public boolean isComplete() { return unknownedges == 0; }

    private static class MyMove {
        int x;
        int y;
        Direction d;
        EdgeState es;
        public MyMove(int x,int y,Direction d, EdgeState es) { this.x = x; this.y = y; this.d = d; this.es = es; }

        public boolean applyMove(Board o) {
            EdgeState ces = o.getEdge(x,y,d);
            if (ces != EdgeState.UNKNOWN) return ces == es;
            o.setEdge(x,y,d,es);
            return true;
        }
    }


    @Override public boolean applyMove(Object o) {
        MyMove mm = (MyMove)o;
        return mm.applyMove(this);
    }

    private static final Direction[] dirs = new Direction[] { Direction.EAST,Direction.SOUTH };

    @Override public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();

        for (Direction d : dirs) {
            if (getEdge(x,y,d) != EdgeState.UNKNOWN) continue;
            Board b1 = new Board(this);
            Board b2 = new Board(this);
            MyMove mm1 = new MyMove(x,y,d,EdgeState.WALL);
            MyMove mm2 = new MyMove(x,y,d,EdgeState.PATH);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
        }
        return result;
    }

}
