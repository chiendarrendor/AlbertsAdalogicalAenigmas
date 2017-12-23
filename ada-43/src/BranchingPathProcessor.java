import java.awt.*;
import java.util.Vector;
import java.util.List;

/**
 * Created by chien on 5/6/2017.
 */
public class BranchingPathProcessor
{
    public BranchingPath bestSolution = null;

    public BranchingPathProcessor(Point p, Board b)
    {
        Vector<BranchingPath> queue = new Vector<>();


        queue.add(new BranchingPath(p));

        while(queue.size() > 0)
        {
//            System.out.print("Branching Path Queue Size: " + queue.size() + " Length: ");
//            if (bestSolution == null)
//            {
//                System.out.println("--null--");
//            }
//            else
//            {
//                System.out.println(bestSolution.length());
//            }

            BranchingPath cur = queue.remove(0);
            List<BranchingPath> successors = cur.walk(b);
            if (successors == null)
            {
                if (bestSolution == null)
                {
                    bestSolution = cur;
                }
                else
                {
                    if (cur.length() > bestSolution.length())
                    {
                        bestSolution = cur;
                    }
                }
            }
            else
            {
                queue.addAll(successors);
            }
        }
    }
}
