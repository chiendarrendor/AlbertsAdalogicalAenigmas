package grid.lambda;

import java.awt.Point;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by chien on 10/22/2017.
 */
public class CellLambda
{
    public static Stream<Point> stream(int width,int height) {
        return IntStream.range(0,height).mapToObj((y) -> IntStream.range(0,width).mapToObj((x)->new Point(x,y))).flatMap((x)->x);
    }


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
