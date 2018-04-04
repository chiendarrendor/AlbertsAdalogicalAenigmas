import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import javafx.scene.control.Cell;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Board implements StandardFlattenSolvable<Board>
{
    GridFileReader gfr;
    CellContainer<CellState> cells;
    CharMap cmap;
    Map<Point,Hole> holes = new HashMap<>();


    public Board(String fname) {
        gfr = new GridFileReader(fname,new String[]{"LETTERS","BALLS"});
        cells = new CellContainer<CellState>(getWidth(),getHeight(),
                (x,y)->{
                    if (gfr.hasBlock("TRAPS") && gfr.getBlock("TRAPS")[x][y].charAt(0) == '#') return CellState.TRAP;
                    if (gfr.getBlock("BALLS")[x][y].charAt(0) == '@') return CellState.HOLE;
                    if (gfr.getBlock("BALLS")[x][y].charAt(0) == '.') return CellState.EMPTY;
                    else return CellState.BALL;
                },
                (x,y,r)->r);
        cmap = new CharMap();

        forEachCell((x,y) -> {
            if (getCellStates().getCell(x,y) != CellState.BALL) return;
            Point p = new Point(x,y);
            holes.put(p,new Hole(p,gfr.getBlock("BALLS")[x][y],this));
        });


    }

    public Board(Board right) {
        gfr = right.gfr;
        cells = new CellContainer<CellState>(right.cells);
        cmap = new CharMap(right.cmap);
        right.holes.values().stream().forEach((x)->holes.put(x.getInitialPoint(),new Hole(x,this)));
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0);}
    public CellContainer<CellState> getCellStates() { return cells; }
    public CharMap getCharMap() { return cmap; }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }
    public boolean onBoard(int x, int y) { return gfr.inBounds(x,y);}
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public String getBall(int x,int y) { return gfr.getBlock("BALLS")[x][y]; }
    public String getSolutionType() { return gfr.getVar("SOLUTIONTYPE");}
    public String getCookedSolution() { return gfr.getVar("COOKEDSOLUTION");}

    public void show() {
        holes.values().stream().forEach((x)->x.show());
    }

    public CellContainer<Shot> getShotInfo() {
        CellContainer<Shot> result = new CellContainer<Shot>(getWidth(),getHeight(),
                (x,y)->null,
                (x,y,r)->{throw new RuntimeException("Don't copy-construct this!");});

        holes.values().stream().forEach((hole)->{
            hole.shotsByLength.values().stream().forEach((shoth)->{
                shoth.fillShotInfo(result);
            });
        });


        return result;
    }



    // -------------------------- Solvable methods ---------------------------

    public boolean isComplete() { return holes.values().stream().allMatch((h)->h.isSet());  }

    // so, our moves in this case will be to enforce a particular shot from a
    // Hole...which means the antimove should be to remove that shot.
    // in either case, the Shot Name itself is an adequate Move object!
    public boolean applyMove(Object o) {
        ShotName name = (ShotName)o;
        Hole h = holes.get(name.initialPoint);
        h.removeShot(name);

        return true;
    }

    @Override
    public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        Point p = new Point(x,y);
        Hole h = holes.get(p);
        if (h == null) return null;
        if (h.isSet()) return null;

        List<ShotName> shotnames = h.getShotNames();
        FlattenSolvableTuple<Board> result = new FlattenSolvableTuple<>();

        shotnames.stream().forEach((name)-> {
            Board b = new Board(this);
            b.holes.get(name.initialPoint).set(name);
            result.addTuple(b,name);
        });


        return result;
    }


}
