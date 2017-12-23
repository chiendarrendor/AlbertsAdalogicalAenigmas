import java.awt.*;

/**
 * Created by chien on 4/22/2017.
 */
public class Solver extends Logicer<Board>
{
    public Solver(Board b)
    {
        super();
        addLogicStep(new NotAdjacentLogicStep());
        addLogicStep(new ConnectivityLogicStep());

        Regions regions = new Regions(b);
        for(Region r : regions.values())
        {
            if (!r.hasNumber()) continue;
            addLogicStep(new NumberedRegionLogicStep(r.getCode(),r.getNumber(),r.getPoints()));
        }

        for (int x = 0 ; x < b.getWidth() ; ++x)
        {
            addLogicStep(new LinearLogicStep(new Point(x,0),new Point(0,1),b.getHeight()));
        }

        for (int y = 0 ; y < b.getHeight() ; ++y)
        {
            addLogicStep(new LinearLogicStep(new Point(0,y),new Point(1,0),b.getWidth()));
        }



        Solve(b);
    }


}
