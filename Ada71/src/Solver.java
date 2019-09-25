import grid.logic.flatten.FlattenLogicer;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        List<Rectangle> boxes = b.boxes;
        int size = b.boxsize;
        BoxOrganizer bo = new BoxOrganizer(boxes);

        for (Rectangle r : boxes) {
            Point ti = bo.getTierInfo(r);
            for (int m = 0 ; m < size ; ++m) {
                int mx = r.x + m;
                int my = r.y + m;

                List<Point> cp = new ArrayList<>();
                for (int i = 0 ; i < size ; ++i) {
                    cp.add(new Point(i+r.x,my));
                }

                addLogicStep(new UniqueLogicStep(cp));
                int xclue = b.getXBlock(ti.x,my);
                if (xclue >= 0) {
                    addLogicStep(new SeparationLogicStep(xclue,cp));
                }

                cp = new ArrayList<>();
                for (int i = 0 ; i < size ; ++i) {
                    cp.add(new Point(mx,i+r.y));
                }
                addLogicStep(new UniqueLogicStep(cp));

                int yclue = b.getYBlock(ti.y,mx);
                if (yclue >= 0) {

                    addLogicStep(new SeparationLogicStep(yclue,cp));
                }
            }
        }
    }
}
