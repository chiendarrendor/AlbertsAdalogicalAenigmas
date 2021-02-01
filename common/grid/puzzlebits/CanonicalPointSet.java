package grid.puzzlebits;


import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// this class will take a set of points,
// rebase them so that the upper left corner of the minimal bounding box is at 0,0
// and provide access to rotations and reflections for equivalent-shape analysis
public class CanonicalPointSet {
    int width;
    int height;
    boolean[][] grid;
    Set<String> equivalences = new HashSet<>();

    public CanonicalPointSet(Collection<Point> points) {
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;

        for (Point p : points) {
            if (p.x > maxx) maxx = p.x;
            if (p.x < minx) minx = p.x;
            if (p.y > maxy) maxy = p.y;
            if (p.y < miny) miny = p.y;
        }

        width = maxx-minx+1;
        height = maxy-miny+1;
        grid = new boolean[width][height];
        for (Point p : points) {
            grid[p.x-minx][p.y-miny] = true;
        }

        equivalences.add(toString());
        equivalences.add(stringOfGrid(width,height,mirror(width,height,grid),'/'));
        boolean[][] g2 = rotateCW(width,height,grid);
        equivalences.add(stringOfGrid(height,width,g2,'/'));
        equivalences.add(stringOfGrid(height,width,mirror(height,width,g2),'/'));
        boolean[][] g3 = rotateCW(height,width,g2);
        equivalences.add(stringOfGrid(width,height,g3,'/'));
        equivalences.add(stringOfGrid(width,height,mirror(width,height,g3),'/'));
        boolean[][] g4 = rotateCW(width,height,g3);
        equivalences.add(stringOfGrid(height,width,g4,'/'));
        equivalences.add(stringOfGrid(height,width,mirror(height,width,g4),'/'));
    }


    public boolean equivalent(CanonicalPointSet other) {
        return equivalences.contains(other.toString());
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean[][] getGrid() { return grid; }


    public String stringOfGrid(int thewidth,int theheight,boolean[][] thegrid,char sep) {
        StringBuffer sb = new StringBuffer();
        for (int y = 0 ; y < theheight ; ++y) {
            for (int x = 0 ; x < thewidth ; ++x) {
                sb.append(thegrid[x][y]?'@':'.');
            }
            sb.append(sep);
        }
        return sb.toString();
    }

    public boolean[][] mirror(int thewidth,int theheight,boolean[][] thegrid) {
        boolean[][] result = new boolean[thewidth][theheight];
        for (int sy=0,dy=0; sy<theheight; ++sy,++dy) {
            for(int sx=0,dx=thewidth-1;sx<thewidth ; ++sx,--dx) {
                result[dx][dy]=thegrid[sx][sy];
            }
        }
        return result;
    }

    public boolean[][] rotateCW(int thewidth,int theheight,boolean[][] thegrid) {
        boolean[][] result = new boolean[theheight][thewidth];
        for (int sy = 0,dx=theheight-1; sy < theheight ; ++sy,--dx) {
            for (int sx = 0,dy=0 ; sx < thewidth ; ++sx,++dy) {
                //System.out.println("sx sy dx dy " + sx + " " + sy + " " + dx + " " + dy);
                result[dx][dy] = thegrid[sx][sy];
            }
        }
        return result;
    }





    @Override public String toString() { return stringOfGrid(width,height,grid,'/'); }

}
