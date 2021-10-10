import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Arrow {
    char id;
    Point head = null;
    List<Point> body = new ArrayList<>();

    public Arrow(char id) { this.id = id;}

    public void add(char c,int x,int y) {
        if (Character.toLowerCase(c) != id) throw new RuntimeException("non-matching character");
        if (Character.isUpperCase(c)) {
            if (head != null) throw new RuntimeException("Duplicate Head");
            head = new Point(x,y);
        } else {
            body.add(new Point(x,y));
        }
    }
}
