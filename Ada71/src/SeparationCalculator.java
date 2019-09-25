
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeparationCalculator {
    Point sourcepoint;
    Board thing;
    List<Point> fullrow;
    int intersum;

    Map<Point,Integer> indices = new HashMap<>();
    int sourceindex;

    List<Terminal> terminals = new ArrayList<>();

    public SeparationCalculator(Point point, Board thing, List<Point> cells, int intersum) {
        sourcepoint = point;
        this.thing = thing;
        fullrow = cells;
        this.intersum = intersum;

        for (int i = 0 ; i < cells.size() ; ++i) {
            indices.put(cells.get(i),i);
        }
        if (!indices.containsKey(point)) throw new RuntimeException("list of cells doesn't contain point!");
        sourceindex = indices.get(point);
    }

    public void addOtherEnd(Point p) {
        if (!indices.containsKey(p)) throw new RuntimeException("List of cells doesn't contain other-end point!");
        int destindex = indices.get(p);

        int start = Math.min(sourceindex,destindex);
        int end = Math.max(sourceindex,destindex);

        List<Point> inters = new ArrayList<>();
        for (int i = start+1 ; i < end ; ++i) {
            inters.add(fullrow.get(i));
        }
        Terminal t = new Terminal(p,inters,thing,intersum);
        if (t.hasNumbering()) terminals.add(t);
    }

    public int getTerminalCount() {
        return terminals.size();
    }

    public Terminal getSoloTerminal() {
        return terminals.get(0);
    }
}
