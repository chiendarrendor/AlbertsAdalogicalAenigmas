import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is an adjunct to EdgeContainer, which keeps track of data in both cell space and edge space
 * Cell space is the 'normal' grid mechanism where the upper left cell is 0,0, and x coordinate increases to the left and y coordinate increases down
 * Edge space is (x,y,isV) where vertical Edges (isV = true) are numbered such that the WEST edge of cell-space cell x,y has coordindte x,y
 *   and horizontal edges (isV = false) are numbered such that the NORTH edge of that same cell has coordinate x,y
 *
 *   Corner Space is a way to number the corners (the vertices of all the edges/corners of the cells)
 *   The NORTHWEST corner of cell-space (x,y) has coordinate (x,y), and x and y coordinates increase to the right and down respectively
 */
public class CornerSpace {
    // these are in cell-space measurements
    int width;
    int height;
    EdgeContainer<CornerSpace> dummyEdges; // we're using this to do edgespace inbounds
    CellContainer<CornerSpace> dummyCells;
    public CornerSpace(int cellSpaceWidth, int cellSpaceHeight) {
        width = cellSpaceWidth;
        height = cellSpaceHeight;
        dummyEdges = new EdgeContainer<CornerSpace>(width,height,(x,y,isV)->null,(x,y,isV,r)->null);
        dummyCells = new CellContainer<CornerSpace>(width,height,(x,y)->null);
    }
    public int getCornerSpaceWidth() { return width + 1; }
    public int getCornerSpaceHeight() { return height + 1; }
    public boolean inBounds(int cornerX, int cornerY) { return cornerX >= 0 && cornerY >= 0 && cornerX <= width && cornerY <= height; }

    private void addCornerDirectionIfInBounds(Map<Direction,Point> map,Direction d,int cellx,int celly) {
        if (!dummyCells.onBoard(cellx,celly)) return;
        map.put(d,new Point(cellx,celly));
    }


    public Map<Direction, Point> getCornerCells(int cornerX, int cornerY) {
        Map<Direction,Point> result = new HashMap<>();
        addCornerDirectionIfInBounds(result,Direction.SOUTHEAST,cornerX,cornerY);
        addCornerDirectionIfInBounds(result,Direction.NORTHEAST,cornerX,cornerY-1);
        addCornerDirectionIfInBounds(result,Direction.NORTHWEST,cornerX-1,cornerY-1);
        addCornerDirectionIfInBounds(result,Direction.SOUTHWEST,cornerX-1,cornerY);
        return result;
    }

    private void addEdgeDirectionIfInBounds(Map<Direction, EdgeContainer.EdgeCoord> map, Direction d, int edgex, int edgey, boolean isV) {
        if (!dummyEdges.inBounds(edgex,edgey,isV)) return;
        map.put(d,new EdgeContainer.EdgeCoord(edgex,edgey,isV));
    }

    public Map<Direction, EdgeContainer.EdgeCoord> getCornerEdges(int cornerX, int cornerY) {
        Map<Direction,EdgeContainer.EdgeCoord> result = new HashMap<>();
        addEdgeDirectionIfInBounds(result,Direction.EAST,cornerX,cornerY,false);
        addEdgeDirectionIfInBounds(result,Direction.SOUTH,cornerX,cornerY,true);
        addEdgeDirectionIfInBounds(result,Direction.WEST,cornerX-1,cornerY,false);
        addEdgeDirectionIfInBounds(result,Direction.NORTH,cornerX,cornerY-1,true);

        return result;
    }

    public Point getCornerOfCell(int cellx, int celly, Direction d) {
        switch (d) {
            case NORTHWEST: return new Point(cellx,celly);
            case NORTHEAST: return new Point(cellx+1,celly);
            case SOUTHWEST: return new Point(cellx,celly+1);
            case SOUTHEAST: return new Point(cellx+1,celly+1);
            default: throw new RuntimeException("getCornerOfCell must be called on a diagonal");
        }
    }



}
