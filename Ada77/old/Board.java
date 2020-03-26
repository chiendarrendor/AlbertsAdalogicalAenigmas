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

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Board implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellState> cells;
    @Deep CellContainer<DestinationSet> destinations;
    @Deep CellContainer<Integer> interdistances;

    @Ignore Set<Point> unmovedboxes = new HashSet<Point>();

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        cells = new CellContainer<CellState>(getWidth(),getHeight(),(x,y)-> {
            if (hasStartingBox(x,y)) {
                unmovedboxes.add(new Point(x,y));
                return CellState.SOURCEBOX;
            }
            return CellState.EMPTY;
        });

        destinations = new CellContainer<DestinationSet>(getWidth(),getHeight(),(x,y)-> {
            if (hasStartingBox(x,y)) return new DestinationSet(canGoFromHere(x,y,false));
            return null;
        },
                (x,y,r)-> r == null ? null : new DestinationSet(r)
        );

        interdistances = new CellContainer<Integer>(getWidth(),getHeight(),(x,y)->-1);




    }

    public Board(Board right) {
        CopyCon.copy(this,right);
        unmovedboxes.addAll(right.unmovedboxes);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasStartingBox(int x,int y) { return gfr.getBlock("BOXES")[x][y].charAt(0) == '@'; }
    public boolean hasClue(int x,int y) { return ! gfr.getBlock("CLUES")[x][y].equals(".");}
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public boolean hasMoved(Point p) { return !unmovedboxes.contains(p); }

    private static Pattern cluematcher = Pattern.compile("^(\\d+)([NSEW])$");

    public int getClueSize(int x,int y) {
        String clue = gfr.getBlock("CLUES")[x][y];
        Matcher m = cluematcher.matcher(clue);
        if (!m.matches()) throw new RuntimeException("Can't parse clue " + clue);
        return Integer.parseInt(m.group(1));
    }

    public Direction getClueDirection(int x,int y) {
        String clue = gfr.getBlock("CLUES")[x][y];
        Matcher m = cluematcher.matcher(clue);
        if (!m.matches()) throw new RuntimeException("Can't parse clue " + clue);
        return Direction.fromShort(m.group(2));
    }

    public Direction alignmentDirection(Point p1, Point p2) { return Direction.fromToOrthogonalNotAdjacent(p1.x,p1.y,p2.x,p2.y); }

    public boolean canDoMove(Point source,Point destination) {

        if (!unmovedboxes.contains(source)) return false;
        if (!destinations.getCell(source.x,source.y).has(destination)) return false;
        if (source.equals(destination)) return true;
        Direction d = alignmentDirection(source,destination);

        for (int i = 1 ; ; ++i) {
            Point np = d.delta(source,i);
            if (destination.equals(np)) return true;
            if (!onBoard(np)) throw new RuntimeException("canDoMove source not aligned with destination!");
            CellState curcs = cells.getCell(np.x,np.y);
            if (curcs != CellState.EMPTY) return false;
        }
    }

    // assumes that canDoMove returned true.
    public void doMove(Point source,Point destination) {
        unmovedboxes.remove(source);
        destinations.getCell(source.x,source.y).set(destination);
        cells.setCell(destination.x,destination.y,CellState.FINALBOX);
        if (source.equals(destination)) return;
        Direction d = alignmentDirection(source,destination);

        int interdistance = -1;
        switch (d) {
            case NORTH: interdistance = source.y - destination.y; break;
            case SOUTH: interdistance = destination.y - source.y; break;
            case WEST: interdistance = source.x - destination.x; break;
            case EAST: interdistance = destination.x - source.x; break;
        }

        for (int i = 1 ; ; ++i) {
            Point np = d.delta(source,i);
            if (destination.equals(np)) {
                return;
            }
            cells.setCell(np.x,np.y,(d == Direction.NORTH || d == Direction.SOUTH) ? CellState.VERPATH : CellState.HORPATH);
            interdistances.setCell(np.x,np.y,interdistance);
        }
    }

    // finds the list of all unmoved boxes that can be moved to this cell
    public List<Point> canComeHere(int x, int y) {
        List<Point> result = new ArrayList<>();
        CellState cs = cells.getCell(x,y);
        if (cs == CellState.HORPATH || cs == CellState.VERPATH || cs == CellState.FINALBOX) return result;

        Point target = new Point(x,y);
        for(Direction d: Direction.orthogonals()) {
            for(int i = (d == Direction.NORTH) ? 0 : 1 ; ; ++i) {
                Point np = d.delta(x,y,i);
                if (!onBoard(np)) break;
                CellState npcs = cells.getCell(np.x,np.y);
                if (npcs == CellState.HORPATH || npcs == CellState.VERPATH || npcs == CellState.FINALBOX) break;
                if (npcs == CellState.EMPTY) continue;
                // if we get here, we have a (possibly) unmoved box..let's see if we can get home from here.
                if (canDoMove(np,target)) result.add(np);
                break;
            }
        }
        return result;
    }






    public List<Point> canGoFromHere(int x,int y,boolean checkdestination) {
        Point source = new Point(x,y);
        if (!unmovedboxes.contains(source)) return null;
        List<Point> result = new ArrayList<>();

        if (!checkdestination || destinations.getCell(x,y).has(source)) result.add(source);

        for (Direction d : Direction.orthogonals()) {
            for (int i = 1 ; ; ++i) {
                Point np = d.delta(source,i);
                if (!onBoard(np)) break;
                if (cells.getCell(np.x,np.y) != CellState.EMPTY) break;
                if (checkdestination && !destinations.getCell(x,y).has(np)) continue;
                result.add(np);
            }
        }
        return result;
    }



    private static class MyMove {
        Point source;
        Point destination;
        boolean anti;
        public MyMove(Point source,Point destination,boolean anti) { this.source = source; this.destination = destination; this.anti = anti; }
        public boolean applyMove(Board b) {

            if (anti) {
                b.destinations.getCell(source.x,source.y).remove(destination);
                return true;
            }

            if (!b.canDoMove(source,destination)) return false;
            b.doMove(source,destination);
            return true;
        }
    }

    @Override public boolean isComplete() { return unmovedboxes.size() == 0; }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    private FlattenSolvableTuple<Board> getCrateTuple(Point crate) {
        List<Point> destinations = canGoFromHere(crate.x,crate.y,true);
        if (destinations == null || destinations.size() == 0) return null;
        FlattenSolvableTuple<Board> result = new FlattenSolvableTuple<Board>();

        for (Point dest : destinations) {
            Board nb = new Board(this);
            MyMove move = new MyMove(crate,dest,false);
            MyMove anti = new MyMove(crate,dest,true);
            move.applyMove(nb);
            result.addTuple(nb,anti);
        }
        return result;
    }



    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        for (Point source : unmovedboxes) {
            FlattenSolvableTuple<Board> fst = getCrateTuple(source);
            if (fst != null) result.add(fst);
        }
        return result;
    }


    @Override public List<Board> guessAlternatives() {
        List<FlattenSolvableTuple<Board>> possibiliities = getSuccessorTuples();
        Comparator<FlattenSolvableTuple<Board>> comparebysize = (FlattenSolvableTuple<Board> fst1,FlattenSolvableTuple<Board> fst2) ->
                Integer.compare(fst1.choices.size(),fst2.choices.size());
        Collections.sort(possibiliities,comparebysize);

        return possibiliities.get(0).choices;
    }
}
