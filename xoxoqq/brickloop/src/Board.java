import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.solverrecipes.genericloopyflatten.LineState;
import grid.solverrecipes.genericloopyflatten.LoopyBoard;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Board implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow CellContainer<CellLines> cells;
    @Deep LoopyBoard subboard;

    private static String cs(int x,int y) { return "(" + x + "," + y + ")"; }

    private static Direction[] GTOP = new Direction[] { Direction.NORTHEAST,Direction.NORTH,Direction.NORTHWEST};
    private static Direction[] GBOT = new Direction[] { Direction.SOUTHWEST,Direction.SOUTH,Direction.SOUTHEAST};
    private static Direction[] GLFT = new Direction[] { Direction.NORTHWEST,Direction.SOUTHWEST};
    private static Direction[] GCTR = new Direction[] { Direction.NORTH,Direction.SOUTH};
    private static Direction[] GRGT = new Direction[] { Direction.NORTHEAST,Direction.SOUTHEAST};
    private static boolean in(Direction[] dar,Direction d) { return Arrays.stream(dar).anyMatch(td->td==d); }


    private static String grids(int cx,int cy, Direction d) {
        int x = 2*cx;
        int y = cy;

        if(in(GBOT,d)) ++y;
        if(in(GCTR,d)) ++x;
        if(in(GRGT,d)) x+=2;
        if (cy % 2 == 1) ++x;
        return cs(x,y);
    }

    private static Direction[] TOPV = new Direction[] { Direction.NORTHWEST,Direction.NORTHEAST};
    private static Direction[] BOTV = new Direction[] { Direction.SOUTHWEST,Direction.SOUTHEAST};
    private static Direction[] EV = new Direction[] {Direction.NORTHEAST,Direction.SOUTHEAST};
    private static Direction[] WV = new Direction[] {Direction.NORTHWEST,Direction.SOUTHWEST};
    private static Direction[] NWV = new Direction[] {Direction.NORTHWEST,Direction.NORTH};
    private static Direction[] NEV = new Direction[] {Direction.NORTH,Direction.NORTHEAST};
    private static Direction[] SWV = new Direction[] {Direction.SOUTHWEST,Direction.SOUTH};
    private static Direction[] SEV = new Direction[] {Direction.SOUTH,Direction.SOUTHEAST};

    private static Direction[] lvDirections(String dir) {
        if (dir.equals("TOP")) return TOPV;
        if (dir.equals("BOT")) return BOTV;
        switch(Direction.fromShort(dir)) {
            case WEST: return WV;
            case EAST: return EV;
            case NORTHWEST: return NWV;
            case NORTHEAST: return NEV;
            case SOUTHWEST: return SWV;
            case SOUTHEAST: return SEV;
            default: throw new RuntimeException("Unknown dir " + dir + " in lvDirections");
        }
    }

    private void addEdge(CellLines cline,Direction cd,int tx,int ty, String td,boolean dosub) {
        try {
            String edgename = cs(tx, ty) + td;
            cline.addLine(cd, edgename);
            String v1 = grids(tx, ty, lvDirections(td)[0]);
            String v2 = grids(tx, ty, lvDirections(td)[1]);
            if (dosub) subboard.addEdge(v1, v2, edgename);
        } catch (Exception ex) {
            throw new RuntimeException("LoopyBoard exception caught: (" + cline.x + "," + cline.y + ") "
                    + cd + ": " + tx + " " + ty + " " + td + "   " + dosub,ex);
        }
    }

    private void addEdge(CellLines cline,Direction cd,int tx,int ty, String td) { addEdge(cline,cd,tx,ty,td,true); }

    private void addEdgeAlt(CellLines cline,Direction cd,int txa,int tya,String tda,int txb,int tyb,String tdb) {
        if (onBoard(txa,tya)) addEdge(cline,cd,txa,tya,tda,false);
        else addEdge(cline,cd,txb,tyb,tdb);
    }


    private CellLines makeNewCell(int x,int y) {
        if (!onBoard(x,y)) return null;
        CellLines result = new CellLines(x,y);

        int nwx,nwy;
        int nex,ney;
        int wx,wy;
        int swx,swy;
        int sex,sey;

        ney = nwy = y-1;
        wy = y;
        sey = swy = y+1;
        wx = x-1;

        if (y%2 == 0) {
            nwx = swx = x-1;
            nex = sex = x;
        } else {
            nwx = swx = x;
            nex = sex = x+1;
        }

        addEdge(result,Direction.EAST,x,y,"E");
        addEdgeAlt(result,Direction.WEST,wx,wy,"E",x,y,"W");

        if (!onBoard(nwx,nwy) && !onBoard(nex,ney)) {
            addEdge(result,Direction.NORTHEAST,x,y,"TOP");
            addEdge(result,Direction.NORTHWEST,x,y,"TOP",false);
        } else {
            addEdgeAlt(result,Direction.NORTHWEST,nwx,nwy,"SE",x,y,"NW");
            addEdgeAlt(result,Direction.NORTHEAST,nex,ney,"SW",x,y,"NE");
        }

        if (!onBoard(sex,sey) && !onBoard(swx,swy)) {
            addEdge(result,Direction.SOUTHWEST,x,y,"BOT");
            addEdge(result,Direction.SOUTHEAST,x,y,"BOT",false );
        } else {
            addEdge(result,Direction.SOUTHEAST,x,y,"SE");
            addEdge(result,Direction.SOUTHWEST,x,y,"SW");
        }


        return result;

    }





    public Board(String fname) {
        gfr = new GridFileReader(fname);
        subboard = new LoopyBoard();

        try {
            cells = new CellContainer<CellLines>(getWidth(0), getHeight(), (x, y) -> {
                return makeNewCell(x, y);
            });
        } catch (Exception ex) {
            subboard.showStructure();
            subboard.showState();
            throw ex;
        }

        cells.forEachCell((x,y)-> {
            if (cells.getCell(x,y) != null) {
                cells.getCell(x, y).verifyLines();
            }
        });




    }

    public Board(Board right) {
        CopyCon.copy(this, right);
    }

    public Board(Board right,LoopyBoard newsub) {
        gfr = right.gfr;
        cells = right.cells;
        subboard = newsub;
    }



    public int getHeight() {        return gfr.getHeight();    }
    public int getWidth(int y) {        return gfr.getWidth() - ((y % 2 == 0) ? 0 : 1);    }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(gfr.getWidth(),gfr.getHeight(),xyl); }

    public boolean onBoard(int x,int y) {
        if (!gfr.inBounds(x,y)) return false;
        if (y%2 == 1 && x == gfr.getWidth() - 1) return false;
        return true;
    }

    public char getLetter(int x, int y) {        return gfr.getBlock("LETTERS")[x][y].charAt(0);    }
    public boolean hasClue(int x, int y) {        return !gfr.getBlock("CLUES")[x][y].equals(".");    }
    public int getClue(int x, int y) {        return Integer.parseInt(gfr.getBlock("CLUES")[x][y]);    }


    public String getLineName(int x,int y,Direction d) {
        return cells.getCell(x,y).getCellLine(d);
    }

    public LineState getLineState(int x, int y, Direction d) {
        return subboard.getEdge(getLineName(x,y,d));
    }

    public boolean isComplete() { return subboard.isComplete(); }
    public boolean applyMove(Object o) { return subboard.applyMove(o); }
    public List<Board> guessAlternatives() {
        List<LoopyBoard> subalts = subboard.guessAlternatives();
        return subalts.stream().map(lb->new Board(this,lb)).collect(Collectors.toList());
    }

    private FlattenSolvableTuple<Board> tupxform(FlattenSolvableTuple<LoopyBoard> intuple) {
        FlattenSolvableTuple<Board> result = new FlattenSolvableTuple<Board>();
        for(int i = 0 ; i < intuple.choices.size() ; ++i) {
            result.addTuple(new Board(this,intuple.choices.get(i)),intuple.antimoves.get(i));
        }
        return result;
    }


    public List<FlattenSolvableTuple<Board>>getSuccessorTuples() {
        List<FlattenSolvableTuple<LoopyBoard>> subtuples = subboard.getSuccessorTuples();
        return subtuples.stream().map(t->tupxform(t)).collect(Collectors.toList());
    }

    private static Point inDir(Point p, Direction d) {
        switch(d) {
            case WEST: return new Point(p.x-1,p.y);
            case EAST: return new Point(p.x+1,p.y);
            case NORTHWEST: return new Point(p.y%2 == 0 ? p.x-1 : p.x,p.y-1);
            case SOUTHWEST: return new Point(p.y%2 == 0 ? p.x-1 : p.x,p.y+1);
            case NORTHEAST: return new Point(p.y%2 == 0 ? p.x : p.x+1,p.y-1);
            case SOUTHEAST: return new Point(p.y%2 == 0 ? p.x : p.x+1,p.y+1);
            default: throw new RuntimeException("Illegal dir in inDir");
        }
    }


    // --------------------- calculating inside/outside and run widths --------------------
    // -10 = unknown
    // -1 = outside
    // 0 = inside;
    public static final int SIDE_UNKNOWN = -10;
    public static final int SIDE_OUTSIDE = -1;
    public static final int SIDE_INSIDE_UNKNOWN = 0;

    public int opp(int x) { return x == SIDE_OUTSIDE ? SIDE_INSIDE_UNKNOWN : SIDE_OUTSIDE; }

    @Ignore private CellContainer<Integer> insides = null;

    private static final Direction[] LEGALDIRS = new Direction[] {
            Direction.NORTHWEST,Direction.NORTHEAST,
            Direction.EAST,Direction.WEST,
            Direction.SOUTHEAST,Direction.SOUTHWEST };

    private boolean isPath(String ename) { return subboard.getEdge(ename)== LineState.PATH;}
    private boolean isPath(Point p,Direction d) {
        return isPath(cells.getCell(p.x,p.y).getCellLine(d));
    }

    private void calculateIO() {
        // set one cell (doesn't matter which one) by coming in from outside
        insides.setCell(0,0,isPath("(0,0)W") ? SIDE_INSIDE_UNKNOWN : SIDE_OUTSIDE);
        // the queue contains a list of board cells that have been marked, but have not yet been expanded from.
        List<Point> queue = new ArrayList<>();
        queue.add(new Point(0,0));
        while(queue.size() > 0) {
            Point curpoint = queue.remove(0);
            int curio = insides.getCell(curpoint.x,curpoint.y);
            for (Direction d : LEGALDIRS) {
                Point childpoint = inDir(curpoint,d);
                if (!onBoard(childpoint.x,childpoint.y)) continue;
                if (insides.getCell(childpoint.x,childpoint.y) != SIDE_UNKNOWN) continue;
                insides.setCell(childpoint.x,childpoint.y,isPath(curpoint,d) ? opp(curio) : curio);
                queue.add(childpoint);
            }
        }
    }

    // x is the first index past the run.
    private void setRunWidths(int x,int y,int count) {
        for (int i = 0 ; i < count ; ++i) {
            insides.setCell(x-1-i,y,count);
        }
    }


    private void calculateRunWidths() {
        for (int y = 0 ; y < getHeight() ; ++y) {
            int oncount = 0;
            int x;
            for (x = 0 ; x < getWidth(y) ; ++x) {
                if(insides.getCell(x,y) == 0) { ++oncount ; continue; }
                setRunWidths(x,y,oncount);
                oncount = 0;
            }
            setRunWidths(x,y,oncount);
        }
    }



    public int getIORunWidth(int cx,int cy) {
        if (!isComplete()) return SIDE_UNKNOWN;
        if (insides == null) {
            insides = new CellContainer<Integer>(gfr.getWidth(),gfr.getHeight(),(x,y)->SIDE_UNKNOWN);
            calculateIO();
            calculateRunWidths();
        }
        return insides.getCell(cx,cy);
    }









}
