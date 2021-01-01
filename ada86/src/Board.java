import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import javafx.geometry.Pos;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<SpaceType> usage;
    @Deep CellContainer<DestinationSet> destinations;
    @Shallow Set<Point> tilelocations;

    private List<PossibleDestination> makeDestinationsForTile(int x,int y) {
        List<PossibleDestination> result = new ArrayList<>();
        PossibleDestination base = new PossibleDestination(x,y);
        result.add(base);

        int maximum = Math.max(getWidth(),getHeight());

        for (Direction d : Direction.orthogonals()) {
            PossibleDestination prev = base;
            for (int i = 1 ; i < maximum ; ++i) {
                Point np = d.delta(x,y,i);
                if (!inBounds(np)) break;
                if (hasTile(np.x,np.y)) break;
                PossibleDestination newpd = new PossibleDestination(prev);
                newpd.addPoint(np.x,np.y);
                result.add(newpd);
                prev = newpd;
            }
        }
        return result;
    }





    public Board(String fname) {
        PossibleDestination.SetMaster(this);
        gfr = new GridFileReader(fname);
        tilelocations = new HashSet<>();

        usage = new CellContainer<SpaceType>(getWidth(),getHeight(),
                (x,y)->{
                    if (hasTile(x,y)) {
                        tilelocations.add(new Point(x,y));
                        return SpaceType.USED;
                    } else {
                        return SpaceType.EMPTY;
                    }
                });
        destinations = new CellContainer<DestinationSet>(getWidth(),getHeight(),
                (x,y)-> {
                    DestinationSet dr = new DestinationSet();
                    if (!hasTile(x,y)) dr.add(new PossibleDestination(x,y,true));
                    return dr;
                },
                (x,y,r)-> new DestinationSet(r));


        for (Point p : tilelocations) {
            List<PossibleDestination> pds = makeDestinationsForTile(p.x,p.y);
            for (PossibleDestination pd : pds) {
                destinations.getCell(pd.sourcex,pd.sourcey).add(pd);
                destinations.getCell(pd.destx,pd.desty).add(pd);
                for (Point dp : pd.intermediates) destinations.getCell(dp.x,dp.y).add(pd);
            }
        }


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public boolean inBounds(Point p) { return gfr.inBounds(p); }

    public boolean hasTile(int x,int y) { return gfr.getBlock("TILES")[x][y].charAt(0) != '.' ; }
    public char tileLetter(int x,int y) { return gfr.getBlock("TILES")[x][y].charAt(0); }
    public Set<Direction> tileDirections(int x, int y) {
        String s = gfr.getBlock("TILES")[x][y].substring(1);
        Set<Direction> result = new HashSet<>();
        for(char c : s.toCharArray()) {
            result.add(Direction.fromShort(""+c));
        }
        return result;
    }
    public boolean tileDirection(int x,int y,Direction d) {
        return tileDirections(x,y).contains(d);
    }

    public DestinationSet getCellDestinations(int x,int y) {
        return destinations.getCell(x,y);
    }

    public boolean destinationPresent(PossibleDestination pd) {
        // we are going to check:
        // sourcex,sourcey
        // destx,desty
        // all intermediates
        int count = 0;
        // so at end, this should either be 0, or intermediate.size + 2

        if (getCellDestinations(pd.sourcex,pd.sourcey).contains(pd)) ++count;
        if (getCellDestinations(pd.destx,pd.desty).contains(pd)) ++count;
        for(Point p : pd.intermediates) {
            if (getCellDestinations(p.x,p.y).contains(pd)) ++count;
        }
        if (count == 0) return false;
        if (count == pd.intermediates.size() + 2) return true;
        throw new RuntimeException("PossibleDestination incomplete on board!");
    }

    // assumes that destination is present, and removes it.
    public void clearDestination(PossibleDestination pd) {
        getCellDestinations(pd.sourcex,pd.sourcey).remove(pd);
        getCellDestinations(pd.destx,pd.desty).remove(pd);
        for(Point p: pd.intermediates) getCellDestinations(p.x,p.y).remove(pd);
    }

    // assumes destination is present, and removes all other entries from shared sets.
    // returns true if it removed any destinations.
    public boolean setDestination(PossibleDestination pd) {
        Set<PossibleDestination> doomed = new HashSet<>();
        doomed.addAll(getCellDestinations(pd.sourcex,pd.sourcey).destinations);
        doomed.addAll(getCellDestinations(pd.destx,pd.desty).destinations);
        for(Point p: pd.intermediates) doomed.addAll(getCellDestinations(p.x,p.y).destinations);

        doomed.remove(pd);
        for(PossibleDestination dpd: doomed) clearDestination(dpd);
        return doomed.size() > 0;
    }

    public PossibleDestination findDestination(int sx,int sy,int dx,int dy,boolean isBlank) {
        DestinationSet ds = getCellDestinations(sx,sy);
        for (PossibleDestination pd : ds.destinations) {
            if (isBlank) {
                if (pd.isBlank()) return pd;
            } else {
                if (pd.destx == dx && pd.desty == dy) return pd;
            }
        }
        return null;
    }



    // this function returns a DestinationSetClassifier as per the algorithm design in TileLogicStep
    public DestinationSetClassifier classifyDestinations(int x,int y) {
        if (!inBounds(x,y)) return DestinationSetClassifier.TERMINAL;
        DestinationSet ds = getCellDestinations(x,y);
        if (ds.size() == 0) return DestinationSetClassifier.INVALID;
        int blankcount = 0;
        int endshere = 0;
        int endselsewhere = 0;
        for (PossibleDestination pd : ds.destinations) {
            if (pd.isBlank()) { ++blankcount; continue; }
            if (pd.destx == x && pd.desty == y) ++endshere; else ++endselsewhere;
        }

        if (endshere == 0) return DestinationSetClassifier.CANNOT_HAVE;
        if (blankcount == 0 && endselsewhere == 0) return DestinationSetClassifier.MUST_HAVE;
        return DestinationSetClassifier.MIGHT_HAVE;
    }







    private static class MyMove {
        PossibleDestination pd;
        boolean doSet;

        public MyMove(PossibleDestination pd, boolean doSet) { this.pd = pd; this.doSet = doSet; }

        public boolean applyMove(Board b) {
            if (doSet) {
                if (!b.destinationPresent(pd)) return false;
                b.setDestination(pd);
                return true;
            } else {
                if (b.destinationPresent(pd)) b.clearDestination(pd);
                return true;
            }
        }
    }

    @Override public boolean isComplete() {
        return CellLambda.terminatingForEachCell(getWidth(),getHeight(),(x,y)->getCellDestinations(x,y).size() == 1);
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        DestinationSet ds = getCellDestinations(x,y);
        if (ds.size() < 2) return null;
        FlattenSolvableTuple<Board> result = new FlattenSolvableTuple<>();

        for (PossibleDestination pd : ds.destinations) {
            Board b1 = new Board(this);
            MyMove pro = new MyMove(pd,true);
            MyMove anti = new MyMove(pd,false);
            pro.applyMove(b1);
            result.addTuple(b1,anti);
        }

        return result;
    }


}
