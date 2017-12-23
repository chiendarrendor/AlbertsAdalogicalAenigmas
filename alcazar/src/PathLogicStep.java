import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;
import sun.rmi.runtime.Log;

import java.util.Collection;
import java.util.Vector;

/**
 * Created by chien on 7/24/2017.
 */
public class PathLogicStep implements grid.logic.LogicStep<LogicBoard>
{
    @Override
    public LogicStatus apply(LogicBoard thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        Board b = thing.getBoard();
        PathManager pm = b.getPathManager();

        if (!pm.isLive) return LogicStatus.CONTRADICTION;

        Collection<PathManager.Path> paths = pm.getAllActivePaths();

        boolean foundDualTerminal = false;

        //System.out.println("Path Processing: ");

        Vector<PathManager.Path> nonDualTerminals = new Vector<>();

        for (PathManager.Path path : paths)
        {
            //System.out.println("\t" + path);


            if (path.isLoop) return LogicStatus.CONTRADICTION;
            if (b.isOutside(path.end1) && b.isOutside(path.end2))
            {
                // should not have two dualterminals
                if (foundDualTerminal) return LogicStatus.CONTRADICTION;
                // but one is fine.
                foundDualTerminal = true;
            }
            else
            {
                nonDualTerminals.add(path);
            }

            if (b.isOutside(path.end1) || b.isOutside(path.end2)) continue;

            // is end1 and end2 adjacent?
            for (Direction dir : Direction.orthogonals())
            {
                if (path.end1.getAdjacent(dir) != path.end2) continue;
                if (b.getEdge(path.end1.x,path.end1.y,dir) == EdgeState.UNKNOWN)
                {
                    result = LogicStatus.LOGICED;
                    b.wall(path.end1.x,path.end1.y,dir);
                }
            }
        }

        if (foundDualTerminal)
        {
            for(PathManager.Path path : nonDualTerminals)
            {
                if (path.end1 != path.end2) return LogicStatus.CONTRADICTION;
                if (!path.end1.isOptional) return LogicStatus.CONTRADICTION;
            }
        }

        return result;
    }
}
