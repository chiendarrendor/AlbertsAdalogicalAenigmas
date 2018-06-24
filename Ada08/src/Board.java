import grid.copycon.Deep;
import grid.copycon.CopyCon;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.GridPathContainer;
import grid.puzzlebits.Path.Path;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Board implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow CellContainer<ArrowInfo> arrows;
    @Deep CellContainer<CellState> cells;
    @Deep EdgeContainer<EdgeState> edges;
    @Shallow int unknowncells;
    @Shallow int unknownedges;
    @Deep GridPathContainer paths;


    public Board(String filename) {
        gfr = new GridFileReader(filename);

        arrows = new CellContainer<ArrowInfo>(getWidth(),getHeight(),
                (x,y)-> {
                    String str = gfr.getBlock("ARROWS")[x][y];
                    if (str.equals(".")) return null;
                    return new ArrowInfo(str);
                });

        LambdaInteger ucli = new LambdaInteger(0);
        cells = new CellContainer<CellState>(getWidth(),getHeight(),
                (x,y)-> {
                    if (arrows.getCell(x,y) != null) {
                        return CellState.ARROW;
                    } else {
                        ucli.inc();
                        return CellState.UNKNOWN;
                    }
                });
        unknowncells = ucli.get();

        LambdaInteger ueli = new LambdaInteger(0);
        edges = new EdgeContainer<EdgeState>(getWidth(),getHeight(),EdgeState.WALL,
                (x,y,isV)-> {
                    ueli.inc();
                    return EdgeState.UNKNOWN;
                },
                (x,y,isV,old) -> old
        );
        unknownedges = ueli.get();

        paths = new GridPathContainer(getWidth(),getHeight(),(x,y,cell)->{
           if (cell.getInternalPaths().size() > 0) throw new BadMergeException("Merging with middle of other path!");
           if (cell.getTerminalPaths().size() > 2) throw new BadMergeException("merging more than two!");
           if (cell.getTerminalPaths().size() == 1) return;

           Path p1 = cell.getTerminalPaths().get(0);
           Path p2 = cell.getTerminalPaths().get(1);

           if (p1 == p2) cell.closeLoop(p1);
           else cell.merge(p1,p2);
        });


        int gidx = 1;
        while (hasGuess(gidx)) {
            String g = getGuess(gidx);
            String[] parts = g.split(" ");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            CellState cs = Enum.valueOf(CellState.class,parts[2]);
            setCell(x,y,cs);

            ++gidx;
        }



    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }


    public boolean hasGuess(int x) { return gfr.hasVar("GUESS" + x); }
    public String getGuess(int x) { return gfr.getVar("GUESS" + x); }
    public String getSolution(int x) { return gfr.getVar("SOLUTION" + x); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }

    public GridPathContainer getPaths() { return paths;  }

    public ArrowInfo getArrowInfo(int x, int y) { return arrows.getCell(x,y); }
    public CellState getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x, int y, CellState cs) {
        if (cs != CellState.UNKNOWN && getCell(x,y) == CellState.UNKNOWN) --unknowncells;
        if (cs == CellState.UNKNOWN && getCell(x,y) != CellState.UNKNOWN) ++unknowncells;
        cells.setCell(x,y,cs);
    }
    public EdgeState getEdge(int x, int y, boolean isV) { return edges.getEdge(x,y,isV); }
    public EdgeState getEdge(int x, int y, Direction d) { return edges.getEdge(x,y,d); }

    public void setEdge(int x, int y, boolean isV, EdgeState es) {
        EdgeContainer.CellCoord cc = EdgeContainer.getCellCoord(x,y,isV);
        setEdge(cc.x,cc.y,cc.d,es);
    }
    public void setEdge(int x, int y, Direction d, EdgeState es) {
        if (es != EdgeState.UNKNOWN && getEdge(x,y,d) == EdgeState.UNKNOWN) --unknownedges;
        if (es == EdgeState.UNKNOWN && getEdge(x,y,d) != EdgeState.UNKNOWN) ++unknownedges;

        edges.setEdge(x,y,d,es);

        // this only works correctly until we allow setting to unknown because linking isn't a thing.
        if (es != EdgeState.PATH) return;
        Point p1 = new Point(x,y);
        Point p2 = d.delta(p1,1);
        paths.link(p1,p2);
    }

    @Override
    public boolean isComplete() { return unknowncells + unknownedges == 0; }



    @Override
    public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();

        forEachCell((x,y)->{
            if (getCell(x,y) != CellState.UNKNOWN) return;

            MyMove mm1 = new MyMove(x,y,CellState.PATH);
            MyMove mm2 = new MyMove(x,y,CellState.WALL);
            Board b1 = new Board(this); mm1.applyMove(b1);
            Board b2 = new Board(this); mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
        });

        edges.forEachEdge((x,y,isV,cur) -> {
            if (cur != EdgeState.UNKNOWN) return;
            MyMove mm1 = new MyMove(x,y,isV,EdgeState.PATH);
            MyMove mm2 = new MyMove(x,y,isV,EdgeState.WALL);
            Board b1 = new Board(this); mm1.applyMove(b1);
            Board b2 = new Board(this); mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
        });


        return result;
    }

    public static class MyMove {
        int x;
        int y;
        boolean isV;
        boolean isEdge;
        CellState cs;
        EdgeState es;
        public MyMove(int x, int y, CellState cs) {
            this.x = x;
            this.y = y;
            this.isEdge = false;
            this.cs = cs;
        }

        public MyMove(int x, int y, boolean isV, EdgeState es) {
            this.x = x;
            this.y = y;
            this.isEdge = true;
            this.isV = isV;
            this.es = es;
        }

        public boolean applyMove(Board thing) {
            if (isEdge) {
                EdgeState cures = thing.getEdge(x,y,isV);
                if (cures != EdgeState.UNKNOWN) return cures == es;
                thing.setEdge(x,y,isV,es);
                return true;
            } else {
                CellState curcs = thing.getCell(x,y);
                if (curcs != CellState.UNKNOWN) return curcs == cs;
                thing.setCell(x,y,cs);
                return true;
            }
        }


    }




    @Override
    public boolean applyMove(Object o) {
        MyMove mm = (MyMove)o;
        return mm.applyMove(this);
    }

    @Override
    public List<Board> guessAlternatives() {
        List<FlattenSolvableTuple<Board>> stuff = getSuccessorTuples();
        return stuff.get(0).choices;
    }
}
