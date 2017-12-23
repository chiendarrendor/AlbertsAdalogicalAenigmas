import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.Set;

/**
 * Created by chien on 7/3/2017.
 */
public class SolutionOverlay
{
    boolean isTerminal[][];
    boolean isEast[][];
    boolean isWest[][];
    boolean isNorth[][];
    boolean isSouth[][];
    int m_rabid[][];





    // takes as argument a Board that is a valid solution
    public SolutionOverlay(Board b)
    {
        isTerminal = new boolean[b.getWidth()][b.getHeight()];
        isEast = new boolean[b.getWidth()][b.getHeight()];
        isWest = new boolean[b.getWidth()][b.getHeight()];
        isNorth = new boolean[b.getWidth()][b.getHeight()];
        isSouth = new boolean[b.getWidth()][b.getHeight()];
        m_rabid = new int[b.getWidth()][b.getHeight()];

        for (int x = 0 ; x < b.getWidth() ; ++x)
        {
            for (int y = 0 ; y < b.getHeight() ; ++y)
            {
                isTerminal[x][y] = false;
                isEast[x][y] = false;
                isWest[x][y] = false;
                isNorth[x][y] = false;
                isSouth[x][y] = false;
                m_rabid[x][y] = -1;
            }
        }




        for(int rabid = 0 ; rabid < b.getNumRabbits() ; ++rabid)
        {
            Set<Integer> hops = b.getLiveHopSet(rabid);
            if (hops.size() != 1) throw new RuntimeException("This was supposed to be a solution!");
            int hopid = hops.iterator().next();

            RabbitHops.Rabbit rabbit = b.hops.rabbits.elementAt(rabid);
            RabbitHops.RabbitHop hop = rabbit.hops.elementAt(hopid);
            Direction dir = hop.dir;
            Point endspace = hop.cells.lastElement();

            m_rabid[rabbit.sx][rabbit.sy] = rabid;
            m_rabid[endspace.x][endspace.y] = rabid;


            // process end space
            isTerminal[endspace.x][endspace.y] = true;

            // process start and end spaces
            switch(dir)
            {
                case NORTH:
                    isNorth[rabbit.sx][rabbit.sy] = true;
                    isSouth[endspace.x][endspace.y] = true;
                    break;
                case SOUTH:
                    isSouth[rabbit.sx][rabbit.sy] = true;
                    isNorth[endspace.x][endspace.y] = true;
                    break;
                case EAST:
                    isEast[rabbit.sx][rabbit.sy] = true;
                    isWest[endspace.x][endspace.y] = true;
                    break;
                case WEST:
                    isWest[rabbit.sx][rabbit.sy] = true;
                    isEast[endspace.x][endspace.y] = true;
                    break;
            }

            // process intermediate spaces
            for (int cid = 0 ; cid < hop.cells.size() - 1 ; ++cid)
            {
                Point p = hop.cells.elementAt(cid);
                m_rabid[p.x][p.y] = rabid;
                switch(dir)
                {
                    case NORTH:
                    case SOUTH:
                        isNorth[p.x][p.y] = true;
                        isSouth[p.x][p.y] = true;
                        break;
                    case EAST:
                    case WEST:
                        isEast[p.x][p.y] = true;
                        isWest[p.x][p.y] = true;
                        break;
                }
            }



        }

        System.out.print("Main Clue: ");
        for (int y = 0 ; y < b.getHeight() ; ++y)
        {
            for (int x = 0 ; x < b.getWidth() ; ++x)
            {
                if (m_rabid[x][y] == -1) continue;
                if (!b.hasLetter(x,y)) continue;
                Set<Integer> hops = b.getLiveHopSet(m_rabid[x][y]);
                int hopid = hops.iterator().next();
                RabbitHops.Rabbit rabbit = b.hops.rabbits.elementAt(m_rabid[x][y]);
                RabbitHops.RabbitHop hop = rabbit.hops.elementAt(hopid);
                char let = b.getLetter(x,y);
                int count = hop.cells.size();
                System.out.print(LetterRotate.Rotate(let,count));

            }
        }
        System.out.println("");

        System.out.print("anti-clue? ");
        for (int y = 0 ; y < b.getHeight() ; ++y)
        {
            for (int x = 0 ; x < b.getWidth() ; ++x)
            {
                if (m_rabid[x][y] != -1) continue;
                if (!b.hasLetter(x,y)) continue;
                System.out.print(b.getLetter(x,y));
            }
        }
        System.out.println("");


    }
}
