import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Post {
    private static int nextid = 0;
    int id;
    String name;
    Point location;
    List<Fence> fences = new ArrayList<>();
    public Post(String name,Point location) { this.id = nextid++; this.name = name; this.location = location; }
}
