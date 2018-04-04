import grid.puzzlebits.Direction;

import java.util.List;
import java.awt.Point;
import java.util.ArrayList;

public class ShotName {
    Point initialPoint;
    int length;
    List<Direction> shotdirs = new ArrayList<>();

    public ShotName(Point initialPoint, int len) {
        this.initialPoint = initialPoint;
        this.length = len;
    }

    public ShotName(ShotName right, Direction symbol) {
        this.initialPoint = right.initialPoint;
        this.length = right.length;
        shotdirs.addAll(right.shotdirs);
        shotdirs.add(symbol);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[(").append(initialPoint.x).append(',').append(initialPoint.y).append(") ").append(length);
        shotdirs.stream().forEach((c)->sb.append(' ').append(c));
        sb.append(']');
        return sb.toString();
    }
}
