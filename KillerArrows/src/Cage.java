import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Cage {
    char id;
    int size = -1;
    List<Point> cells = new ArrayList<>();
    public Cage(char id ) { this.id = id; }
    public void add(int x,int y) { cells.add(new Point(x,y)); }
    public void addCount(int c) { size = c; }
}
