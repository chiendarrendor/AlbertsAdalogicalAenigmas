package grid.puzzlebits;

import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;

import java.lang.reflect.Array;

public class CellContainer<T> {
    private T[][] cells;
    int width;
    int height;

    public interface Creator<K> { public K op(int x,int y); }
    public interface Copier<K> { public K op(int x,int y, K old); }

    Creator<T> creator;
    Copier<T> copier;

    private void makeNewCells() { cells = (T[][]) new Object[width][height]; }


    public CellContainer(int width,int height,Creator<T> cr) { this(width,height,cr,(x,y,r)->r); }

    public CellContainer(int width,int height,Creator<T> cr,Copier<T> cop)
    {
        this.width = width;
        this.height = height;
        creator = cr;
        copier = cop;
        makeNewCells();
        forEachCell((x,y)-> cells[x][y] = creator.op(x,y));
    }

    public CellContainer(CellContainer<T> right)
    {
        this.width = right.width;
        this.height = right.height;
        this.creator = right.creator;
        this.copier = right.copier;
        makeNewCells();
        forEachCell((x,y)-> cells[x][y] = copier.op(x,y,right.getCell(x,y)));
    }


    int getWidth() { return width; }
    int getHeight() { return height; }
    public T getCell(int x,int y) { return cells[x][y]; }
    public void setCell(int x,int y,T val) { cells[x][y] = val; }

    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(width,height,xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(width,height,bxyl); }

}
