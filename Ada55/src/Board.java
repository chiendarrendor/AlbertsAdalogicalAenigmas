import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Board implements FlattenSolvable<Board> {
    private GridFileReader gfr;
    private CellContainer<Integer> numbers;
    private EdgeContainer<EdgeState> edges;
    int unknown;
    int minNumber;
    int maxNumber;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        int[] minmax = new int[2];
        minmax[0] = Integer.MAX_VALUE;
        minmax[1] = Integer.MIN_VALUE;

        numbers = new CellContainer<Integer>(getWidth(),getHeight(),
                (x,y) -> {
                    String istring = gfr.getBlock("NUMBERS")[x][y];
                    if (istring.equals(".")) return null;
                    int result = Integer.parseInt(istring);
                    if (result < minmax[0]) minmax[0] = result;
                    if (result > minmax[1]) minmax[1] = result;
                    return result;
                },
                (x,y,r)->r
        );
        minNumber = minmax[0];
        maxNumber = minmax[1];

        int[] ucount = new int[1];
        ucount[0] = 0;
        edges = new EdgeContainer<EdgeState>(getWidth(),getHeight(),EdgeState.WALL,
                (x,y,isV)->{ ++ucount[0]; return EdgeState.UNKNOWN; },
                (x,y,isV,old)->old
        );
        unknown = ucount[0];
    }

    public Board(Board right) {
        gfr = right.gfr;
        numbers = right.numbers;
        edges = new EdgeContainer<EdgeState>(right.edges);
        unknown = right.unknown;
        minNumber = right.minNumber;
        maxNumber = right.maxNumber;
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasNumber(int x,int y) { return numbers.getCell(x,y) != null; }
    public int getNumber(int x,int y) { return numbers.getCell(x,y); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public EdgeState getEdge(int x,int y,Direction d) { return edges.getEdge(x,y,d); }
    public void setEdge(int x,int y,Direction d,EdgeState es) { edges.setEdge(x,y,d,es); --unknown; }
    public void setEdge(int x,int y, boolean isV,EdgeState es) { edges.setEdge(x,y,isV,es); --unknown; }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y);}
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public int getMin() { return minNumber; }
    public int getMax() { return maxNumber; }
    public EdgeContainer<EdgeState> getEdges() { return edges; }
    public String getSolution() { return gfr.getVar("SOLUTION");}

    // --------------------- FlattenSolvable requires --------------------------
    @Override
    public boolean isComplete() {
        return unknown == 0;
    }

    public static class MyMove {
        int x;
        int y;
        Direction d;
        EdgeState es;
        public MyMove(int x,int y,Direction d,EdgeState es) { this.x = x; this.y = y; this.d = d; this.es = es; }
        public boolean applyMove(Board b) {
            if (b.getEdge(x,y,d) == EdgeState.UNKNOWN) {
                b.setEdge(x,y,d,es);
                return true;
            }
            return b.getEdge(x,y,d) == es;
        }
    }



    @Override
    public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {

        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();

        forEachCell( (x,y) -> {
            for (Direction d : Utility.OPDIRS) {
                if (getEdge(x,y,d) != EdgeState.UNKNOWN) continue;

                Board b1 = new Board(this);
                MyMove mm1 = new MyMove(x,y,d,EdgeState.WALL);
                Board b2 = new Board(this);
                MyMove mm2 = new MyMove(x,y,d,EdgeState.PATH);
                mm1.applyMove(b1);
                mm2.applyMove(b2);
                result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
            }
        });
        return result;
    }

    @Override
    public boolean applyMove(Object o) {
        MyMove mm = (MyMove)o;
        return mm.applyMove(this);
    }


    // Heuristic:  cells where numbers are and cells that already have a lot
    // of their edges set are likely to be more interesting...
    //

    static int NUMBERSCORE = 10;
    static int WALLSCORE = 10;
    static int PATHSCORE = 10;

    public MyMove[] makeGuess() {
        MyMove[] move = new MyMove[2];
        move[0] = null;
        move[1] = null;
        int[] bestscore = new int[1];
        bestscore[0] = 0;

        forEachCell((x,y) -> {
            for (Direction d : Utility.OPDIRS) {
                if (getEdge(x, y, d) != EdgeState.UNKNOWN) continue;

                Point cp = new Point(x,y);
                Point nsp = d.delta(cp,1);

                int score = 0;
                if (hasNumber(cp.x,cp.y)) score += NUMBERSCORE;
                if (hasNumber(nsp.x,nsp.y)) score += NUMBERSCORE;
                for(Direction rd : Direction.orthogonals()) {
                    if (getEdge(cp.x,cp.y,rd) == EdgeState.WALL) score += WALLSCORE;
                    if (getEdge(nsp.x,nsp.y,rd) == EdgeState.WALL) score += WALLSCORE;
                    if (getEdge(cp.x,cp.y,rd) == EdgeState.PATH) score += PATHSCORE;
                    if (getEdge(nsp.x,nsp.y,rd) == EdgeState.PATH) score += PATHSCORE;
                }

                if (move[0] == null || score > bestscore[0]) {
                    bestscore[0] = score;
                    move[0] = new MyMove(x,y,d,EdgeState.WALL);
                    move[1] = new MyMove(x,y,d,EdgeState.PATH);
                }
            }
        });

        if (move[0] == null) throw new RuntimeException("Called guessAlternatives on a complete board!?");
        return move;
    }



    @Override
    public List<Board> guessAlternatives() {
        MyMove move[] = makeGuess();

        List<Board> successors = new ArrayList<>();
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        move[0].applyMove(b1);
        move[1].applyMove(b2);
        successors.add(b1);
        successors.add(b2);



        return successors;
    }







}
