import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by chien on 10/1/2017.
 */
public class Board extends StandardFlattenSolvable<Board>
{
    GridFileReader gfr;
    Cell[][] cells;

    Map<Character,Region> regions = new HashMap<>();


    public Board(String arg)
    {
        gfr = new GridFileReader(arg);
        cells = new Cell[getWidth()][getHeight()];
        forEachCell( (x,y) -> {
            cells[x][y] = new Cell();

            if (getFrag(x,y) != '.'){
                Region r = new Region(this,x,y);
                regions.put(r.rid,r);
            }

            return true;
        } );
        for (Region r : regions.values()) { r.Expand(this); }
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        cells = new Cell[getWidth()][getHeight()];
        forEachCell( (x,y) -> {
           cells[x][y] = new Cell(right.cells[x][y]);
           return true;
        });
        for (Map.Entry<Character,Region> me : right.regions.entrySet()) regions.put(me.getKey(),new Region(me.getValue()));
    }

    public int getWidth()
    {
        return gfr.getWidth();
    }
    public int getHeight()
    {
        return gfr.getHeight();
    }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public char getFrag(int x,int y) { return gfr.getBlock("FRAGS")[x][y].charAt(0); }

    public Cell getCell(int x,int y) { return cells[x][y];}
    public Region getRegion(char rid) { return regions.get(rid); }


    @Override
    public boolean isComplete()
    {
        boolean[] result = new boolean[1];
        result[0] = true;

        forEachCell((x,y)->{
            if (!getCell(x,y).isFixed()) { result[0] = false ; return false; }
            return true;
        });
        return result[0];
     }

     private class MyMove
     {
         int x;
         int y;
         char badid;
         public MyMove(int x,int y,char badid) { this.x = x; this.y = y; this.badid = badid; }
     }


    @Override
    public void applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        getCell(mm.x,mm.y).setImpossible(mm.badid);
    }

    @Override
    public FlattenSolvableTuple<Board> getOneTuple(int x, int y)
    {
        Cell c = getCell(x,y);
        if (c.getPossibles().size() < 2) return null;

        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();

        for (char id : c.getPossibles())
        {
            Board nb = new Board(this);
            nb.getCell(x,y).setIs(id);
            MyMove am = new MyMove(x,y,id);
            fst.addTuple(nb,am);
        }

        return fst;
    }



    // cell lambda stuff
    public interface CellLambda
    {
        boolean operation(int x,int y);
    }

    public void forEachCell(CellLambda cl)
    {
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                if (!cl.operation(x,y)) return;
            }
        }
    }

    public void forEachRectangleCell(Rectangle r,CellLambda cl)
    {
        for (int x = (int)r.getMinX() ; x < r.getMaxX() ; ++x)
        {
            for (int y = (int) r.getMinY(); y < r.getMaxY(); ++y)
            {
                if (!cl.operation(x, y)) return;
            }
        }
    }

    public boolean fits(Rectangle r,char rid)
    {
        boolean[] result = new boolean[1];
        result[0] = true;
        forEachRectangleCell(r,(x,y) -> {
            if (!getCell(x,y).canBe(rid)) { result[0] = false; return false; }
            return true;
        });
        return result[0];
    }
}
