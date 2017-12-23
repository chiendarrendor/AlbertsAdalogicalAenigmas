package grid.lambda;

/**
 * Created by chien on 10/22/2017.
 */
public class CellLambda
{
    public static void forEachCell(int width,int height,XYLambda xyl)
    {
        for (int y = 0 ; y < height ; ++y)
        {
            for (int x = 0 ; x < width ; ++x)
            {
                xyl.operation(x,y);
            }
        }
    }

    // returns false if this terminated early.
    public static boolean terminatingForEachCell(int width,int height,BooleanXYLambda xyl)
    {
        for (int y = 0 ; y < height ; ++y)
        {
            for (int x = 0 ; x < width ; ++x)
            {
                if (!xyl.operation(x,y)) return false;
            }
        }
        return true;
    }


}
