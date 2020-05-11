package OnTheFlyAStar;

import java.util.*;

/**
 * This class implements an A* algorithm where we know we don't have
 * the whole Graph of successors at the start, if ever, because it is
 * for one reason or another untenable to calculate the whole thing.
 */
public final class AStar {
    // utility classes should not have constructors.
    private AStar() {}

    private static class WorkNode<T extends AStarNode> {
        T thing;
        WorkNode<T> parent;
        int grade;

        public boolean isValid() {
            if (grade < 0) return false;
            if (grade > thing.winGrade()) return false;
            return true;
        }

        public boolean isSolution() {
            return grade == thing.winGrade();
        }

        public WorkNode(T thing, WorkNode<T> parent) {
            this.thing = thing;
            this.parent = parent;
            this.grade = thing.getGrade();
        }

        public String canonicalKey() { return thing.getCanonicalKey(); }

        public List<T> successors() { return thing.successors(); }
    }

    public static class AStarSolution<T extends AStarNode> {
        public List<T> solution = null;
        public List<T> bestseen = null;

        private List<T> unwindState(WorkNode<T> state) {
            List<T> result = new ArrayList<T>();
            WorkNode<T> lnode = state;
            while(lnode != null) {
                result.add(lnode.thing);
                lnode = lnode.parent;
            }
            Collections.reverse(result);
            return result;
        }


        public AStarSolution(WorkNode<T> solstate, WorkNode<T> beststate) {
            if (solstate != null) solution = unwindState(solstate);
            if (beststate != null) bestseen = unwindState(beststate);
        }


    }




    public static <T extends AStarNode> AStarSolution<T> execute(T start) {
        Set<String> seen = new HashSet<String>();
        PriorityQueue<WorkNode<T>> queue = new PriorityQueue<WorkNode<T>>( (wn1,wn2) -> Integer.compare(wn1.grade,wn2.grade));

        WorkNode<T> sn = new WorkNode<T>(start,null);
        seen.add(sn.canonicalKey());
        queue.add(sn);

        int bestgrade = Integer.MIN_VALUE;
        WorkNode<T> best = null;


        while(!queue.isEmpty()) {
            System.out.println("Queue Size: " + queue.size() + " best grade: " + bestgrade);
            final WorkNode<T> curnode = queue.poll();

            if (!curnode.isValid()) continue;

            if (curnode.grade > bestgrade) {
                best = curnode;
                bestgrade = curnode.grade;
            }

            if (curnode.isSolution()) {
                return new AStarSolution<T>(curnode,null);
            }

            curnode.successors().stream().forEach(successor -> {
                if (seen.contains(successor.getCanonicalKey())) return;
                seen.add(successor.getCanonicalKey());
                WorkNode<T> wn = new WorkNode<>(successor,curnode);
                queue.add(wn);
            });
        }

        return new AStarSolution<>(null,best);
    }
}
