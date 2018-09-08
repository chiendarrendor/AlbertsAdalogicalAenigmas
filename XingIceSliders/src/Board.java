import OnTheFlyAStar.AStarNode;
import grid.copycon.CopyCon;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import javafx.scene.control.Cell;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Board implements AStarNode<Board> {
    @Shallow GridFileReader gfr;
    @Shallow Point[] goals;
    @Ignore Point[] pillars; // this needs a deep copy, but @Deep only does copy-constructor semantics
    @Shallow CellContainer<Boolean> ices;
    @Shallow boolean iced;
    @Ignore String move;

    public Board(String filename){
        gfr = new GridFileReader(filename);
        final int pillen = Integer.parseInt(gfr.getVar("PILLARCOUNT"));
        if (pillen != 3) throw new RuntimeException("This code specialized to only work with 3 pillars/goals");
        goals = new Point[pillen];
        pillars = new Point[pillen];
        LambdaInteger gidx = new LambdaInteger(0);
        LambdaInteger pidx = new LambdaInteger(0);

        ices = new CellContainer<Boolean>(getWidth(),getHeight(),
                (x,y) -> {
                    char thing = gfr.getBlock("THINGS")[x][y].charAt(0);
                    switch(thing) {
                        case 'I': return true;
                        case '.': return false;
                        case 'P':
                            if (pidx.get() >= pillen) throw new RuntimeException("Too many pillars specified!");
                            pillars[pidx.get()] = new Point(x,y);
                            pidx.inc();
                            return false;
                        case 'G':
                            if (gidx.get() >= pillen) throw new RuntimeException("Too many goals specified!");
                            goals[gidx.get()] = new Point(x,y);
                            gidx.inc();
                            return false;
                        default:
                            throw new RuntimeException("Unknown thing character specified " + thing);
                    }
                });
        iced = true;

        if (pidx.get() != pillen) throw new RuntimeException("Not enough Pillars");
        if (gidx.get() != pillen) throw new RuntimeException("Not enough Goals");


        setMove("START");
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
        // Deep copy (given that points are immutable)
        pillars = new Point[right.pillars.length];
        for (int i = 0 ; i < right.pillars.length ; ++i) pillars[i] = right.pillars[i];
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public void setMove(String m) { move = m; }
    public String getMove() { return move; }

    public FloorType getFloor(int x,int y) {
        switch(gfr.getBlock("FLOOR")[x][y].charAt(0)) {
            case 'I': return FloorType.ICE;
            case 'T': return FloorType.PATH;
            case '.': return FloorType.WALL;
            default: throw new RuntimeException(String.format("Illegal floor type %s at %d,%d",
                    gfr.getBlock("FLOOR")[x][y],x,y));
        }
    }

    public boolean isIce(int x,int y) { return iced && ices.getCell(x,y); }
    public void deIce() { iced = false; }
    public void reIce() { iced = true; }

    public boolean isPillar(int x,int y) {
        Point tp = new Point(x,y);
        return Arrays.stream(pillars).anyMatch(p-> p.equals(tp));
    }
    public boolean isGoal(int x,int y) {
        Point tp = new Point(x,y);
        return Arrays.stream(goals).anyMatch(p-> p.equals(tp));
    }


    private int maxDist() {
        return (getWidth() - 1) * (getHeight() - 1);
    }


    @Override public int winGrade() {
        return maxDist() * goals.length;
    }

    @Ignore int[][] pairings = {
            { 0, 1, 2 },
            { 0, 2, 1 },
            { 1, 0, 2 },
            { 1, 2, 0 },
            { 2, 0, 1 },
            { 2, 1, 0 }
    };

    private int getPairingGrade(int pidx) {
        int result = 0;
        for (int i = 0 ; i < goals.length ; ++i) {
            int pilidx = i;
            int goalidx = pairings[pidx][i];
            result += Math.abs(pillars[pilidx].x - goals[goalidx].x);
            result += Math.abs(pillars[pilidx].y - goals[goalidx].y);
        }
        return maxDist()*goals.length - result;
    }

    @Override public int getGrade() {
        return IntStream.range(0,pairings.length).map(this::getPairingGrade).max().getAsInt();
    }

    @Override public String getCanonicalKey() {
        StringBuffer sb = new StringBuffer();
        forEachCell((x,y)-> sb.append(isPillar(x,y) ? 'P' : '.') );
        sb.append(iced ? 'I' : '.');
        return sb.toString();
    }

    @Override public List<Board> successors() {
        List<Board> result = new ArrayList<>();
        if (iced) {
            Board ib = new Board(this);
            ib.deIce();
            ib.setMove("DE-ICE");
            result.add(ib);
        } else  if (Arrays.stream(pillars).allMatch(p->getFloor(p.x,p.y)==FloorType.PATH)) {
            Board ib = new Board(this);
            ib.reIce();
            ib.setMove("RE-ICE");
            result.add(ib);
        }






        for (Direction d: Direction.orthogonals()) {
            for(int pi = 0 ; pi < pillars.length ; ++pi) {
                Board nb = getPillarSuccessorInDirection(pi,d);
                if (nb != null) result.add(nb);
            }
        }
        return result;
    }

    private Board getPillarSuccessorInDirection(int pi, Direction d) {
        Point p = pillars[pi];
        Point dp = null;

        int idx = 1;
        while(true) {
            Point np = d.delta(p, idx);
            if (!onBoard(np)) break;
            if (isPillar(np.x, np.y)) break;
            if (isIce(np.x, np.y)) break;
            if (getFloor(np.x, np.y) == FloorType.WALL) break;
            // if we get here, we're not blocked from moving into a legal space
            // and that new space could be our destination
            dp = np;
            if (getFloor(np.x, np.y) == FloorType.PATH) break;
            // if we get here, we're now on ice and need to check the next space
            ++idx;
        }
        if (dp == null) return null;
        Board result = new Board(this);
        result.pillars[pi] = dp;

        result.setMove("Move Pillar " + p + " direction " + d);


        return result;
    }


}
