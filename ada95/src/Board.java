import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.LambdaInteger;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Board implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow CornerSpace corners;
    @Deep EdgeContainer<EdgeState> edges;
    @Shallow int unknowns;
    @Deep Districts districts;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        corners = new CornerSpace(getWidth(), getHeight());
        LambdaInteger liunk = new LambdaInteger(0);
        edges = new EdgeContainer<EdgeState>(getWidth(), getHeight(), EdgeState.WALL, (x, y, isV) -> {
            liunk.inc();
            return EdgeState.UNKNOWN;
        }, (x, y, isV, r) -> r);
        unknowns = liunk.get();

        districts = new Districts(getWidth(),getHeight(),this);
    }


    public Board(Board right) {
        CopyCon.copy(this, right);
    }

    public int getWidth() {
        return gfr.getWidth();
    }

    public int getHeight() {
        return gfr.getHeight();
    }

    public boolean hasLetter(int x, int y) {
        return !gfr.getBlock("LETTERS")[x][y].equals(".");
    }

    public boolean hasNumber(int x, int y) {
        return !gfr.getBlock("NUMBERS")[x][y].equals(".");
    }

    public boolean hasDot(int x, int y) {
        return !gfr.getBlock("DOTS")[x][y].equals(".");
    }

    public char getLetter(int x, int y) {
        return gfr.getBlock("LETTERS")[x][y].charAt(0);
    }

    public int getNumber(int x, int y) {
        return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]);
    }

    public Direction getDot(int x, int y) {
        return Direction.fromShort(gfr.getBlock("DOTS")[x][y]);
    }

    public CornerSpace getCornerSpace() { return corners; }

    // this is gettting presence of corner, given a corner in corner space.
    public boolean hasDotInCorner(int x,int y) {
        Map<Direction, Point> cellsOfCorner = corners.getCornerCells(x,y);

        for (Map.Entry<Direction, Point> ent : cellsOfCorner.entrySet()) {
            if (!hasDot(ent.getValue().x, ent.getValue().y)) continue;
            Direction cellClue = getDot(ent.getValue().x, ent.getValue().y);
            Direction workdir = ent.getKey();
            Direction workopp = workdir.getOpp();
            if (cellClue == workopp) return true;
        }
        return false;
    }

    // given a corner direction for a cell, does that cell have a dot in it?
    public boolean hasDotInCorner(int x, int y, Direction d) {
        Point cornerCoords = corners.getCornerOfCell(x, y, d);
        return hasDotInCorner(cornerCoords.x,cornerCoords.y);
    }

    public EdgeState getEdge(int x, int y, Direction d) {
        return edges.getEdge(x, y, d);
    }

    public EdgeState getEdge(int x, int y, boolean isV) {
        return edges.getEdge(x, y, isV);
    }

    public void setEdge(int x, int y, Direction d, EdgeState es) {
        EdgeContainer.EdgeCoord ec = new EdgeContainer.EdgeCoord(new EdgeContainer.CellCoord(x,y,d));
        setEdge(ec.x,ec.y,ec.isV,es);
    }

    public void setEdge(int x, int y, boolean isV, EdgeState es) {
        --unknowns;
        edges.setEdge(x, y, isV, es);
        if (es == EdgeState.PATH) {
            districts.addPath(new EdgeContainer.EdgeCoord(x,y,isV));
        } else {
            districts.addWall(new EdgeContainer.EdgeCoord(x,y,isV));
        }
    }

    public District getDistrict(int x,int y) {
        return districts.districtsByCell.getCell(x,y);
    }


    private static class MyMove {
        int x;
        int y;
        boolean isV;
        EdgeState es;

        public MyMove(int x, int y, boolean isV, EdgeState es) {
            this.x = x;
            this.y = y;
            this.isV = isV;
            this.es = es;
        }

        public boolean applyMove(Board b) {
            if (b.getEdge(x, y, isV) != EdgeState.UNKNOWN) return es == b.getEdge(x, y, isV);
            b.setEdge(x, y, isV, es);
            return true;
        }
    }

    @Override public boolean isComplete() {return unknowns == 0;}

    @Override public boolean applyMove(Object o) {return ((MyMove) o).applyMove(this);}

    private FlattenSolvableTuple<Board> getTupleForEdge(int x, int y, boolean isV) {
        if (getEdge(x, y, isV) != EdgeState.UNKNOWN) return null;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x, y, isV, EdgeState.PATH);
        MyMove mm2 = new MyMove(x, y, isV, EdgeState.WALL);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1, mm1, b2, mm2);
    }

    private List<FlattenSolvableTuple<Board>> getAllTuples(final boolean onlyOne) {
        final List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        edges.booleanForEachEdge((x, y, isV, r) -> {
            FlattenSolvableTuple<Board> fst = getTupleForEdge(x, y, isV);
            if (fst == null) return true;
            result.add(fst);
            return !onlyOne;
        });
        return result;
    }

    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {return getAllTuples(false);}

    @Override public List<Board> guessAlternatives() {return getAllTuples(true).get(0).choices;}
}
