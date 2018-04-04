package grid.puzzlebits;

public class EdgeContainer<T>
{
    // width and height are the count of cells around and between which are the edges.
    private int width;
    private int height;

    // hedge 0,0 is the edge NORTH of cell 0,0
    // hedge 0,1 is the edge NORTH of cell 0,1 (aka SOUTH of cell 0,0)
    //
    private T[][] hedges;

    // vedge 0,0 is the edge WEST of cell 0,0
    // vedge 1,0 is the edge WEST of cell 1,0 (aka EAST of cell 0,0)
    private T[][] vedges;

    public interface Creator<K> { public K op(int x,int y,boolean isV); }
    public interface Copier<K> { public K op(int x,int y, boolean isV, K old); }
    public interface Operator<K> { public void op(int x,int y,boolean isV,K old); }

    Copier<T> copier;

    private void makeNewEdges()
    {
        hedges = (T[][]) new Object[width][height+1];
        vedges = (T[][]) new Object[width+1][height];
    }


    public void forEachEdge(Operator<T> lam)
    {
        for (int y = 0 ; y <= height ; ++y)
        {
            for (int x = 0 ; x <= width ; ++x)
            {
                if (y < height) lam.op(x,y,true,vedges[x][y]);
                if (x < width) lam.op(x,y,false,hedges[x][y]);
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * Given an edge-domain coordinate, is this edge an outer edge?
     * @param x
     * @param y
     * @param isV
     * @return
     */
    boolean isOuter(int x,int y,boolean isV) {
        if (isV) {
            if (x == 0) return true;
            if (x == width) return true;
            return false;
        }
        else {
            if (y == 0) return true;
            if (y == height) return true;
            return false;
        }
    }

    /**
     * given a cell-domain coordinate, is this edge an outer edge?
     * @param x
     * @param y
     * @param d
     * @return
     */
    boolean isOuter(int x,int y,Direction d) {
        switch(d) {
            case NORTH:
                return y == 0;
            case SOUTH:
                return y == height - 1;
            case EAST:
                return x == width - 1;
            case WEST:
                return x == 0;
            default: throw new RuntimeException("Directions for EdgeContainer must be orthogonal");
        }
    }


    public T getEdge(int x,int y,boolean isV) { return isV ? vedges[x][y] : hedges[x][y]; }
    public void setEdge(int x,int y,boolean isV,T newval) { if (isV) vedges[x][y] = newval; else hedges[x][y] = newval; }

    public T getEdge(int x,int y, Direction d)
    {
        boolean isv = d == Direction.EAST || d == Direction.WEST;
        if (d == Direction.EAST) ++x;
        if (d == Direction.SOUTH) ++y;
        return getEdge(x,y,isv);
    }

    public void setEdge(int x,int y, Direction d,T val)
    {
        boolean isv = d == Direction.EAST || d == Direction.WEST;
        if (d == Direction.EAST) ++x;
        if (d == Direction.SOUTH) ++y;
        setEdge(x,y,isv,val);
    }

    /**
     * this constructor form will set the extreme edges to outerWall (many puzzles simply have hard walls at the outer edge, for example)
     * If you need a variant outer wall, use the other constructor.
     * @param width
     * @param height
     * @param outerWall
     * @param creator
     * @param copier
     */

    public EdgeContainer(int width,int height,final T outerWall,Creator<T> creator, Copier<T> copier) {
        this.width = width;
        this.height = height;
        this.copier = copier;
        makeNewEdges();
        forEachEdge((x,y,isv,old) -> {
            if (isOuter(x,y,isv)) setEdge(x,y,isv, outerWall);
            else setEdge(x,y,isv,creator.op(x,y,isv));
        });
    }


    /**
     * This constructor will use creator to create an edge object for every edge in and around the cells
     * implied by width and height.  Note that the (x,y,isV) tuple used for both creator and copier
     * is in edge coordinate system:
     *      the left-most edge is (0,n,true), for n = 0 to height -1.
     *      the right-most edge is (width,n,true) for n = 0 to height - 1 (there are one more vert columns than cells)
     *      the top edge is (n,0,false) for n = 0 to width - 1
     *      the bottom edge is (n,height,false) for n = 0 to width - 1 (there are one more horiz rows than cells)
     * @param width
     * @param height
     * @param creator
     * @param copier
     */
    public EdgeContainer(int width, int height, Creator<T> creator, Copier<T> copier)
    {
        this.width = width;
        this.height = height;
        this.copier = copier;

        makeNewEdges();
        forEachEdge((x,y,isv,old)-> setEdge(x,y,isv,creator.op(x,y,isv)));
    }

    public EdgeContainer(EdgeContainer<T> right)
    {
        this.width = right.getWidth();
        this.height = right.getHeight();
        this.copier = right.copier;
        makeNewEdges();
        forEachEdge((x,y,isv,old) -> setEdge(x,y,isv,copier.op(x,y,isv,right.getEdge(x,y,isv))));

    }



}
