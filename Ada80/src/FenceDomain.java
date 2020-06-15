import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FenceDomain {
    Map<Integer,Fence> fences = new HashMap<>();
    Map<Integer,Post> posts = new HashMap<>();
    Map<Point,Post> postsbylocation = new HashMap<>();

    public FenceDomain(Board b) {
        Set<Integer> xcoords = new HashSet<>();
        Set<Integer> ycoords = new HashSet<>();

        b.forEachCell((x,y)-> {
            if (!b.hasPost(x,y)) return;
            xcoords.add(x);
            ycoords.add(y);
            Point p = new Point(x,y);
            Post np = new Post("("+x+","+y+")",p);
            posts.put(np.id,np);
            postsbylocation.put(p ,np);
            if (x == b.getWidth() - 1 || y == b.getHeight() - 1) throw new RuntimeException("Can't have a post on a board edge!");
        });

        // we've kept track of the xcoords and ycoords because there will be a complete coverage of all internal
        // edges of the board if and only if there is at least one post on every internal x coordinate and every internal y coordinate
        if (xcoords.size() < b.getWidth() - 1) throw new RuntimeException("Board is not completely covered x");
        if (ycoords.size() < b.getHeight() - 1) throw new RuntimeException("Board is not completely covered y");



        for (Post post : posts.values()) {
            for (Direction d : Direction.orthogonals()) {
                Fence newfence = new Fence("(" + post.location.x + "," + post.location.y + "," + d + ")");
                newfence.p1 = post;
                boolean atEdge = false;
                boolean isPost = false;
                for (int i = 1 ; ; ++i) {
                    Point npost = d.delta(post.location,i);
                    isPost = postsbylocation.containsKey(npost);
                    switch(d) {
                        case NORTH:
                            atEdge = npost.y == -1;
                            newfence.edges.add(new EdgeContainer.EdgeCoord(npost.x+1,npost.y+1,true));
                            break;
                        case SOUTH:
                            atEdge = npost.y == b.getHeight() - 1;
                            newfence.edges.add(new EdgeContainer.EdgeCoord(npost.x+1,npost.y,true));
                            break;
                        case EAST:
                            atEdge = npost.x == b.getWidth() - 1;
                            newfence.edges.add(new EdgeContainer.EdgeCoord(npost.x,npost.y+1,false));
                            break;
                        case WEST:
                            atEdge = npost.x == -1;
                            newfence.edges.add(new EdgeContainer.EdgeCoord(npost.x+1,npost.y+1,false));
                            break;
                        default: throw new RuntimeException("Unknown direction " + d);
                    }
                    if (isPost) {
                        newfence.p2 = postsbylocation.get(npost);
                    }
                    if (atEdge || isPost) break;
                }

                if (atEdge) {
                    fences.put(newfence.id,newfence);
                    post.fences.add(newfence);
                }
                if (isPost && (d == Direction.EAST || d == Direction.SOUTH)) {
                    fences.put(newfence.id,newfence);
                    post.fences.add(newfence);
                    newfence.p2.fences.add(newfence);
                }
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Post p : posts.values()) {
            sb.append("Post: " + p.id  + p.name + ":" );
            for (Fence f : p.fences) sb.append(" " + f.name);
            sb.append("\n");
        }

        return sb.toString();
    }

}
