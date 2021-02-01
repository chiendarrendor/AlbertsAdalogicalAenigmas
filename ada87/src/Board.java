import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.graph.GridGraph;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.logic.simple.Solvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Board implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep PatternContainer patterns;
    @Deep CellContainer<RegionSet> regions;
    @Shallow List<RegionId> regionnames;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        patterns = new PatternContainer(getWidth(),getHeight());
        regionnames = new ArrayList<>();
        forEachCell((x,y)->{
            if (!hasTag(x,y)) return;
            char tag = getTag(x,y);
            Pattern p = patterns.gocPattern(tag);
            StringBuffer sb = new StringBuffer();
            RegionId rid = new RegionId(tag,x,y);
            regionnames.add(rid);
            p.clearEdges(x,y);
        });

        regions = new CellContainer<RegionSet>(getWidth(),getHeight(),
                (x,y)->{
                    RegionSet result = new RegionSet(regionnames);
                    if (hasTag(x,y)) {
                        result.setRegion(new RegionId(getTag(x,y),x,y));
                    }
                    return result;
                },
                (x,y,r)->new RegionSet(r)
        );



    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public char getTag(int x,int y) { return gfr.getBlock("TAGS")[x][y].charAt(0); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasTag(int x,int y) { return getTag(x,y) != '.'; }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }
    public RegionSet getRegionSet(int x,int y) { return regions.getCell(x,y); }
    public RegionId getUniqueRegion(int x,int y) { return regions.getCell(x,y).getRegions().iterator().next(); }
    public Collection<Character> getPatternIds() { return patterns.getPatternIds(); }
    public Collection<RegionId> getRegions() { return regionnames; }
    public Pattern getPattern(char pid) { return patterns.getPattern(pid); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }


    public boolean isSolution() {
        return isComplete();
    }

     public boolean isComplete() {
        if (!patterns.isComplete()) return false;
        return terminatingForEachCell((x,y)->regions.getCell(x,y).size() == 1);
    }

    private static class MyMoveRegion {
        int x;
        int y;
        RegionId rid;
        boolean doSet;
        public MyMoveRegion(int x,int y,RegionId rid,boolean doSet) { this.x = x; this.y = y; this.rid = rid; this.doSet = doSet; }

        public boolean applyMove(Board b) {
            RegionSet rs = b.getRegionSet(x,y);
            if (doSet) {
                if (!rs.hasRegion(rid)) return false;
                rs.setRegion(rid);
                return true;
            } else {
                rs.clearRegion(rid);
                return true;
            }
        }
    }

    private static class MyMovePattern {
        char pid;
        int x;
        int y;
        PatternCell cell;
        public MyMovePattern(char pid,int x,int y, PatternCell cell) { this.pid = pid; this.x = x; this.y = y; this.cell = cell; }
        public boolean applyMove(Board b) {
            Pattern p = b.getPattern(pid);
            if (p.getCell(x,y) != PatternCell.UNKNOWN) return p.getCell(x,y) == cell;
            p.setCell(x,y,cell);
            return true;
        }
    }



    public boolean applyMove(Object o) { return ((MyMovePattern)o).applyMove(this); }


    public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        RegionSet rs = getRegionSet(x,y);
        if (rs.size() < 2) return null;

        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
        for (RegionId rid : rs.getRegions()) {
            Board b1 = new Board(this);
            MyMoveRegion pro = new MyMoveRegion(x,y,rid,true);
            MyMoveRegion anti = new MyMoveRegion(x,y,rid,false);
            pro.applyMove(b1);
            fst.addTuple(b1,anti);
        }
        return fst;
    }

    private FlattenSolvableTuple<Board> makeTuple(char patternid,int x,int y) {
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMovePattern mm1 = new MyMovePattern(patternid,x,y,PatternCell.INSIDE);
        MyMovePattern mm2 = new MyMovePattern(patternid,x,y,PatternCell.OUTSIDE);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }




    private List<FlattenSolvableTuple<Board>> getTuples(boolean onlyone) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();

        artout:
        for (char pid : getPatternIds()) {
            Pattern p = getPattern(pid);
            Collection<Point> articulations = p.getArticulatingUnknowns();
            for (Point pt : articulations) {
                result.add(makeTuple(pid,pt.x,pt.y));
                if (onlyone) break artout;
            }
        }

        System.out.println("Articulating Tuples Found: " + result.size());
        if (result.size() > 0) return result;

        outermost:
        for (char pid : getPatternIds()) {
            Pattern p = getPattern(pid);
            for (int y = p.miny ; y <= p.maxy ; ++y) {
                for (int x = p.minx ; x <= p.maxx ; ++x) {
                    if (p.getCell(x,y) != PatternCell.UNKNOWN) continue;
                    result.add(makeTuple(pid,x,y));
                    if (onlyone) break outermost;
                }
            }
        }

        System.out.println("Tuples found: " + result.size());
        return result;
    }



    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {
        return getTuples(false);
    }

    @Override public List<Board> guessAlternatives() {
        return getTuples(true).get(0).choices;
    }

    /** non-flatten guessalternatives
    public List<Board> guessAlternatives() {
        char maxpattern = '\0';
        PointDist best = new PointDist(-1,null);

        for (char patid : getPatternIds()) {
            Pattern p = getPattern(patid);
            PointDist pd = p.furthestPoint();
            if (pd.p == null) continue;
            if (pd.distance > best.distance) {
                best = pd;
                maxpattern = patid;
            }
        }
        if (best.p == null) throw new RuntimeException("No alternative guessable");

        Board b1 = new Board(this);
        Board b2 = new Board(this);
            b1.getPattern(maxpattern).setCell(best.p.x,best.p.y,PatternCell.INSIDE);
        b2.getPattern(maxpattern).setCell(best.p.x,best.p.y,PatternCell.OUTSIDE);
        List<Board> result = new ArrayList<>();
        result.add(b1);
        result.add(b2);
        return result;
    }
    */

}
