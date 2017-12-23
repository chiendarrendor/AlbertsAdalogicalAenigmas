package grid.logic.astar;

import grid.logic.LogicStatus;
import grid.logic.LogicerBase;

import java.util.*;

/**
 * Created by chien on 5/20/2017.
 */
public class AStarLogicer<T extends AStarSolvable<T>> extends LogicerBase<T>
{
    private class TComparator implements Comparator<T>
    {
        @Override
        public int compare(T o1, T o2)
        {
            return o2.grade() - o1.grade();
        }
    }

    private enum RecursionStatus { DEAD, DONE, GO };

    // applies logic until
    // a) solution is reached (returns DONE)
    // b) contradiction is reached (returns DEAD)
    // c) stymied is reached (returns GO)
    private RecursionStatus recursiveApplyLogic(T thing)
    {
        while(true)
        {
            LogicStatus stat = ApplyLogic(thing);
            if (stat == LogicStatus.CONTRADICTION) return RecursionStatus.DEAD;
            if (thing.grade() == thing.winGrade()) return RecursionStatus.DONE;
            if (stat == LogicStatus.STYMIED) return RecursionStatus.GO;
        }
    }

    private int numCycles = -1;
    public void setCycles(int x) { numCycles = x;}

    public void Solve(T start)
    {

        // once we have pulled a given state from the queue, we never need to insert it again.
        Set<String> seenSet = new HashSet<>();


        // the only thing that should be in the priority queue are
        // non-solution stymied
        PriorityQueue<T> queue = new PriorityQueue<T>(11,new TComparator());
        // make sure that start is non-solution stymied.
        RecursionStatus rs = recursiveApplyLogic(start);
        if (rs == RecursionStatus.DEAD) return;
        if (rs == RecursionStatus.DONE)
        {
            addSolution(start);
            return;
        }
        // must be GO! :-)
        queue.add(start);


        int ctr = 0;
        while(queue.size() > 0 && (numCycles == -1 || (numCycles - ctr > 0)))
        {
            ++ctr;
           T curThing = queue.poll();
           seenSet.add(curThing.canonicalKey());
           System.out.println("queue size: " + queue.size() + " solution size: " + numSolutions() + " best grade: " + curThing.grade());

            // we know that curThing is not a solution and stymied, so it doesn't go back into the queue
            List<T> successors = curThing.successors();
            int deadcount = 0;
            int livecount = 0;
            T contra = null;
            for(T succ : successors)
            {
                rs = recursiveApplyLogic(succ);
                if (rs == RecursionStatus.DEAD)
                {
                    ++deadcount;
                    contra = succ;
                    continue;
                }
                if (rs == RecursionStatus.DONE)
                {
                    addSolution(succ);
                    continue;
                }
                ++livecount;
                if (seenSet.contains(succ.canonicalKey())) continue;
                queue.add(succ);

            }


            System.out.println("out of " + successors.size() + ": " + deadcount + " contradictions, " + livecount + " possibles");
            addSolution(contra);
            return;
        }
        if (queue.size() > 0 && numSolutions() == 0)
        {
            addSolution(queue.poll());
        }
    }
}
