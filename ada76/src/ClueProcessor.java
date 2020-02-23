import grid.graph.GridGraph;
import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.Set;

public class ClueProcessor {

    private static class MyReference implements GridGraph.GridReference {
        Board b;
        public MyReference(Board b) { this.b = b; }

        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return b.getCell(x,y).isBlank(); }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    public static String Process(Board b) {
        CellContainer<Integer> groupsize = new CellContainer<>(b.getWidth(),b.getHeight(),(x,y)->0);
        GridGraph gg = new GridGraph(new MyReference(b));

        for (Set<Point> conset : gg.connectedSets() ) {
            for (Point p : conset) {
                groupsize.setCell(p.x,p.y,conset.size());
            }
        }

        StringBuffer sb = new StringBuffer();
        b.forEachCell((x,y)-> {
            int gsize = groupsize.getCell(x,y);
            if (gsize == 0 || gsize > 2) return;
            sb.append(LetterRotate.Rotate(b.getLetter(x,y),gsize));
        });

        return sb.toString();

    }
}
