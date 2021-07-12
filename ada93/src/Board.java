import grid.file.GridFileReader;
import grid.graph.GridGraph;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;

import java.awt.Point;

public class Board {
    GridFileReader gfr;
    GridFileReader solution = null;
    GridGraph beads = null;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasClue(int x,int y) {return !gfr.getBlock("NUMBERS")[x][y].equals("."); }
    public int getClue(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    private class MyGridReference implements GridGraph.GridReference {
        @Override public int getWidth() { return Board.this.getWidth(); }
        @Override public int getHeight() { return Board.this.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return isSolutionCell(x,y); }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }



    public void applySolution(String fname) {
        solution = new GridFileReader(fname);
        beads = new GridGraph(new MyGridReference());
    }

    public boolean hasSolution() { return solution != null; }

    public boolean isSolutionCell(int x, int y) {
        return solution.getBlock("SOLUTION")[x][y].charAt(0) == '@';
    }

    public int getBeadSize(int x,int y) {
        return beads.connectedSetOf(new Point(x,y)).size();
    }
}
