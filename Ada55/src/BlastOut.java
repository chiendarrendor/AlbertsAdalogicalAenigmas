
// This class will take a group of known-adjacent cells (with PATH between them)
// and explore how it expands out through UNKNOWN edges
// there are cells that cannot be part of the expansion set:
// * any cell belonging to a known-adjacent group with a cell on the other side of a wall from cell in our set
// * any cell belonging to a known-adjacent group with two numbers already
// * if our group has two numbers, any cell belonging to a known-adjacent group with one number
// * if our group has one number, any cell belonging to a known-adjacent group with one number within one of our number
//

import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.Set;
import java.util.stream.Collectors;

public class BlastOut {
    private void clearNums(CellContainer<Boolean> cells, Set<Point> nset) {
        nset.stream().forEach(p->cells.setCell(p.x,p.y,false));
    }

    private static class ThisReference implements GridGraph.GridReference {
        Board b;
        CellContainer<Boolean> allowedCells;

        public ThisReference(Board b,CellContainer<Boolean> allowedCells) { this.b = b; this.allowedCells = allowedCells; }
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isIncludedCell(int x, int y) { return allowedCells.getCell(x,y); }
        public boolean edgeExitsEast(int x, int y) { return b.getEdge(x,y, Direction.EAST) != EdgeState.WALL; }
        public boolean edgeExitsSouth(int x, int y) { return b.getEdge(x,y,Direction.SOUTH) != EdgeState.WALL; }
    }

    private Set<Point> extendedPoints;
    private Set<Point> basePoints;


    public BlastOut(Board thing, GridGraph gg, Set<Point> thisgroup,Set<Point> thisnumbers) {
        basePoints = thisgroup;

        boolean issingle = thisnumbers.size() == 1;
        boolean isdouble = thisnumbers.size() == 2;
        Point numloc =  thisnumbers.iterator().next();
        int singlenum = thing.getNumber(numloc.x,numloc.y);

        CellContainer<Boolean> cells = new CellContainer<Boolean>(thing.getWidth(),thing.getHeight(),
                (x,y)->true,
                (x,y,r)->r);

        for (Set<Point> pset : gg.connectedSets()) {
            if (pset == thisgroup) continue;

            Set<Point> nset = pset.stream().filter(p->thing.hasNumber(p.x,p.y)).collect(Collectors.toSet());
            if (nset.size() == 0) continue;
            if (nset.size() >= 2) { clearNums(cells,nset); continue; }
            if (isdouble) { clearNums(cells,nset); continue; }
            Point thisnumloc = nset.iterator().next();
            int thisnum = thing.getNumber(thisnumloc.x,thisnumloc.y);
            if (Math.abs(thisnum-singlenum) < 2) clearNums(cells,nset);
        }

        for (Point myp : thisgroup) {
            for (Direction d: Direction.orthogonals()) {
                if (thing.getEdge(myp.x,myp.y,d) != EdgeState.WALL) continue;
                Point nextp = d.delta(myp,1);
                if (!thing.onBoard(nextp)) continue;
                Set<Point> points = gg.connectedSetOf(nextp);
                clearNums(cells,points);
            }
        }

        GridGraph newgg = new GridGraph(new ThisReference(thing,cells));
        extendedPoints = newgg.connectedSetOf(thisgroup.iterator().next());
    }

    LogicStatus singleNumberExtend(Board thing) {
        Set<Point> nset = extendedPoints.stream().filter(p->thing.hasNumber(p.x,p.y)).collect(Collectors.toSet());
        if (nset.size() < 2) return LogicStatus.CONTRADICTION;


        return LogicStatus.STYMIED;
    }


}
