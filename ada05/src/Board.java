import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.solverrecipes.singleloopflatten.SingleLoopBoard;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Board extends SingleLoopBoard<Board> implements MultiFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow int numgates;
    @Shallow Point startcell;
    @Shallow ClueType cluetype = ClueType.UNKNOWN;
    @Shallow GateManager gates;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        numgates = Integer.parseInt(gfr.getVar("STARTSIZE"));
        String[] parts = gfr.getVar("STARTLOC").split(" ");
        startcell = new Point(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));

        setClueType();

        gates = new GateManager(gfr);
        if (numgates != gates.getGateCount()) {
            throw new RuntimeException("Gate count " + gates.getGateCount() + " does not match start space count " + numgates);
        }

        init();

        if (gfr.hasBlock("SOLUTION")) {
            forEachCell((x,y)-> {
                String s = gfr.getBlock("SOLUTION")[x][y];
                if (s.contains("E")) setEdge(x,y,Direction.EAST,EdgeState.PATH);
                if (s.contains("S")) setEdge(x,y,Direction.SOUTH,EdgeState.PATH);
            });
        }

    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean isBlock(int x,int y) { return gfr.getBlock("BLOCKS")[x][y].charAt(0) == '*'; }
    public Point getStartCell() { return startcell; }

    public int getGateCount() { return numgates; }
    public Collection<Gate> getGates() { return gates.getGates(); }
    public Gate getGate(int id) { return gates.getGate(id); }
    public boolean isGate(int x,int y) { return gates.isGate(x,y); }
    public Gate getGate(int x,int y) { return gates.getGate(x,y); }

    public ClueType getClueType() { return cluetype; }
    public GateManager.GatePointer getGatePointer(int x,int y) { return gates.getGatePointer(x,y); }

    private void setClueType() {
        if (gfr.hasBlock("GATECLUES")) {
            cluetype = ClueType.GATECLUES;
        }

        if (gfr.hasBlock("LETTERS")) {
            if (cluetype != ClueType.UNKNOWN) {
                throw new RuntimeException("Can't have both GATECLUES and LETTERS");
            }
            cluetype = ClueType.CELLCLUES;
        }

        if (cluetype == ClueType.UNKNOWN) {
            throw new RuntimeException("Have to have one of GATECLUES or LETTERS");
        }
    }

    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean isLetter(int x,int y) { return getLetter(x,y) != '.'; }

    @Override public boolean isComplete() {
        return getUnknownCount() == 0;
    }

    private static class MyMove {
        int x;
        int y;
        Direction d;
        EdgeState es;

        public MyMove(int x,int y,Direction d, EdgeState es) {
            this.x = x; this.y = y;
            this.d = d; this.es = es;
        }

        public boolean applyMove(Board b) {
            if (b.getEdge(x,y,d) != EdgeState.UNKNOWN) {
                return b.getEdge(x,y,d) == es;
            }

            b.setEdge(x,y,d,es);
            return true;
        }
    }


    @Override public boolean applyMove(Object o) {
        return ((MyMove)o).applyMove(this);
    }

    private static Direction[] workdirs = new Direction[] { Direction.EAST, Direction.SOUTH };

    @Override public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();

        for (Direction d : workdirs) {
            if (getEdge(x,y,d) != EdgeState.UNKNOWN) continue;
            Board b1 = new Board(this);
            Board b2 = new Board(this);
            MyMove mm1 = new MyMove(x,y,d,EdgeState.PATH);
            MyMove mm2 = new MyMove(x,y,d,EdgeState.WALL);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
        }

        return result;
    }


}
