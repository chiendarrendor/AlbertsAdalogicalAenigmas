import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.newpath.PathContainer;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class PathDecomposer {
    private static class DecomposerCell {
        Map<Direction,DrawDirection> directions = new HashMap<>();
    }

    private CellContainer<DecomposerCell> cells;

    private void makeDecomposition(Point p, Direction d, DrawDirection drawDirection) {
        if (cells.getCell(p.x,p.y) == null) {
            cells.setCell(p.x,p.y,new DecomposerCell());
        }
        cells.getCell(p.x,p.y).directions.put(d,drawDirection);
    }

    private void setPair(Point p1,Point p2,boolean isReversible) {
        Direction d = Direction.fromTo(p1.x,p1.y,p2.x,p2.y);
        makeDecomposition(p1,d,isReversible ? DrawDirection.REVERSIBLE : DrawDirection.OUTBOUND);
        makeDecomposition(p2,d.getOpp(), isReversible ? DrawDirection.REVERSIBLE : DrawDirection.INBOUND);
    }


    private void applyPath(PathContainer.Path p) {
        for (int i = 0 ; i < p.getCells().size() - 1 ; ++i) {
            setPair(p.getCells().get(i),p.getCells().get(i+1),p.isReversible());
        }
        if (p.isLoop()) {
            setPair(p.tail(),p.head(),p.isReversible());
        }
    }




    public PathDecomposer(PathContainer pcon) {
        cells = new CellContainer<DecomposerCell>(pcon.getWidth(),pcon.getHeight(),(x,y)->null);

        for (PathContainer.Path p : pcon.getPaths()) {
            applyPath(p);
        }
    }

    public DrawDirection getDecomposition(int x,int y,Direction d) {
        DecomposerCell dcell = cells.getCell(x,y);
        if (dcell == null) return DrawDirection.NONE;
        if (!dcell.directions.containsKey(d)) return DrawDirection.NONE;
        return dcell.directions.get(d);
    }


}
