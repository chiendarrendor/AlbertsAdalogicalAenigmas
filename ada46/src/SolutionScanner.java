import grid.letter.LetterRotate;

import java.awt.*;

/**
 * Created by chien on 8/13/2017.
 */
public class SolutionScanner
{
    public static void scan(LogicBoard b, int i, boolean uselargest,boolean readhorizontal)
    {
        System.out.print("SolutionScanner " + i + " " + uselargest + ": ");

        int outermax = readhorizontal ? b.getHeight() : b.getWidth();
        int innermax = readhorizontal ? b.getWidth() : b.getHeight();

        for (int outer = 0 ; outer < outermax ; ++outer)
        {
            for (int inner = 0 ; inner < innermax ; ++inner)
            {
                int x = readhorizontal ? inner : outer;
                int y = readhorizontal ? outer : inner;

                if (!b.hasLetter(x,y)) continue;
                if (b.getPossibles(x,y).getSingular() != i) continue;
                int extreme = -1;
                for (Point p : b.getAdjacentCells(x,y))
                {
                    if (extreme == -1) { extreme = b.getPossibles(p.x,p.y).getSingular(); continue; }

                    if (uselargest)
                    {
                        if (b.getPossibles(p.x,p.y).getSingular() > extreme) extreme =  b.getPossibles(p.x,p.y).getSingular();
                    }
                    else
                    {
                        if (b.getPossibles(p.x,p.y).getSingular() < extreme) extreme = b.getPossibles(p.x,p.y).getSingular();
                    }
                }

                System.out.print(LetterRotate.Rotate(b.getLetter(x,y),extreme));
//                System.out.print("(" + x + "," + y + ")");
            }
        }
        System.out.println("");

    }
}
