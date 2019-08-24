import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import org.omg.CORBA.UNKNOWN;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Board implements Grid,MultiFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<RectangleRegionHandler> regions;
    @Deep CellContainer<RectangleRegionSet> cells;
    @Shallow FakeInfo fakeinfo = null;
    public Board(String fname) {
        gfr = new GridFileReader(fname);

        makeFakeInfo();

        cells = new CellContainer<RectangleRegionSet>(getWidth(),getHeight(),
                (x,y)->new RectangleRegionSet(),
                (x,y,r)->new RectangleRegionSet(r)
        );

        final Realness initialrealness = fakeinfo == null ? Realness.REAL : Realness.UNKNOWN;

        regions = new CellContainer<RectangleRegionHandler>(getWidth(),getHeight(),
                (x,y)->{
                    if (!hasNumber(x,y)) return null;
                    return new RectangleRegionHandler(this,getNumber(x,y),new Point(x,y),initialrealness);
                },
                (x,y,r)->{
                    if (r == null) return null;
                    return new RectangleRegionHandler(r);
                }
        );
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    private void makeFakeInfo() {
        if (!gfr.hasVar("ALLOWLIARS")) return;
        if (!gfr.hasVar("ROWMINLIARS")) throw new RuntimeException("ALLOWLIARS needs ROWMINLIARS");
        if (!gfr.hasVar("ROWMAXLIARS")) throw new RuntimeException("ALLOWLIARS needs ROWMAXLIARS");
        if (!gfr.hasVar("COLMINLIARS")) throw new RuntimeException("ALLOWLIARS needs COLMINLIARS");
        if (!gfr.hasVar("COLMAXLIARS")) throw new RuntimeException("ALLOWLIARS needs COLMAXLIARS");
        fakeinfo = new FakeInfo(
                Integer.parseInt(gfr.getVar("ROWMINLIARS")),
                Integer.parseInt(gfr.getVar("ROWMAXLIARS")),
                Integer.parseInt(gfr.getVar("COLMINLIARS")),
                Integer.parseInt(gfr.getVar("COLMAXLIARS"))
        );
    }


    @Override public int getWidth() { return gfr.getWidth(); }
    @Override public int getHeight() { return gfr.getHeight(); }

    @Override public boolean inBounds(int x, int y) { return gfr.inBounds(x,y); }
    @Override public boolean inBounds(Point p) { return gfr.inBounds(p); }

    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return  CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl);}

    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) {return getLetter(x,y) != '.'; }
    public boolean hasNumber(int x,int y) { return gfr.getBlock("NUMBERS")[x][y].charAt(0) != '.'; }
    public int getNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }



    @Override public void addToGrid(int x,int y,RectangleRegion rr) { cells.getCell(x,y).add(rr); }
    @Override public void removeFromGrid(int x,int y,RectangleRegion rr) { cells.getCell(x,y).remove(rr); }

    public void removeRectangle(RectangleRegion rr) { getHandler(rr).remove(rr,this); }
    public void setRectangle(RectangleRegion rr) { getHandler(rr).set(rr,this); }
    public boolean hasRectangle(RectangleRegion rr) { return getHandler(rr).hasRectangle(rr); }
    public Realness getRealness(RectangleRegion rr) { return getHandler(rr).realness; }

    public RectangleRegionHandler getHandler(RectangleRegion rr) { return regions.getCell(rr.realCenter.x,rr.realCenter.y); }
    public RectangleRegionHandler getHandler(int x,int y) { return regions.getCell(x,y); }




    @Override public boolean isComplete() {
        return terminatingForEachCell((x,y)->{
            RectangleRegionSet rrs = cells.getCell(x,y);
            RectangleRegionHandler rrh = regions.getCell(x,y);

            if (rrs.size() != 1) return false;
            if (rrh == null) return true;
            if (rrh.realness == Realness.UNKNOWN) return false;
            if (rrh.realness == Realness.REAL && rrh.currentRectangles.size() == 1) return true;
            if (rrh.realness == Realness.FAKE && rrh.currentRectangles.size() == 0) return true;
            return false;
        });
    }

    private static class MyMove {
        boolean isReal;
        int realx;
        int realy;
        Realness realness;

        boolean doSet;
        RectangleRegion rr;

        public MyMove(int realx,int realy,Realness realness) {
            isReal = true;
            this.realx = realx;
            this.realy = realy;
            this.realness = realness;
        }

        public MyMove(RectangleRegion rr, boolean doSet) {
            isReal = false;
            this.rr = rr;
            this.doSet = doSet;
        }

        public boolean applyMove(Board b) {
            if (isReal) {
                RectangleRegionHandler rrh = b.regions.getCell(realx,realy);
                if (rrh.realness != Realness.UNKNOWN) return rrh.realness == realness;
                rrh.realness = realness;
                return true;
            } else {
                if (doSet) {
                    if (!b.hasRectangle(rr)) return false;
                    b.setRectangle(rr);
                    return true;
                } else {
                    b.removeRectangle(rr);
                    return true;
                }
            }
        }
    }



    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y) {
        if (!hasNumber(x,y)) return null;
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        RectangleRegionHandler rrh = regions.getCell(x,y);

        if (rrh.realness == Realness.UNKNOWN) {
            Board b1 = new Board(this);
            Board b2 = new Board( this);
            MyMove mm1 = new MyMove(x,y,Realness.REAL);
            MyMove mm2 = new MyMove(x,y,Realness.FAKE);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
        }

        if (rrh.currentRectangles.size() > 1) {
            FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
            for (RectangleRegion rr : rrh.currentRectangles) {
                Board tb = new Board(this);
                MyMove promove = new MyMove(rr,true);
                MyMove antimove = new MyMove(rr,false);
                promove.applyMove(tb);
                fst.addTuple(tb,antimove);
            }
            result.add(fst);
        }

        return result;
    }

    // this method will return the real-center of all Rectangle Regions touching this cell, or null
    // if either the region has no points or if more than one real-center is represented
    public Point getSingularRegion(int x,int y) {
        RectangleRegionSet rrs = cells.getCell(x,y);
        boolean first = true;
        Point curp = null;
        for (RectangleRegion rr : rrs) {
            if (first) {
                curp = rr.realCenter;
                first = false;
            } else if (!curp.equals(rr.realCenter)) {
                return null;
            }
        }
        return curp;
    }

    // returns true if, for both the cell at x,y and the cell at d.delta(1,(x,y)),
    // both cells have singular regions
    // and their singular regions are the same
    public boolean regionsMatch(int x,int y,Direction d) {
        Point p1 = getSingularRegion(x,y);
        if (p1 == null) return false;
        Point op = d.delta(x,y,1);
        Point p2 = getSingularRegion(op.x,op.y);
        if (p2 == null) return false;
        return p1.equals(p2);
    }





}

