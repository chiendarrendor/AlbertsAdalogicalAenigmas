import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by chien on 5/27/2017.
 */
public class Board implements FlattenSolvable<Board>
{
    GridFileReader gfr;
    int count;
    CellState cells[][];
    int numunknown;

    Map<Integer,Vector<Point>> hlines = new HashMap<>();
    Map<Integer,Vector<Point>> vlines = new HashMap<>();
    Map<Integer,Vector<Point>> regions = new HashMap<>();

    private void gocinsert(Map<Integer,Vector<Point>> mivp,int key, int x,int y)
    {
        if (!mivp.containsKey(key)) mivp.put(key,new Vector<>());
        mivp.get(key).add(new Point(x,y));
    }


    public Board(String filename)
    {
        gfr = new GridFileReader(filename);
        if (!gfr.hasBlock("REGIONS")) throw new RuntimeException("File doesn't have REGIONS");
        if (gfr.getVar("COUNT") == null) throw new RuntimeException("File doesn't have variable COUNT");
        count = Integer.parseInt(gfr.getVar("COUNT"));
        cells = new CellState[getWidth()][getHeight()];
        numunknown = getHeight() * getWidth();

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                cells[x][y] = CellState.UNKNOWN;
                gocinsert(hlines,y,x,y);
                gocinsert(vlines,x,x,y);
                gocinsert(regions,gfr.getBlock("REGIONS")[x][y].charAt(0),x,y);
            }
        }
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        count = right.count;
        numunknown = right.numunknown;
        hlines = right.hlines;
        vlines = right.vlines;
        regions = right.regions;

        cells = new CellState[getWidth()][getHeight()];
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                cells[x][y] = right.cells[x][y];
            }
        }
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public CellState getCell(int x,int y) { return cells[x][y]; }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }

    public void setCellTree(int x,int y)
    {
        --numunknown;
        cells[x][y] = CellState.TREE;
    }

    public void setCellGrass(int x,int y)
    {
        --numunknown;
        cells[x][y] = CellState.GRASS;
    }


    @Override
    public boolean isComplete()
    {
        return numunknown == 0;
    }

    private static class MyMove
    {
        int x;
        int y;
        CellState cs;
        public MyMove(int x,int y,CellState cs) { this.x = x ; this.y = y ; this.cs = cs; }
    }



    private FlattenSolvableTuple<Board> getOneTuple(int x,int y)
    {
        if (cells[x][y] != CellState.UNKNOWN) return null;
        Board b1 = new Board(this);
        b1.setCellGrass(x,y);
        MyMove mm1 = new MyMove(x,y,CellState.GRASS);

        Board b2 = new Board(this);
        b2.setCellTree(x,y);
        MyMove mm2 = new MyMove(x,y,CellState.TREE);

        return new FlattenSolvableTuple<>(b1,mm1,b2,mm2);
    }





    @Override
    public List<FlattenSolvableTuple<Board>> getSuccessorTuples()
    {
        Vector<FlattenSolvableTuple<Board>> result = new Vector<>();
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                FlattenSolvableTuple<Board> fst = getOneTuple(x,y);
                if (fst != null) result.add(fst);
            }
        }

        return result;
    }

    @Override
    public void applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        if (mm.cs == CellState.GRASS) setCellGrass(mm.x,mm.y);
        else if (mm.cs == CellState.TREE) setCellTree(mm.x,mm.y);
        else throw new RuntimeException("Illegal cell type in MyMove");
    }

    @Override
    public List<Board> guessAlternatives()
    {
        Vector<Board> result = new Vector<>();
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                FlattenSolvableTuple<Board> fst = getOneTuple(x, y);
                if (fst == null) continue;
                result.add(fst.choice1);
                result.add(fst.choice2);
                return result;
            }
        }
        throw new RuntimeException("Should not get here");
    }
}
