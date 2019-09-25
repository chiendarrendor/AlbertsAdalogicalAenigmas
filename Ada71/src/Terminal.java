import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Terminal {
    Point terminalpoint;
    List<Point> interpoints;
    List<int[]> numberings;
    Board thing;
    int intersum;

    private static int sum(int[] ar) { return Arrays.stream(ar).sum(); }


    private List<int[]> recursiveCalculateNumberings(int curnum,int[] prefix) {
        List<int[]> result = new ArrayList<>();
        int sumprefix = sum(prefix);
        if (sumprefix > intersum) return result;

        if (curnum >= interpoints.size()) {
            if (sumprefix < intersum) return result;
            result.add(prefix);
            return result;
        }

        Point curp = interpoints.get(curnum);
        CellSet cs = thing.getCell(curp.x,curp.y);

        for(int v : cs) {
            if (v == CellSet.BLACK) continue;
            if (Arrays.stream(prefix).anyMatch(x->x==v)) continue;
            int[] nextitem = new int[prefix.length+1];
            System.arraycopy(prefix,0,nextitem,0,prefix.length);
            nextitem[prefix.length] = v;
            result.addAll(recursiveCalculateNumberings(curnum+1,nextitem));
        }

        return result;
    }





    public Terminal(Point p, List<Point> inters, Board thing, int intersum) {
        this.thing = thing;
        terminalpoint = p;
        interpoints = inters;
        this.intersum = intersum;

        numberings = recursiveCalculateNumberings(0,new int[0]);

    }


    public Point getTerminalPoint() {
        return terminalpoint;
    }

    public List<Point> getInterPoints() {
        return interpoints;
    }

    public boolean hasNumbering() {
        return numberings.size() > 0;
    }

    public void showNumberings() {
        System.out.println("# of Numberings: " + numberings.size());
        for (int[] ar : numberings) {
            System.out.print("[");
            for (int i = 0 ; i < ar.length ; ++i) { System.out.print(" "+ar[i]); }
            System.out.println(" ]");
        }
    }

    public static void test(Board b) {
        Point startp = new Point(1,1);
        for (int length = 0 ; length < 4 ; ++length) {
            for (int sum = 0; sum < 7 ; ++sum) {
                System.out.println("length: " + length + " sum: " + sum);
                List<Point> inters = new ArrayList<>();
                for(int i = 1 ; i <= length ; ++i) {
                    inters.add(Direction.SOUTH.delta(startp,i));
                }
                Terminal t = new Terminal(Direction.SOUTH.delta(startp,length+1),inters,b,sum);
                t.showNumberings();
            }
        }
    }
}
