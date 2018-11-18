// this class is a representation of the shape created by merging two
// regions together (as represented by two sets of Points)
// there are two properties we are interested in:
// 1) is this set of cells rectangular (i.e. is the set of cells equivalent to the rectangular hull of the cells extent)
// 2) what is the canonical shape identifier of this set of cells?
//
// a shape identifier of a set of cells is as follows:
// what is the width of the extent of cells?
// what is the height of the extent of cells?
// what is the encoding (binary 1 = on, binary 0 = off) of the cells in the set?
//
// a canonical shape identifier of a set of cells is, given all possible rotations and reflections of the set,
// the shape identifier with
// a) the smallest width
// b) the smallest encoding

import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class RegionPair {
    private boolean isRectangular;
    private ShapeIdentifier canonical;

    private class ShapeIdentifier implements Comparable<ShapeIdentifier> {
        int width;
        int height;
        int encoding;

        public int compareTo(ShapeIdentifier right) {
            if (width != right.width) return Integer.compare(width,right.width);
            if (height != right.height) return Integer.compare(height,right.height);
            return Integer.compare(encoding,right.encoding);
        }

        public ShapeIdentifier(CellContainer<Boolean> cc) {
            width = cc.getWidth();
            height = cc.getHeight();

            StringBuffer sb = new StringBuffer();
            sb.append('1'); // number of leading 0's is significant, so put a sentinel 1 in
            cc.forEachCell((x,y)->sb.append(cc.getCell(x,y) ? '1' : '0'));
            encoding = Integer.parseInt(sb.toString(),2);
        }

        public void show() {
            System.out.format("ShapeIdentifier: (%d,%d,%d)%n",width,height,encoding);
            String bits = Integer.toBinaryString(encoding);
            int idx = 1;
            for (int y = 0 ; y < height ; ++y) {
                for (int x = 0; x < width ; ++x) {
                    char c = bits.charAt(idx++);
                    System.out.print(c == '1' ? '#' : '.');
                }
                System.out.println("");
            }

        }

    }



    private static CellContainer<Boolean> makeContainer(Set<Point> ps) {
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;

        for (Point p: ps) {
            if (p.x < minx) minx = p.x;
            if (p.y < miny) miny = p.y;
            if (p.x > maxx) maxx = p.x;
            if (p.y > maxy) maxy = p.y;
        }

        CellContainer<Boolean> result = new CellContainer<Boolean>(maxx-minx + 1,maxy-miny+1, (x,y)->false);
        for (Point p: ps) {
            result.setCell(p.x - minx, p.y - miny, true);
        }
        return result;
    }

    private static CellContainer<Boolean> rotated(CellContainer<Boolean> orig) {
        return new CellContainer<Boolean>(orig.getHeight(),orig.getWidth(),
                (x,y)->orig.getCell(y,orig.getHeight()-x-1)
        );
    }

    private static CellContainer<Boolean> mirrored(CellContainer<Boolean> orig) {
        return new CellContainer<Boolean>(orig.getWidth(),orig.getHeight(),
                (x,y)->orig.getCell(orig.getWidth()-x-1,y));
    }

    private static void showContainer(CellContainer<Boolean> cc) {
        System.out.format("(%d,%d)%n",cc.getWidth(),cc.getHeight());
        for (int y = 0 ; y < cc.getHeight() ; ++y) {
            for (int x = 0 ; x < cc.getWidth() ; ++x) {
                System.out.print(cc.getCell(x,y) ? "#" : ".");
            }
            System.out.println("");
        }
    }


    public RegionPair(Set<Point> a, Set<Point> b) {
        Set<Point> joined = new HashSet<>();
        joined.addAll(a);
        joined.addAll(b);

        if (a.size() == 0 || b.size() == 0) throw new RuntimeException("RegionPairs must be created with non-empty regions!");

        CellContainer<Boolean> orig = makeContainer(joined);
        isRectangular = joined.size() == orig.getWidth() * orig.getHeight();

        SortedSet<ShapeIdentifier> sids = new TreeSet<>();
        sids.add(new ShapeIdentifier(orig));
        sids.add(new ShapeIdentifier(mirrored(orig)));

        CellContainer<Boolean> rot1 = rotated(orig);
        sids.add(new ShapeIdentifier(rot1));
        sids.add(new ShapeIdentifier(mirrored(rot1)));

        CellContainer<Boolean> rot2 = rotated(rot1);
        sids.add(new ShapeIdentifier(rot2));
        sids.add(new ShapeIdentifier(mirrored(rot2)));

        CellContainer<Boolean> rot3 = rotated(rot2);
        sids.add(new ShapeIdentifier(rot3));
        sids.add(new ShapeIdentifier(mirrored(rot3)));

        canonical = sids.first();
    }

    public boolean isRectangular() { return isRectangular; }
    public boolean sameShape(RegionPair rp) { return canonical.compareTo(rp.canonical) == 0;}
    public void showCanonicalShape() { canonical.show(); }

}
