import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * Created by chien on 4/23/2017.
 */
public class AStar
{
    PriorityQueue<AStarPath> pq = new PriorityQueue<>();

    int reslength = -1;
    Vector<AStarPath> results = new Vector<>();

    public AStar(List<Board> starts)
    {
        for (Board b : starts)
        {
            pq.add(new AStarPath(b));
        }

        while(!pq.isEmpty())
        {
            AStarPath asp = pq.poll();
            if (asp.grade() == 0)
            {
                if (reslength == -1)
                {
                    reslength = asp.pathLen();
                }

                if (asp.pathLen() > reslength) break;
                results.add(asp);
            }
            else
            {
                pq.addAll(asp.successors());
            }
        }
    }
}
