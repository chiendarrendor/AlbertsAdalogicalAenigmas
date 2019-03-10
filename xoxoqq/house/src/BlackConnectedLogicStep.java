import grid.graph.GridGraph;
import grid.logic.LogicStatus;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlackConnectedLogicStep implements grid.logic.LogicStep<Board> {
    private static class MyReference implements GridGraph.GridReference {
        Board b;
        Set<Point> blackset = new HashSet<>();
        public MyReference(Board b,Point art) { this.b = b; }

        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }

        @Override public boolean isIncludedCell(int x, int y) {
            if (!b.onBoard(x,y)) return false;
            if (b.getCell(x,y) == CellType.BLACK) blackset.add(new Point(x,y));
            return b.getCell(x,y) != CellType.WHITE;
        }
    }

    private static class SecondReference implements GridGraph.GridReference {
        Board b;
        Set<Point> blackset;
        public SecondReference(Board b,Set<Point> blackset) { this.b = b; this.blackset = blackset; }

        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }

        @Override public boolean isIncludedCell(int x,int y) {
            return blackset.contains(new Point(x,y));
        }

    }



    @Override public LogicStatus apply(Board thing) {
        MyReference myr = new MyReference(thing,null);
        GridGraph maingg = new GridGraph(myr);
        Set<Point> blackconset = null;

        for(Point p : myr.blackset) {
            Set<Point> conset = maingg.connectedSetOf(p);
            if (blackconset != null && blackconset != conset) return LogicStatus.CONTRADICTION;
            blackconset = conset;
        }
        // if we get here, all black cells are in the same connected set.
        LogicStatus result = LogicStatus.STYMIED;
        SecondReference sr = new SecondReference(thing,blackconset);
        GridGraph artgg = new GridGraph(sr);
        for (Point p : artgg.getArticulationPoints()) {
            if (thing.getCell(p.x,p.y) == CellType.BLACK) continue;
            List<Set<Point>> arts = artgg.getArticulationSet(p);
            List<Set<Point>> blackarts = new ArrayList<>();
            for (Set<Point> art : arts) {
                if (art.stream().anyMatch(sp->thing.getCell(sp.x,sp.y) == CellType.BLACK)) blackarts.add(art);
            }
            if (blackarts.size() > 1) {
                thing.setCell(p.x,p.y,CellType.BLACK);
                result = LogicStatus.LOGICED;
            }
        }

        return result;
    }
}
