import grid.puzzlebits.Direction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chien on 9/3/2017.
 */

// the scan function of this class will take a board (created from a file name
// and apply all the clues from the CLUE block to it.
public class ClueScanner
{
    public static void setEdgeToPathUnlessPath(Board b,int x,int y,Direction d)
    {
        if (b.getEdge(x,y,d) == EdgeType.PATH) return;
        b.setEdge(x,y,d,EdgeType.PATH);
    }

    public static void setEdgeToWallUnlessWall(Board b,int x,int y,Direction d)
    {
        if (b.getEdge(x,y,d) == EdgeType.NOTPATH) return;
        b.setEdge(x,y,d,EdgeType.NOTPATH);
    }

    public static void setEdgesToPath(Board b,int x,int y,Direction ... pathdirs)
    {
        Set<Direction> walldirs = new HashSet<Direction>(Arrays.asList(Direction.orthogonals()));

        for(Direction d : pathdirs)
        {
            walldirs.remove(d);
            setEdgeToPathUnlessPath(b,x,y,d);
        }
        for(Direction d : walldirs) { setEdgeToWallUnlessWall(b,x,y,d); }


    }




    public static void scan(Board b)
    {
        b.forEachCell( (x,y) ->
        {
            switch (b.getClue(x, y))
            {
                case '.': break;
                case 'n':
                    setEdgesToPath(b,x,y,Direction.WEST,Direction.SOUTH);
                    break;
                case 'r':
                    setEdgesToPath(b,x,y,Direction.EAST,Direction.SOUTH);
                    break;
                case 'L':
                    setEdgesToPath(b,x,y,Direction.EAST,Direction.NORTH);
                    break;
                case 'q':
                    setEdgesToPath(b,x,y,Direction.WEST,Direction.NORTH);
                    break;
                case '|':
                    setEdgesToPath(b,x,y,Direction.NORTH,Direction.SOUTH);
                    break;
                case '-':
                    setEdgesToPath(b,x,y,Direction.WEST,Direction.EAST);
                    break;
                case '+':
                    setEdgesToPath(b,x,y,Direction.EAST,Direction.WEST,Direction.SOUTH,Direction.NORTH);
                    break;

            }

            return true;
        });




    }



}
