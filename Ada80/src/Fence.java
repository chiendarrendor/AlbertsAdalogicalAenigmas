import grid.puzzlebits.EdgeContainer;

import java.util.ArrayList;
import java.util.List;

public class Fence {
    private static int nextid = 0;
    int id;
    String name;
    Post p1 = null;
    Post p2 = null;
    List<EdgeContainer.EdgeCoord> edges = new ArrayList<>();
    public Fence(String name) { this.id = nextid++; this.name = name; }
}
