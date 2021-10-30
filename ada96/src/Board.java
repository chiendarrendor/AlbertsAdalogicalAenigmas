import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.simple.Solvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Board implements Solvable<Board> {
    @Shallow GridFileReader gfr;
    @Ignore Map<Integer,Knight> knights = new HashMap<>();
    @Deep SegmentSet activeSgements;
    @Deep CellContainer<CellState> cells;
    @Shallow int knightsPerLine;
    @Shallow Map<Point, Integer> initialPositions = new HashMap<>();

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        if (getWidth() != getHeight()) throw new RuntimeException("According to Rules, board must be square");
        activeSgements = new SegmentSet();

        cells = new CellContainer<CellState>(getWidth(),getHeight(),
                (x,y)-> {
                    if (!isKnight(x,y)) return CellState.UNKNOWN;
                    Point thisPoint = new Point(x,y);
                    Knight newKnight = new Knight(thisPoint, knightSize(x,y));
                    knights.put(newKnight.getId(),newKnight);
                    initialPositions.put(thisPoint,newKnight.getId());
                    if (knightSize(x,y) == -1) return CellState.POSITION_INITIAL;
                    return CellState.POSITION_INTERMEDIATE;
                });

        if (knights.size() % getWidth() != 0) throw new RuntimeException("Number of knights is not an integer multiple of edge length!");
        knightsPerLine = knights.size() / getWidth();
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
        for(int kkey : right.knights.keySet() ) {
            Knight newKnight = new Knight(right.knights.get(kkey));
            knights.put(kkey,newKnight);
        }
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x, int y) { return gfr.inBounds(x,y); }
    public boolean inBounds(Point p) { return gfr.inBounds(p); }
    public boolean isKnight(int x, int y) { return !gfr.getBlock("KNIGHTS")[x][y].equals("."); }
    public int knightSize(int x,int y) {
        if (!isKnight(x,y)) throw new RuntimeException("Can't ask that of a non-knight spzce!");
        if (gfr.getBlock("KNIGHTS")[x][y].equals("@")) return -1;
        return Integer.parseInt(gfr.getBlock("KNIGHTS")[x][y]);
    }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }


    public Collection<Integer> getKnightKeys() { return knights.keySet(); }
    public Knight getKnight(int knightKey) { return knights.get(knightKey); }
    public int getKnightsPerLine() { return knightsPerLine; }
    public CellState getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y, CellState cs) { cells.setCell(x,y,cs); }


    // this method should only be called when the point's current state is POSITION_INITIAL which means that it is a knight that might move, or might not.
    // the final state must be either POSITION_INTERMEDIATE (which means that the knight isn't here any more) or POSITION_FINAL (which means the knight _must_ be here)
    public void processInitialCell(Point p, CellState targetState) {
        if (getCell(p.x,p.y) != CellState.POSITION_INITIAL) throw new RuntimeException("Invariant Violation: processInitialCell called on a cell that is not POSITION_INITIAL");
        Knight k = knights.get(initialPositions.get(p));
        if (!k.hasZero()) throw new RuntimeException("Invariant Violation: a knight with cell POSITION_INITIAL should have a zero");

        switch(targetState) {
            case POSITION_INTERMEDIATE:
                setCell(p.x,p.y,targetState);
                k.clearZero();
                break;
            case POSITION_FINAL:
                setCell(p.x,p.y,targetState);
                k.setZero();
                break;
            default:
                throw new RuntimeException("Invariant Violation: processInitialCell called with an invalid target state of " + targetState);
        }
    }

    public void placePathOnBoard(Knight knight, KnightStep path) {
        knight.lock();
        path.set();

        List<Point> pathPoints = new ArrayList<>();
        List<Segment> pathSegments = new ArrayList<>();
        for ( ; path != null ; path = path.getParent()) {
            pathPoints.add(0, path.getPoint());
            if (path.getParent() != null) pathSegments.add(0,path.getJumpSegment());
        }
        SegmentSet ss = new SegmentSet();
        pathSegments.stream().forEach(ps->ss.addSegment(ps));
        getMasterSegmentSet().addSegmentSet(ss);

        for (int i = 0 ; i < pathPoints.size() - 1 ; ++i) {
            Point p = pathPoints.get(i);
            setCell(p.x,p.y,CellState.POSITION_INTERMEDIATE);
        }
        Point p = pathPoints.get(pathPoints.size()-1);
        setCell(p.x,p.y,CellState.POSITION_FINAL);
    }


    public SegmentSet getMasterSegmentSet() { return activeSgements; }


    @Override public boolean isSolution() { return knights.values().stream().allMatch(k->k.isLocked()); }

    @Override public List<Board> guessAlternatives() {
        List<Knight> unlockedKnights = knights.values().stream().filter(k->!k.isLocked()).sorted(
                (k1,k2)->{
                    int v1 = Integer.compare(k1.unexpandedCount(),k2.unexpandedCount());
                    if (v1 != 0) return v1;
                    return Integer.compare(k1.feasibleCount(),k2.feasibleCount());
                }
        ).collect(Collectors.toList());

        Knight knight = unlockedKnights.get(0);

        List<Board> successors = new ArrayList<>();
        if (knight.unexpandedCount() > 0) {
            Board b = new Board(this);
            Knight newKnight = b.getKnight(knight.getId());
            newKnight.increaseAllowableFeasibles();
            System.out.println("Doubling capacity of knight " + newKnight.getStartingPoint() + " to " + newKnight.getAllowableFeasibles());
            successors.add(b);
            return successors;
        }

        for (KnightStep feasibleStep : knight.getFeasibles()) {
            Board b = new Board(this);
            Knight newKnight = b.getKnight(knight.getId());
            KnightStep newFeasibleStep = newKnight.findKnightStep(feasibleStep);
            b.placePathOnBoard(newKnight,newFeasibleStep);
            successors.add(b);
        }
        return successors;
    }

}


