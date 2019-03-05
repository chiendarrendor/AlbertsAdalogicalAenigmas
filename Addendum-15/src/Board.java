import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.PointAdjacency;


import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Ignore List<Region> regions = new ArrayList<>();
    @Deep CellContainer<Region> cells;
    @Deep EdgeContainer<EdgeState> enemyWalls;
    @Deep CellContainer<Set<Integer>> exclusionZones;



    public Board(String fname) {
        gfr = new GridFileReader(fname);

        LambdaInteger maxnum = new LambdaInteger(-1);

        cells = new CellContainer<Region>(getWidth(),getHeight(),
                (x,y)->{
                    if (!hasNumber(x,y)) return null;
                    int num = getNumber(x,y);
                    if (num > maxnum.get()) maxnum.set(num);

                    int newrid = regions.size();
                    Region newr = new Region(num,newrid);
                    regions.add(newr);
                    newr.addCell(new Point(x,y));
                    return newr;
                },
                (x,y,old)->null
                );

        exclusionZones = new CellContainer<Set<Integer>>(getWidth(),getHeight(),
                (x,y)-> {
                    Set<Integer> result = new HashSet<>();
                    for (int i = 1; i <= maxnum.get() + 1; ++i) result.add(i);
                    return result;
                },
                (x,y,old)->{
                    Set<Integer> result = new HashSet<>();
                    result.addAll(old);
                    return result;
                }
                );

        enemyWalls = new EdgeContainer<EdgeState>(getWidth(),getHeight(),EdgeState.WALL,
                (x,y,isV)->EdgeState.UNKNOWN,
                (x,y,isV,old)->old);

    }
    public Board(Board right) {
        for (Region or : right.regions) regions.add(new Region(or));
        CopyCon.copy(this,right);

        CellLambda.forEachCell(getWidth(),getHeight(),(x,y)-> {
            if (right.getRegionByCell(x,y) == null) cells.setCell(x,y,null);
            else cells.setCell(x,y,regions.get(right.getRegionByCell(x,y).getId()));
        });

        actions.addAll(right.actions);

    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public String getSolution() { return gfr.getVar("SOLUTION"); }
    public String getLetters() { return gfr.getVar("LETTERS"); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean isGray(int x,int y) { return gfr.getBlock("GRAYS")[x][y].charAt(0) == 'X'; }
    public boolean hasNumber(int x,int y) { return ! gfr.getBlock("NUMBERS")[x][y].equals("."); }
    public int getNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }
    public Region getRegionById(int id) { return regions.get(id); }
    public Region getRegionByCell(int x,int y) { return cells.getCell(x,y); }
    public List<Region> getRegions() { return regions; }
    public boolean isEnemyWall(int x,int y,Direction d) { return enemyWalls.getEdge(x,y,d) == EdgeState.WALL; }


    // will add newp to the given region.   if newp is in a region, destroys that region
    // and adds all cells of that region to the first region.
    // will fail if the regions are immiscable due to enemies, number mismatch, or new size too large.
    public boolean canExtendInto(int regionid, Point newp) {
        Region r1 = getRegionById(regionid);
        Region r2 = getRegionByCell(newp.x, newp.y);

        if (!exclusionZones.getCell(newp.x,newp.y).contains(r1.larger()) &&
                !exclusionZones.getCell(newp.x,newp.y).contains(r1.smaller())) {
            return false;
        }




        if (r2 == null) {
            if (r1.larger() <= r1.numCells()) return false;
            if (r1.hasEnemy(newp)) return false;
            return true;
        }


        if (!r1.numbersMatch(r2)) return false;
        int newactualsize = r1.joinActualSize(r2);
        int maxsize = newactualsize == -1 ? r1.getSize() + 1 : newactualsize;

        if (r1.numCells() + r2.numCells() > maxsize) return false;


        if (r1.cellStream().filter(cell->r2.hasEnemy(cell)).count() > 0) return false;
        if (r2.cellStream().filter(cell->r1.hasEnemy(cell)).count() > 0) return false;

        return true;
    }



    @Ignore List<LowLevelAction> actions = new ArrayList<>();


    public void setActualSize(int regionid,int size) {
        actions.add(new LowLevelAction(regionid,size,ActionType.SIZE));
        getRegionById(regionid).setActualSize(size);
    }

    public void addEnemy(int regionid, Point newp) {
        actions.add(new LowLevelAction(regionid,ActionType.ENEMY,newp));
        Region r = getRegionById(regionid);
        r.addEnemy(newp);
        r.cellStream().forEach(p->{
            if (PointAdjacency.adjacent(p,newp,false)) {
                enemyWalls.setEdge(p.x,p.y,PointAdjacency.adjacentDirection(p,newp),EdgeState.WALL);
            }
        });
    }

    public void addCell(int regionid, Point newp) {
        actions.add(new LowLevelAction(regionid,ActionType.ADD,newp));
        Region r = getRegionById(regionid);
        r.addCell(newp);
        cells.setCell(newp.x,newp.y,r);
    }

    public void emptyCells(int regionid) {
        actions.add(new LowLevelAction(regionid));
        getRegionById(regionid).emptyCells();
    }

    public Region unNumberedRegion(int size) {
        int curindex = regions.size();
        Region result = new Region(size + 1, curindex);
        regions.add(result);
        result.setActualSize(size);

        actions.add(new LowLevelAction(curindex,size,ActionType.REGION));
        return result;
    }

    private enum ActionType { SIZE,ENEMY,ADD,CLEAR,REGION };
    private static class LowLevelAction {
        int regionid;
        ActionType type;
        int size;
        Point p;

        public LowLevelAction(int regionid,int size,ActionType type) {
            this.regionid = regionid;
            this.size = size;
            this.type = type;
        }
        public LowLevelAction(int regionid) { this.regionid = regionid; this.type = ActionType.CLEAR; }
        public LowLevelAction(int regionid,ActionType type, Point p) { this.regionid = regionid; this.type = type; this.p = p; }
        public void apply(Board b) {
            switch(type) {
                case SIZE: b.setActualSize(regionid, size); return;
                case ENEMY: b.addEnemy(regionid,p); return;
                case ADD: b.addCell(regionid,p); return;
                case CLEAR: b.emptyCells(regionid);
                case REGION: b.unNumberedRegion(size);
            }
        }
        public String toString() {
            StringBuffer sb = new StringBuffer();
            switch(type) {
                case SIZE: sb.append("b.setActualSize(" + regionid + "," + size + ");"); break;
                case ENEMY: sb.append("b.addEnemy(" + regionid + ",new Point(" + p.x + "," + p.y + "));" ); break;
                case ADD: sb.append("b.addCell(" + regionid + ",new Point(" + p.x + "," + p.y + "));" ); break;
                case CLEAR: sb.append("b.emptyCells(" + regionid + ");"); break;
                case REGION: sb.append("b.unNumberedRegion(" + size + ");");
            }
            return sb.toString();
        }
    }

    public void showActions() {
        actions.stream().forEach(a->System.out.println(a.toString()));
    }



    // only call this if canExtendInto is true!
    public void extendInto(int regionid, Point newp) {
        Region r1 = getRegionById(regionid);
        Region r2 = getRegionByCell(newp.x, newp.y);

        /*
        System.out.println("extending region " + r1.toString() + " into " );
        if (r2 == null) {
            System.out.println("  Point " + newp);
        } else {
            System.out.println("  Region " + r2.toString());
        }
    */


        if (r2 == null) {
            addCell(regionid,newp);
            return;
        }

        int newactualsize = r1.joinActualSize(r2);

        setActualSize(regionid,newactualsize);
        r2.enemyStream().forEach(e->addEnemy(regionid,e));
        r2.cellStream().forEach(p -> {
            addCell(regionid,p);
        });
        emptyCells(r2.getId());
    }

    private static class MyMove {
        int regionid;
        boolean isNumber;
        int number;

        boolean isEnemy;
        Point p;

        public MyMove(int regionid, int number) { this.regionid = regionid; isNumber = true; this.number = number; }
        public MyMove(int regionid, Point p, boolean isEnemy) {
            this.regionid = regionid;
            isNumber = false;
            this.p = p;
            this.isEnemy = isEnemy;
        }

        public boolean applyMove(Board b) {
            Region r = b.getRegionById(regionid);
            if (isNumber) {
                if (r.getActualSize() != -1) return r.getActualSize() == number;
                b.setActualSize(regionid,number);
                return true;
            } else if (isEnemy) {
                if (r.hasCell(p)) return false;
                b.addEnemy(regionid,p);
                return true;
            } else {
                if (!b.canExtendInto(regionid,p)) return false;
                b.extendInto(regionid,p);
                return true;
            }
        }
    }


    @Override public boolean isComplete() {
        if (! regions.stream().allMatch(r->r.isDone())) return false;
        return CellLambda.terminatingForEachCell(getWidth(),getHeight(),(x,y)->cells.getCell(x,y) != null);
    }

    private List<FlattenSolvableTuple<Board>> getSuccessorTuplesForRegion(Region r) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        if (r.isDone()) return result;
        if (!r.isActive()) return result;
        if (r.getActualSize() == -1) {
            Board b1 = new Board(this);
            Board b2 = new Board(this);
            MyMove mm1 = new MyMove(r.getId(),r.getSize() - 1);
            MyMove mm2 = new MyMove(r.getId(),r.getSize() + 1);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>("region " + r.toString() + "smaller/larger",b1,mm1,b2,mm2));
        }

        Set<Integer> regionsSeen = new HashSet<>();
        r.getAdjacents(this,true).stream().forEach(p -> {
                        Region or = this.getRegionByCell(p.x,p.y);
            if (or != null) {
                if (regionsSeen.contains(or.getId())) return;
                regionsSeen.add(or.getId());
            }


            Board b1 = new Board(this);
            Board b2 = new Board(this);
            MyMove mm1 = new MyMove(r.getId(),p,true);
            MyMove mm2 = new MyMove(r.getId(),p,false);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>("region " + r.toString() + "join point " + p,b1,mm1,b2,mm2));
        });
        return result;
    }



    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        for (Region r : regions) result.addAll(getSuccessorTuplesForRegion(r));
        return result;
    }

    @Override public boolean applyMove(Object o) {
        return ((MyMove)o).applyMove(this);
    }

    @Override public List<Board> guessAlternatives() {
        for (Region r: regions) {
            List<FlattenSolvableTuple<Board>> ritems = getSuccessorTuplesForRegion(r);
            if (ritems.size() > 0) return ritems.get(0).choices;
        }
        throw new RuntimeException("guess alternatives with no alternatives?");
    }
}
