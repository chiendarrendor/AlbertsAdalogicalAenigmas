import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Knight {
    private static int nextId = 0;

    // the number of moves the knight has to make (-1 if unlimited)
    private int moveCount;
    private Point startingPoint;
    private int id;
    private KnightStep stepTree;
    private boolean locked;
    private int allowableFeasibles = 1;

    // This is ephemeral, used to communicate between the most recent KnightLogicStep for each knight, and a subsequent guessAlternatives.
    private List<KnightStep> feasibles = new ArrayList<>();
    private List<KnightStep> unexpandeds = new ArrayList<>();

    public Knight(Point p, int size){
        startingPoint = p;
        id = ++nextId;
        moveCount = size;
        stepTree = new KnightStep(null,p,0,size==-1 || size == 0);
        locked = false;
    }

    public Knight(Knight right) {
        moveCount = right.moveCount;
        startingPoint = right.startingPoint;
        id = right.id;
        stepTree = new KnightStep(right.stepTree);
        locked = right.locked;
        allowableFeasibles = right.allowableFeasibles;
    }

    public Point getStartingPoint() { return startingPoint; }
    public int getId() { return id; }
    public int numPaths() { return stepTree.numPaths(); }
    public int getMaxDepth() { return moveCount; }
    public List<Point> getUniquePathList() { return stepTree.feasibles().get(0).getPath(); } // only reasonable if numPaths == 1

    // new 'hasZero' stuff.
    public boolean hasZero() { return stepTree.isFeasible(); }
    public void clearZero() { stepTree.markInfeasable(); }
    public void setZero() { stepTree.removeChildren(); }

    public boolean isLocked() { return locked; }
    public void lock() { locked = true; }

    public KnightStep getStepTree() { return stepTree; }

    public void clearEphemera() {
        feasibles.clear();
        unexpandeds.clear();
    }

   public void addFeasible(KnightStep step) {
        feasibles.add(step);
   }
   public void addUnexpanded(KnightStep step) {
        unexpandeds.add(step);
   }

   public int feasibleCount() { return feasibles.size(); }
   public int unexpandedCount() { return unexpandeds.size(); }
   public Collection<KnightStep> getFeasibles() { return feasibles; }
   public Collection<KnightStep> getUnexpandeds() { return unexpandeds; }

   // given a KnightStep, find the KnightStep in this that is equivalent (might be a copy-con's copy of us, so we can't use object ==)
   public KnightStep findKnightStep(KnightStep original) {
        List<Point> pathPoints = new ArrayList<>();
        for( ; original != null ; original = original.getParent()) {
            pathPoints.add(0,original.getPoint());
        }
        if (!pathPoints.get(0).equals(getStartingPoint())) throw new RuntimeException("This KnightStep doesn't belong to us!");

        KnightStep result = stepTree;
        for (int i = 1 ; i < pathPoints.size() ; ++i) {
            Point cp = pathPoints.get(i);

            if (!result.containsChild(cp)) throw new RuntimeException("This Knight tree doesn't contain this path");
            result = result.getChild(cp);
        }
        return result;
   }

   public int getAllowableFeasibles() { return allowableFeasibles; }
   public void increaseAllowableFeasibles() { allowableFeasibles *= 2; }


}
