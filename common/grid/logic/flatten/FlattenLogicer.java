package grid.logic.flatten;

import grid.logic.LogicStatus;
import grid.logic.LogicerBase;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

/**
 * Created by chien on 5/20/2017.
 */

    // this particular solver is used if guesses/successors come in pairs (black/white, on/off) etc.
    // theory is that if we find a contradiction down one side's logic, but the other side isn't, then
    // the non-contradictory side can _immediately_ be applied to the original T as if it were
    // any other sort of logic from a grid.logic.LogicStep.

public class FlattenLogicer<T extends FlattenSolvable<T>> extends LogicerBase<T>
{
    public enum RecursionStatus { DEAD, DONE, GO };
    public interface IntermediateCallback<Q extends FlattenSolvable<Q>> {
        public void foundOne(Q state);
    }
    IntermediateCallback<T> callback = null;
    int maxdepth = Integer.MAX_VALUE;

    public FlattenLogicer() {}
    public FlattenLogicer(IntermediateCallback<T> callback,int maxdepth) { this.callback = callback; this.maxdepth = maxdepth; }


    // a test function that will repeat the set of logic that is run, other than the guessing.
    public void testRecursion(T thing) {
        while(true) {
            RecursionStatus rs = recursiveApplyLogic(thing);
            System.out.println("RAL 1: " + rs);
            if (rs != RecursionStatus.GO) break;

            LogicStatus ats = applyTupleSuccessors(thing);
            System.out.println("ATS: " + ats);
            if (ats != LogicStatus.LOGICED) break;

            RecursionStatus rs2 = recursiveApplyLogic(thing);
            System.out.println("RAL 1: " + rs2);
            if (rs2 != RecursionStatus.GO) break;
        }
    }




    // applies logic until
    // a) solution is reached (returns DONE)
    // b) contradiction is reached (returns DEAD)
    // c) stymied is reached (returns GO)
    public RecursionStatus recursiveApplyLogic(T thing)
    {
        while(true)
        {
            LogicStatus stat = ApplyLogic(thing);
            if (stat == LogicStatus.CONTRADICTION) return RecursionStatus.DEAD;
            if (thing.isComplete()) return RecursionStatus.DONE;
            if (stat == LogicStatus.STYMIED) return RecursionStatus.GO;
        }
    }

    // this function looks for any cases where we have a contradiction only on
    // one side of any pair.  in that case, the current item _must_ be
    // the other way.
    // contradictions on both sides of a tuple is a contradiction in input thing
    // the other cases would require guessing.

    public LogicStatus applyTupleSuccessors(T thing)
    {
        List<FlattenSolvableTuple<T>> tuples = thing.getSuccessorTuples();
        LogicStatus result = LogicStatus.STYMIED;

        for (FlattenSolvableTuple<T> tuple : tuples)
        {
            if (debug) System.out.println("Operating on Tuple " + tuple.code);
            Vector<Object> antimoves = new Vector<>();

            for (int i = 0; i < tuple.choices.size(); ++i)
            {
                T child = tuple.choices.elementAt(i);
                Object antimove = tuple.antimoves.elementAt(i);
                if (debug) System.out.println("  processing tuple child with antimove " + antimove);

                RecursionStatus rs = recursiveApplyLogic(child);
                if (rs != RecursionStatus.DEAD) continue;
                antimoves.add(antimove);
            }

            if (antimoves.size() == tuple.choices.size()) return LogicStatus.CONTRADICTION;

            for(Object antimove : antimoves)
            {
                result = LogicStatus.LOGICED;
                if (!thing.applyMove(antimove)) return LogicStatus.CONTRADICTION;
            }
        }
        return result;
    }




    @Override
    public void Solve(T start)
    {
        Vector<T> queue = new Vector<T>();
        queue.add(start);

        while(queue.size() > 0)
        {
            System.out.println("Queue Size:" + queue.size() + " solution size: " + numSolutions());
            if (queue.size() > maxdepth) break;
            T curitem = queue.remove(0);

            System.out.println("First Stage Apply Logic");
            RecursionStatus rs = recursiveApplyLogic(curitem);
            if (rs == RecursionStatus.DEAD) continue;
            if (rs == RecursionStatus.DONE)
            {
                if (ApplyLogic(curitem) != LogicStatus.CONTRADICTION)   addSolution(curitem);
                continue;
            }

            System.out.println("Logic Okay, trying tuple successors");
            LogicStatus ls = applyTupleSuccessors(curitem);

            System.out.println("Second Stage Apply Logic");
            rs = recursiveApplyLogic(curitem);
            if (rs == RecursionStatus.DEAD) continue;
            if (rs == RecursionStatus.DONE)
            {
                if (ApplyLogic(curitem) != LogicStatus.CONTRADICTION) addSolution(curitem);
                continue;
            }

            System.out.println("Dispatching on result of applyTupleSuccessors");





            switch(ls)
            {
                case LOGICED: queue.add(curitem); break;
                case CONTRADICTION: break;
                case STYMIED:
                    queue.addAll(curitem.guessAlternatives());
                    if (callback != null) callback.foundOne(curitem);
                    break;
            }

        }
        System.out.println("Final Queue Size:" + queue.size() + " solution size: " + numSolutions());

    }
}
