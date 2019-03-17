package grid.spring;

import grid.puzzlebits.Direction;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.image.BufferedImage;


// this adapter is designed for problems where we need to do individual cell edges all the way to the
// edge of the board.  The user should still be able to refer to their grid by their grid coordinates
// and edges, but this will demand Edge information for outer edges (semantic difference from basic EdgeListener)
public interface ExpandedGridEdgeListener extends GridPanel.GridListener,GridPanel.EdgeListener {

    default public int getNumXCells() { return getBoardWidth() + 2; }
    default public int getNumYCells() { return getBoardHeight() + 2; }
    default public boolean drawGridNumbers() { return false; }
    default public boolean drawGridLines() { return false; }
    default public boolean drawBoundary() { return false; }
    default public EdgeDescriptor onBoundary() { return null; }

    default public boolean drawCellContents(int x,int y,BufferedImage bi) {
        if (x == 0) {
            if (y == 0 || y == getNumYCells() - 1) return false;
            GridPanel.DrawStringInCell(bi, Color.BLACK,""+(y-1));
            return true;
        }
        if (y == 0) {
            if (x == 0 || x == getNumXCells() - 1) return false;
            GridPanel.DrawStringInCell(bi,Color.BLACK,""+(x-1));
            return true;
        }
        if (x == getNumXCells() - 1 || y == getNumYCells() - 1) return false;
        return drawBoardCellContents(x-1,y-1,bi);
    }

    default public EdgeDescriptor toEast(int x,int y) {
        if (y == 0 || y == getNumYCells() - 1) return null;
        if (x == 0) {
            return getBoardEdgeDescriptor(0,y-1,Direction.WEST);
        }
        return getBoardEdgeDescriptor(x-1,y-1,Direction.EAST);
    }

    default public EdgeDescriptor toSouth(int x, int y) {
        if (x == 0 || x == getNumXCells() - 1) return null;
        if (y == 0) {
            return getBoardEdgeDescriptor(x-1,0,Direction.NORTH);
        }
        return getBoardEdgeDescriptor(x-1,y-1,Direction.SOUTH);
    }



    public int getBoardWidth();
    public int getBoardHeight();
    public boolean drawBoardCellContents(int x,int y,BufferedImage bi);
    public EdgeDescriptor getBoardEdgeDescriptor(int x,int y,Direction d);
    public String[] getAnswerLines();
}
