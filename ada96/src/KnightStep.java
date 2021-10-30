import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnightStep {
    private static int nextId = 0;

    private int id;
    private Point thisPoint;
    private Segment jumpSegment;
    private KnightStep parent;
    private Map<Point,KnightStep> children = new HashMap<>();
    private boolean expanded = false;
    private int depth;
    // true if the path that ends here can be a final destination for the knight
    // false if the only use for this path is as an intermediate step.
    private boolean feasible;

    public KnightStep(KnightStep parent,Point p,int depth,boolean feasible) {
        this.id = ++nextId;
        this.parent = parent;
        thisPoint = p;
        if (parent != null) jumpSegment = new Segment(parent.getPoint(),p);
        this.depth = depth;
        this.feasible = feasible;
    }

    public KnightStep(KnightStep right) {
        this(right,null);
        if (right.parent != null) throw new RuntimeException("Only call KnightStep copycon on root");
    }


    private KnightStep(KnightStep right,KnightStep parent) {
        this.id = right.id;
        this.thisPoint = right.thisPoint;
        this.jumpSegment = right.jumpSegment;
        this.parent = parent;
        this.expanded = right.expanded;
        this.depth = right.depth;
        this.feasible = right.feasible;

        for (Point p : right.children.keySet()) {
            children.put(p,new KnightStep(right.children.get(p),this));
        }
    }

    public Point getPoint() { return thisPoint; }
    public KnightStep getParent() { return parent; }
    public Segment getJumpSegment() { return jumpSegment;  }
    public int getId() { return id; }
    public boolean isExpanded() { return expanded; }
    public int getDepth() { return depth; }
    public boolean isFeasible() { return feasible; }
    public void markInfeasable() { feasible = false; }
    public int getChildCount() { return children.size(); }
    public Collection<KnightStep> getChildren() { return children.values(); }
    public boolean containsChild(Point p) {
        return children.containsKey(p);
    }
    public KnightStep getChild(Point p) {
        return children.get(p);
    }

    public void delete() {
        if (parent == null) throw new RuntimeException("Can't delete root");
        parent.children.remove(thisPoint);
    }

    // clears all steps from the tree other than this one
    public void set() {
        set(null);
    }

    private void set(Point savedChild) {
        if (savedChild == null) {
            removeChildren();
            if (!feasible) throw new RuntimeException("Shouldn't set() an infeasible!");
        } else {
            if (!expanded) throw new RuntimeException("How did we have a child if we haven't expanded?");
            feasible = false; // if our child (recursively) is the one being set, then we're not feasible any more
            KnightStep saved = children.get(savedChild);
            children.clear();
            children.put(savedChild,saved);
        }
        if (parent != null) parent.set(thisPoint);
    }


    public void removeChildren() {
        expanded = true;
        children.clear();
    }



    public void expand(boolean feasible) {
        if (expanded) throw new RuntimeException("Can't expand an already expanded jump");
        expanded = true;
        List<Point> adjacents = KnightsJump.destinations(thisPoint);

        for (Point p : adjacents) {
            children.put(p,new KnightStep(this,p,depth+1,feasible));
        }
    }

    public List<Point> getPath() {
        if (parent == null) {
            List<Point> result = new ArrayList<>();
            result.add(thisPoint);
            return result;
        } else {
            List<Point> parentList = parent.getPath();
            parentList.add(thisPoint);
            return parentList;
        }
    }

    // this counts the number of nodes (internal and leaf) that are feasible.
    // returns Integer.MAX if any leaf node is unexpanded
    // throws an exception if any non-leaf node is unexpandeed
    public int numPaths() {
        int sum = 0;
        if (!expanded && children.size() > 0) throw new RuntimeException("An unexpanded node shouldn't have children!");
        if (expanded) {
            for (KnightStep child : children.values()) {
                int cvalue = child.numPaths();
                if (cvalue == Integer.MAX_VALUE) return Integer.MAX_VALUE;
                sum += cvalue;
            }
        } else {
            return Integer.MAX_VALUE;
        }
        // if we get here, neither ourselves nor any of our children are unexpanded, so we can return a value
        return sum + (feasible ? 1 : 0);
    }

    public List<KnightStep> feasibles() {
        List<KnightStep> result = new ArrayList<>();
        feasibles(result);
        return result;
    }

    private void feasibles(List<KnightStep> result) {
        if (!expanded && children.size() > 0) throw new RuntimeException("An unexpanded node shouldn't have children!");
        for (KnightStep child: children.values()) {
            child.feasibles(result);
        }
        if (feasible) result.add(this);
    }


}
