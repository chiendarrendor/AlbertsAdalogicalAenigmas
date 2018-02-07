import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.*;

public class Board implements StandardFlattenSolvable<Board>
{
    GridFileReader gfr;
    CellContainer<CellType> cells;
    CellContainer<Integer> numbers;
    CellContainer<CellNumbers> workspace;
    Rectangle[] rects;

    public Board(String fname)
    {
        gfr = new GridFileReader(fname);

        if (!gfr.hasVar("MAXV")) throw new RuntimeException("Missing MAXV");
        if (!gfr.hasVar("LETTERS")) throw new RuntimeException("Missing LETTERS");
        if (!gfr.hasVar("NUMBOXES")) throw new RuntimeException("Missing NUMBOXES");

        numbers = new CellContainer<Integer>(getWidth(),getHeight(),(x,y)->{return 0;},(x,y,r)->{return r; });

        cells = new CellContainer<CellType>(getWidth(),getHeight(),(x,y)->{
            String s = gfr.getBlock("GRID")[x][y];
            if ("W".equals(s)) return CellType.WHITE;
            if ("G".equals(s)) return CellType.GREY;
            if ("#".equals(s)) return CellType.OFFBOARD;
            if (s.chars().allMatch( Character::isDigit )) { numbers.setCell(x,y,Integer.parseInt(s)); return CellType.NUMBER; }
            throw new RuntimeException("Illegal Grid clue type");
        },(x,y,r) -> { return r; });

        workspace = new CellContainer<CellNumbers>(getWidth(),getHeight(),
                (x,y)-> {
                    if (getCell(x,y) != CellType.WHITE && getCell(x,y) != CellType.GREY) return null;
                    return new CellNumbers(Integer.parseInt(gfr.getVar("MAXV")));
                },
                (x,y,r)->{
                    if (r == null) return null;
                    return new CellNumbers(r);
                });

        int numboxes = Integer.parseInt(gfr.getVar("NUMBOXES"));
        rects = new Rectangle[numboxes];
        for (int i = 0 ; i < numboxes ; ++i)
        {
            String parts[] = gfr.getVar("BOX"+i).split(" ");
            rects[i] = new Rectangle(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]));
        }


    }

    public Board(Board right)
    {
        gfr = right.gfr;
        cells = right.cells;
        numbers = right.numbers;
        workspace = new CellContainer<CellNumbers>(right.workspace);
        rects = right.rects;
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y) && getCell(x,y) != CellType.OFFBOARD; }
    public CellType getCell(int x,int y) { return cells.getCell(x,y); }
    public int getNumber(int x,int y) { return numbers.getCell(x,y); }
    public CellNumbers getWorkBlock(int x,int y) { return workspace.getCell(x,y); }
    public Rectangle[] getRectangles() { return rects; }

    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }

    public boolean isComplete()
    {
        return terminatingForEachCell((x,y)->{
            CellNumbers cn = getWorkBlock(x,y);
            if (cn == null) return true;
            if (cn.isDone()) return true;
            return false;
        });
    }

    private class MyMove
    {
        int removeval;
        int x;
        int y;
        public MyMove(int x,int y,int removeval) { this.x = x; this.y = y; this.removeval = removeval; }
    }

    @Override
    public boolean applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        getWorkBlock(mm.x,mm.y).removeNumber(mm.removeval);
        return true;
    }

    @Override
    public FlattenSolvableTuple<Board> getOneTuple(int x, int y)
    {
        CellNumbers wb = getWorkBlock(x,y);

        if (wb == null) return null;
        if (!wb.isValid()) return null;
        if (wb.isDone()) return null;

        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
        for (int i = wb.getMin() ; i <= wb.getMax() ; ++i)
        {
            if (!wb.isOn(i)) continue;
            Board nb = new Board(this);
            CellNumbers nwb = nb.getWorkBlock(x,y);
            nwb.setNumber(i);
            MyMove mm = new MyMove(x,y,i);
            fst.addTuple(nb,mm);
        }
        return fst;
    }




}
