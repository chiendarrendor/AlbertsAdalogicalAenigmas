/**
 * Created by chien on 2/21/2017.
 */
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;
import java.awt.Point;
import java.util.*;

public class BoardConnectivityCalculator extends SimpleGraph<Point,DefaultEdge>
{
    ConnectivityInspector<Point,DefaultEdge> ci = null;
    public BoardConnectivityCalculator(Board b)
    {
        super(DefaultEdge.class);
        for (int x = 0 ; x < b.getWidth() ; ++x)
        {
            for (int y = 0 ; y < b.getHeight() ; ++y)
            {
                if (b.getCellInfo(x,y).type == CellInfo.WALL) continue;
                addVertex(new Point(x,y));
            }
        }

        for(Point p : vertexSet())
        {
            Point eastP = new Point(p.x+1,p.y);
            Point southP = new Point(p.x,p.y+1);
            Point southeastP = new Point(p.x+1,p.y+1);
            Point southwestP = new Point( p.x-1,p.y+1);
            if (containsVertex(eastP)) addEdge(p,eastP);
            if (containsVertex(southP)) addEdge(p,southP);
            if (containsVertex(southeastP)) addEdge(p,southeastP);
            if (containsVertex(southwestP)) addEdge(p,southwestP);
        }

        ci = new ConnectivityInspector<Point,DefaultEdge>(this);
    }

    public boolean isConnected()
    {
        return ci.isGraphConnected();
    }

}
