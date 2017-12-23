import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import grid.file.GridFileReader;
import grid.puzzlebits.Direction;
import java.util.Collection;
import static grid.puzzlebits.PointAdjacency.allAdjacent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by chien on 8/12/2017.
 */
public class Board
{
    GridFileReader gfr;

    public class IntegerSet extends HashSet<Integer>
    {
        public void removeAllBut(int x)
        {
            if (!contains(x)) throw new RuntimeException("Can't remove all but " + x + " when it's not there.");
            clear();
            add(x);
        }

        public boolean isSingular() { return size() == 1; }
        // getsingular will only make sense if isSingular is true!
        public int getSingular() { return iterator().next(); }

        public int getSmallest()
        {
            int result = 100;
            for (Integer i : this) { if (i < result) result = i; }
            return result;
        }

        public int getLargest()
        {
            int result = -1;
            for (Integer i : this) { if (i > result) result = i; }
            return result;
        }

        // returns true if we removed any.
        public boolean keepSmallerThan(int largest)
        {
            boolean result = false;
            Integer[] fields = toArray(new Integer[0]);
            for (int i : fields)
            {
                if (i < largest) continue;
                result = true;
                remove(i);
            }
            return result;
        }




        // returns true if we removed any.
        public boolean keepLargerThan(int smallest)
        {
            boolean result = false;
            Integer[] fields = toArray(new Integer[0]);
            for (int i : fields)
            {
                if (i > smallest) continue;
                result = true;
                remove(i);
            }
            return result;
        }
    }

    IntegerSet[][] possibles;
    Multimap<Character,Point> pointsByRegion = ArrayListMultimap.create();

    public Board(String fname)
    {
        gfr = new GridFileReader(fname);
        possibles = new IntegerSet[getWidth()][getHeight()];

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight(); ++y)
            {
                if (!hasRegion(x,y))
                {
                    if (hasLetter(x,y)) throw new RuntimeException("non-region areas should not have letters!");
                    if (!hasArrow(x,y)) throw new RuntimeException("non-region areas should be arrows!");
                    continue;
                }

                if (!hasLetter(x,y)) throw new RuntimeException("region areas should have letters!");
                if (hasArrow(x,y)) throw new RuntimeException("region areas should not be arrows!");
                pointsByRegion.put(regionId(x,y),new Point(x,y));
                possibles[x][y] =  new IntegerSet();
            }
        }

        for (Character rid : pointsByRegion.keySet())
        {
            Collection<Point> regionpoints = pointsByRegion.get(rid);
            if (!allAdjacent(regionpoints,false)) throw new RuntimeException("not all adjacent: " + rid);

            for (Point rp : regionpoints)
            {
                for (int i = 1; i <= regionpoints.size(); ++i)
                {
                    possibles[rp.x][rp.y].add(i);
                }
            }
        }
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        pointsByRegion = right.pointsByRegion;
        possibles = new IntegerSet[getWidth()][getHeight()];

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                if (!hasRegion(x,y)) continue;
                possibles[x][y] = new IntegerSet();
                possibles[x][y].addAll(right.getPossibles(x,y));
            }
        }
    }



    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }

    private String rawArrow(int x,int y) { return gfr.getBlock("ARROWS")[x][y]; }

    public boolean hasArrow(int x,int y) {return !rawArrow(x,y).equals("."); }
    public Direction getArrow(int x,int y) { return Direction.fromShort(rawArrow(x,y));}

    public boolean hasRegion(int x,int y) { return !gfr.getBlock("REGIONS")[x][y].equals("."); }
    public char regionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }


    IntegerSet getPossibles(int x,int y) { return possibles[x][y]; }
    Collection<Character> getRegionIds() { return pointsByRegion.keySet(); }
    Collection<Point>  getRegionPoints(char regionid) { return pointsByRegion.get(regionid); }

    Collection<Point> getAdjacentCells(int x,int y)
    {
        Vector<Point> result = new Vector<>();
        for (Direction d : Direction.orthogonals())
        {
            int nx = x + d.DX();
            int ny = y + d.DY();
            if (!gfr.inBounds(nx,ny)) continue;
            if (!hasRegion(nx,ny)) continue;
            result.add(new Point(nx,ny));
        }
        return result;
    }

}
