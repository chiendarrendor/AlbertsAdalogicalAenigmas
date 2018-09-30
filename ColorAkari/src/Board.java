import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow CellContainer<GroundState> grounds;
    @Deep CellContainer<LightCell> lightcells;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        grounds = new CellContainer<GroundState>(getWidth(),getHeight(),
            (x,y) -> {
                char c = gfr.getBlock("CELLS")[x][y].charAt(0);
                switch(c) {
                    case '.': return GroundState.TILE;
                    case '#': return GroundState.WALL;
                    case 'P': return GroundState.MAGENTA_TARGET;
                    case 'Y': return GroundState.YELLOW_TARGET;
                    case 'C': return GroundState.CYAN_TARGET;
                    default: throw new RuntimeException("Illegal character " + c);
                }
            }
        );

        lightcells = new CellContainer<LightCell>(getWidth(),getHeight(),
                (x,y)-> {
                    if (getGroundState(x,y) != GroundState.TILE) return null;
                    return new LightCell();
                },
                (x,y,r)->r == null ? null : new LightCell(r)
        );


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public GroundState getGroundState(int x,int y) { return grounds.getCell(x,y); }
    public LightCell getLightCell(int x,int y) { return lightcells.getCell(x,y); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public Stream<Point> stream() { return CellLambda.stream(getWidth(),getHeight()); }

    public List<Point> illluminating(int x,int y) {
        List<Point> result = new ArrayList<>();
        Point cp = new Point(x,y);

        for(Direction d : Direction.orthogonals()) {
            int delta = 1;
            while(true) {
                Point np = d.delta(cp,delta);
                if (!onBoard(np)) break;
                if (getGroundState(np.x,np.y) != GroundState.TILE) break;
                result.add(np);
                ++delta;
            }
        }
        return result;
    }



    @Override public boolean isComplete() {
        return stream().allMatch(p-> {
            LightCell ls = getLightCell(p.x,p.y);
            if (ls == null) return true;
            return ls.isComplete();
        });
    }

    private static class MyMove {
        int x;
        int y;
        boolean isOnly;
        LightState ls;

        public MyMove(int x,int y,boolean isOnly,LightState ls) { this.x = x; this.y = y; this.isOnly = isOnly; this.ls = ls; }
        public boolean applyMove(Board thing) {
            LightCell lc = thing.getLightCell(x,y);
            if (isOnly) {
                if (!lc.contains(ls)) return false;
                lc.setAs(ls);
            } else {
                lc.remove(ls);
            }
            return true;
        }
    }


    @Override public boolean applyMove(Object o) {
        return ((MyMove)o).applyMove(this);
    }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        LightCell lc = getLightCell(x,y);
        if (lc == null) return null;
        if (lc.isComplete()) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
        lc.stream().forEach(state -> {
            MyMove mm = new MyMove(x,y,true,state);
            MyMove mm2 = new MyMove(x,y,false,state);
            Board nb = new Board(this);
            mm.applyMove(nb);
            fst.addTuple(nb,mm2);
        });

        return fst;
    }

}

