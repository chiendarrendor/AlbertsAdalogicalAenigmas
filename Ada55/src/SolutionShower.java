import grid.graph.GridGraph;
import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SolutionShower {
    private static class SolutionRef implements GridGraph.GridReference {
        Board b;
        public SolutionRef(Board b) { this.b = b; }
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isIncludedCell(int x, int y) { return true; }
        public boolean edgeExitsEast(int x, int y) { return b.getEdge(x,y, Direction.EAST) == EdgeState.PATH; }
        public boolean edgeExitsSouth(int x, int y) { return b.getEdge(x,y,Direction.SOUTH) == EdgeState.PATH; }
    }


    public static void show(Board solution) {
        GridGraph gg = new GridGraph(new SolutionRef(solution));
        List<Set<Point>> groups = gg.connectedSets();
        Set<Point> onpaths = new HashSet<>();
        CellContainer<Integer> sizes = new CellContainer<Integer>(solution.getWidth(),solution.getHeight(),
                (x,y)->0,
                (x,y,r)->r);

        for (Set<Point> group : groups) {
            group.stream().forEach(p->sizes.setCell(p.x,p.y,group.size()));
            Set<Point> numbers = group.stream().filter(p->solution.hasNumber(p.x,p.y)).collect(Collectors.toSet());
            Iterator<Point> ni = numbers.iterator();
            Point p1 = ni.next();
            Point p2 = ni.next();
            onpaths.addAll(gg.shortestPathBetween(p1,p2));
        }

        StringBuffer sb = new StringBuffer();
        solution.forEachCell((x,y)->{
            if (onpaths.contains(new Point(x,y))) return;
            if (solution.hasNumber(x,y)) return;
            sb.append(LetterRotate.Rotate(solution.getLetter(x,y),sizes.getCell(x,y)));
        });

        DisplayListeners.BoardHolder bh = ()->solution;

        GridFrame gf = new GridFrame("Adalogical Aenigma #55",1000,700,
                new DisplayListeners.MyGridListener(bh,sb.toString(),solution.getSolution()),
                new DisplayListeners.MyEdgeListener(bh));



    }
}
