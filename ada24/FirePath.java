
import java.util.*;
import java.awt.Point;

public class FirePath extends Vector<PathElement>
{	
    public FirePath(){}
    public FirePath(FirePath right)
    {
        for(PathElement pe : right)
        {
            add(new PathElement(pe));
        }
    }

    public String toString()
    {
            StringBuffer sb = new StringBuffer();
            for( PathElement pe : this) { sb.append(pe); }
            return sb.toString();
    }

    public LogicStatus ApplyPath(State s)
    {
        LogicStatus status = LogicStatus.STYMIED;
        for (PathElement pe : this)
        {
            if (pe.isHypothetical) status = LogicStatus.LOGICED;
            s.state[pe.p.x][pe.p.y] = pe.state;
        }
        return status;
    }
}
	