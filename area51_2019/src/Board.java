import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.graph.GridGraph;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.solverrecipes.genericloopyflatten.LineState;
import grid.solverrecipes.genericloopyflatten.LoopyBoard;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Board extends CellEdgeTranslator implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep LoopyBoard loopy;
    @Deep CellContainer<CellState> cells;
    @Shallow int cell_unknowns;
    @Shallow List<LargeCell> largecells;
    @Shallow Map<Point,VertexClueColor> vertexClues = new HashMap<>();
    @Shallow List<EdgeCross> edgecrosses;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        LambdaInteger cunk = new LambdaInteger(0);
        cells = new CellContainer<CellState>(getWidth(),getHeight(),(x,y)->{
           if (hasPod(x,y)) return CellState.INSIDE;
           char cc = gfr.getBlock("CELLS")[x][y].charAt(0);
           if (cc == 'A') return CellState.INSIDE;
           if (cc == 'C') return CellState.OUTSIDE;
           cunk.inc();
           return CellState.UNKNOWN;
        });

        cell_unknowns = cunk.get();


        class UnspecifiedGridReference implements GridGraph.GridReference {
            @Override public int getWidth() { return Board.this.getWidth(); }
            @Override public int getHeight() { return Board.this.getHeight(); }
            @Override public boolean isIncludedCell(int x, int y) { return isBigClueCell(x,y); }
            @Override public boolean edgeExitsEast(int x, int y) { return true; }
            @Override public boolean edgeExitsSouth(int x, int y) { return true; }
        }

        largecells = new ArrayList<>();
        edgecrosses = new ArrayList<>();
        GridGraph gg = new GridGraph(new UnspecifiedGridReference());
        for (Set<Point> conset : gg.connectedSets()) {
            int clue = -1;
            for (Point p : conset) {
                if (!hasClue(p.x,p.y)) throw new RuntimeException("Large Cell at " + p.x +"," + p.y + " must have clue");
                int myclue = getClue(p.x,p.y);
                if (clue == -1) clue = myclue;
                else if (clue != myclue) throw new RuntimeException("Large Cell at " + p.x +"," + p.y + " must have all the same clue");
            }
            largecells.add(new LargeCell(clue,conset));
        }

        loopy = new LoopyBoard();
        forEachCell((x,y)->{
            String nwname = vertexName(x,y,Direction.NORTHWEST);
            String nename = vertexName(x,y,Direction.NORTHEAST);
            String swname = vertexName(x,y,Direction.SOUTHWEST);
            String sename = vertexName(x,y,Direction.SOUTHEAST);
            String nname = edgeName(x,y,Direction.NORTH);
            String sname = edgeName(x,y,Direction.SOUTH);
            String ename = edgeName(x,y,Direction.EAST);
            String wname = edgeName(x,y,Direction.WEST);
            Point p = new Point(x,y);

            if (x == 0 && !encumbered(x,y,Direction.WEST)) {
                loopy.addEdge(swname, nwname, wname);
                edgecrosses.add(new EdgeCross(p,Direction.WEST.delta(p,1),wname));
            }

            if (y == 0 && !encumbered(x,y,Direction.NORTH)) {
                loopy.addEdge(nename,nwname,nname);
                edgecrosses.add(new EdgeCross(p,Direction.NORTH.delta(p,1),nname));
            }

            if (!encumbered(x,y,Direction.EAST)) {
                loopy.addEdge(nename,sename,ename);
                edgecrosses.add(new EdgeCross(p,Direction.EAST.delta(p,1),ename));
            }

            if (!encumbered(x,y,Direction.SOUTH)) {
                loopy.addEdge(sename,swname,sname);
                edgecrosses.add(new EdgeCross(p,Direction.SOUTH.delta(p,1),sname));
            }

            String cclue = gfr.getBlock("CORNERS")[x][y];
            if (!cclue.equals(".")) {
                if (cclue.length() != 3) throw new RuntimeException("Illegal Corner Clue designator: " + cclue);
                String dirstring = cclue.substring(0, 2);
                char colchar = cclue.charAt(2);

                Direction d = Direction.fromShort(dirstring);
                VertexClueColor vcc = null;
                if (colchar == 'W') vcc = VertexClueColor.WHITE;
                if (colchar == 'B') vcc = VertexClueColor.BLACK;
                if (vcc == null) throw new RuntimeException("Illegal corner clue color: " + colchar);
                vertexClues.put(cellToVertex(x, y, d), vcc);
            }

            if (!isBigClueCell(x,y) && hasClue(x,y)) {
                int size = getClue(x,y);
                Set<String> eset = new HashSet<>();
                eset.add(nname); eset.add(sname); eset.add(ename); eset.add(wname);
                loopy.addClue(size,eset);
            }
        });

        for (LargeCell ls : largecells) {
            loopy.addClue(ls.cluesize,ls.getEdgeIdentifiers());
        }

        for (Point p : vertexClues.keySet()) {
            loopy.demandVertex(vertexName(p));
        }

    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public Board(Board right,LoopyBoard child) {
        this(right);
        this.loopy = child;
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public boolean hasPod(int x,int y) { return gfr.getBlock("PODS")[x][y].charAt(0) != '.'; }
    public boolean isBigClueCell(int x,int y) { return gfr.getBlock("CELLS")[x][y].charAt(0) == 'X'; }
    public int getPodValue(int x,int y) { return Integer.parseInt(gfr.getBlock("PODS")[x][y]); }
    public boolean hasClue(int x,int y) { return gfr.getBlock("CLUES")[x][y].charAt(0) != '.'; }
    public int getClue(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUES")[x][y]); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }
    public Stream<Point> stream() { return CellLambda.stream(getWidth(),getHeight()); }
    public List<EdgeCross> getEdgeCrosses() { return edgecrosses; }


    // to be called on a valid cell.  this is true if the cell is part of a big clue and the cell in the given direction
    // is either off the board or also part of a big clue
    public boolean encumbered(int x,int y, Direction d) {
        if (!isBigClueCell(x,y)) return false;
        Point dp = d.delta(x,y,1);
        if (!onBoard(dp)) return true;
        return isBigClueCell(dp.x,dp.y);
    }



    public CellState getCellState(int x,int y) { return cells.getCell(x,y); }
    public void setCellState(int x,int y,CellState cs) {
        --cell_unknowns;
        cells.setCell(x,y,cs);
    }


    public LineState getLineState(int x,int y, Direction d) {
        if (loopy.hasEdge(edgeName(x,y,d))) return loopy.getEdge(edgeName(x,y,d));
        return null;
    }

    public void setLineState(int x,int y,Direction d,LineState ls){
        if (loopy.hasEdge(edgeName(x,y,d))) loopy.setEdge(edgeName(x,y,d),ls);
    }

    public VertexClueColor getVertexClue(int x,int y,Direction d) {
        Point vp = cellToVertex(x,y,d);
        if (vertexClues.containsKey(vp)) return vertexClues.get(vp);
        return null;
    }


    @Override public boolean isComplete() { return loopy.isComplete() && cell_unknowns == 0; }

    private static class CellMove {
        int x;
        int y;
        CellState cs;
        public CellMove(int x,int y,CellState cs) { this.x = x; this.y = y; this.cs = cs; }
        public boolean applyMove(Board b) {
            if (b.getCellState(x,y) != CellState.UNKNOWN) return b.getCellState(x,y) == cs;
            b.setCellState(x,y,cs);
            return true;
        }
    }

    @Override public boolean applyMove(Object o) {
        if (o.getClass() == CellMove.class) return ((CellMove)o).applyMove(this);
        return loopy.applyMove(o);
    }

    private FlattenSolvableTuple<Board> makeIOTuple(int x,int y) {
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        CellMove cm1 = new CellMove(x,y,CellState.INSIDE);
        CellMove cm2 = new CellMove(x,y,CellState.OUTSIDE);
        cm1.applyMove(b1);
        cm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,cm1,b2,cm2);
    }


    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        List<FlattenSolvableTuple<LoopyBoard>> childSuccessors = loopy.getSuccessorTuples();

        for (FlattenSolvableTuple<LoopyBoard> subsuc : childSuccessors) {
            FlattenSolvableTuple<Board> parent = new FlattenSolvableTuple<>();
            for (int i = 0 ; i < subsuc.choices.size() ; ++i) {
                parent.addTuple(new Board(this,subsuc.choices.get(i)),subsuc.antimoves.get(i));
            }
            result.add(parent);
        }

        forEachCell((x,y)-> {
            if (getCellState(x,y) != CellState.UNKNOWN) return;
            result.add(makeIOTuple(x,y));
        });

        return result;
    }

    @Override public List<Board> guessAlternatives() {
        List<Board> result = new ArrayList<>();
        boolean unfound = terminatingForEachCell((x,y)-> {
            if (getCellState(x,y) != CellState.UNKNOWN) return true;
            FlattenSolvableTuple<Board> fsb = makeIOTuple(x,y);
            result.addAll(fsb.choices);
            return false;
        });

        if (!unfound) return result;
        List<LoopyBoard> lbl = loopy.guessAlternatives();
        lbl.stream().forEach(lb->result.add(new Board(this,lb)));

        return result;
    }



}
