import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chien on 10/1/2017.
 */
public class Region
{
    private enum RegionType { HOR,VERT,EQUAL};
    static char nextRegion = 'a';

    Set<Rectangle> possibles = new HashSet<>();
    char rid;
    int cx;
    int cy;
    RegionType type;


    public Region(Board b,int cx,int cy)
    {
        this.rid = nextRegion++;
        this.cx = cx;
        this.cy = cy;

        switch (b.getFrag(cx, cy))
        {
            case '-':
                type = RegionType.HOR;
                break;
            case '|':
                type = RegionType.VERT;
                break;
            case '+':
                type = RegionType.EQUAL;
                break;
            default:
                throw new RuntimeException("Region must be centered on a frag");
        }

        b.getCell(cx, cy).setIs(rid);
    }

    public Region(Region right)
    {
        possibles.addAll(right.possibles);
        rid = right.rid;
        cx = right.cx;
        cy = right.cy;
        type = right.type;
    }



    public void Expand(Board b)
    {
        for (int w = 1 ; w <= b.getWidth() ; ++w)
        {
            for (int h = 1; h <= b.getHeight() ; ++h )
            {
                if (type == RegionType.EQUAL && w != h) continue;
                if (type == RegionType.HOR && w <= h) continue;
                if (type == RegionType.VERT && w >= h) continue;

                for (int dx = 0 ; dx < w ; ++dx)
                {
                    int ulx = cx - dx;
                    int lrx = ulx + w - 1;

                    for (int dy = 0 ; dy < h ; ++dy)
                    {
                        int uly = cy - dy;
                        int lry = uly + h - 1;

                        if (ulx < 0) continue;
                        if (lrx >= b.getWidth()) continue;
                        if (uly < 0) continue;
                        if (lry >= b.getHeight()) continue;

                        Rectangle r = new Rectangle(ulx,uly,w,h);

                        // can't use fits here because this body of code is what produces the possible sets in each cell.
                        // we have to do a similar mechanism to fits, but rejecting cells in the rectangle that are
                        // fixed and not us (i.e. someone else's starting point.
                        boolean[] result = new boolean[1];
                        result[0] = true;


                        b.forEachRectangleCell(r,(x,y)->{
                            if (x == cx && y == cy) return true;
                            if (b.getCell(x,y).isFixed()) { result[0] = false;  return false; }
                            return true;
                        });

                        if (result[0] == false) continue;

                        possibles.add(r);


                        b.forEachRectangleCell(r,(x,y) -> {
                            if (x == cx && y == cy) return true;
                            b.getCell(x,y).setPossible(rid);
                            return true;
                        });
                    }
                }
            }
        }
    }



}
