package grid.logic.simple;

import grid.logic.LogicStatus;
import grid.logic.LogicerBase;

import java.util.*;

public class Logicer<T extends Solvable<T>> extends LogicerBase<T>
{
        public void Solve(T original)
        {
            Vector<T> queue = new Vector<T>();
            queue.add(original);
            
            while(queue.size() > 0)
            {
                T cur = queue.remove(0);
                System.out.println("queue size: " + queue.size() + " solution size: " + numSolutions());

                LogicStatus stat = ApplyLogic(cur);
                switch(stat)
                {
                case CONTRADICTION:
                    break;
                case LOGICED:
                    queue.add(cur);
                    break;
                case STYMIED:
                    if (cur.isSolution())
                    {
                        addSolution(cur);
                    }
                    else
                    {
                        List<T> successors = cur.guessAlternatives();
                        for (T s : successors) queue.add(0, s);
                    }
                    break;       
                }
            }
        }
}
	
	