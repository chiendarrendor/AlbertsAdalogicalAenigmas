// This potentially generic class will create all the ominos of a particular size,
// organize them into groups by rotational symmetry, and pair those groups that are
// mirror images of each other.

import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OminoSet {
    Map<String,Omino> ominoes = new HashMap<>();
    Map<Character,OminoFamily> families = new HashMap<>();
    Map<Character,OminoFamily> mirrorfamilies = new HashMap<>();

    private char nextfamily = 'A';
    public class OminoFamily {
        char familyid;
        OminoFamily mirrorfamily = null;
        List<Omino> children = new ArrayList<>();

        public OminoFamily() { familyid = nextfamily++; }
    }

    public class Omino {
        int width;
        int height;
        boolean[][] cells;
        OminoFamily myfamily = null;
        OminoFamily mirrorfamily = null;


        public Omino(int width,int height) {
            this.width = width;
            this.height = height;
            cells = new boolean[width][height];
        }

        private String ck = null;
        public String toString() {
            if (ck == null) {
                StringBuffer sb = new StringBuffer();
                Point tscenter = getCenter();
                for (int y = 0; y < height; ++y) {
                    for (int x = 0; x < width; ++x) {
                        sb.append(cells[x][y] ? ((x == tscenter.x && y == tscenter.y) ? '#' : '@')  : '.');
                    }
                    sb.append("/");
                }
                ck = sb.toString();
            }
            return ck;
        }

        private Point calcCenter() {
            for (int y = 0 ; y < height ; ++y ) {
                for (int x = 0 ; x < width ; ++x) {
                    if (cells[x][y]) return new Point(x,y);
                }
            }
            throw new RuntimeException("No on cells?");
        }



        private Point center = null;
        public Point getCenter() {
            if (center == null) {
                center = calcCenter();
            }
            return center;
        }

        private Set<Point> pointsOnCenter = null;
        public Set<Point> getPointsOnCenter() {
            if (pointsOnCenter == null) {
                Point center = getCenter();
                pointsOnCenter = new HashSet<>();

                for (int y = 0 ; y < height ; ++y) {
                    for (int x = 0 ; x < width ; ++x) {
                        if (!cells[x][y]) continue;
                        pointsOnCenter.add(new Point(x-center.x,y-center.y));
                    }
                }
            }
            return pointsOnCenter;
        }




        public Omino(Set<Point> ons) {
            int minx = 0;
            int maxx = 0;
            int miny = 0;
            int maxy = 0;

            boolean first = true;

            for (Point p : ons) {
                if (first) {
                    first = false;
                    minx = p.x;
                    maxx = p.x;
                    miny = p.y;
                    maxy = p.y;
                    continue;
                }
                if (p.x > maxx) maxx = p.x;
                if (p.x < minx) minx = p.x;
                if (p.y > maxy) maxy = p.y;
                if (p.y < miny) miny = p.y;
            }

            width = maxx - minx + 1;
            height = maxy - miny + 1;
            cells = new boolean[width][height];
            for (int x = 0 ; x < width ; ++x) {
                for (int y = 0 ; y < height ; ++y) {
                    cells[x][y] = false;
                }
            }

            for (Point p : ons) {
                cells [p.x - minx][p.y-miny] = true;
            }


        }

        Omino clockwise() {
            Omino result = new Omino(height,width);

            for (int x = 0 ; x < width ; ++x) {
                for (int y = 0 ; y < height ; ++y) {
                    result.cells[height-y-1][x] = cells[x][y];
                }
            }
            return result;
        }

        Omino mirror() {
            Omino result = new Omino(width,height);
            for (int x = 0 ; x < width ; ++x ) {
                for (int y = 0 ; y < height ; ++y ) {
                    result.cells[width-x-1][y] = cells[x][y];
                }
            }
            return result;
        }




    }


    public OminoSet(int size) {
        List<Set<Point>> queue = new ArrayList<>();

        Set<Point> start = new HashSet<>();
        start.add(new Point(0,0));
        queue.add(start);

        while(queue.size() > 0) {
            Set<Point> cur = queue.remove(0);
            if (cur.size() == size) {
                Omino newo = new Omino(cur);
                if (!ominoes.containsKey(newo.toString())) {
                    ominoes.put(newo.toString(),newo);
                }
                continue;
            }

            for (Point p : cur) {
                for (Direction d : Direction.orthogonals()) {
                    Point np = d.delta(p,1);
                    if (cur.contains(np)) continue;
                    Set<Point> newset = new HashSet<>();
                    newset.addAll(cur);
                    newset.add(np);
                    queue.add(newset);
                }
            }
        }

        for (Omino o : ominoes.values()) {
            if (o.myfamily != null) continue;

            OminoFamily of = new OminoFamily();
            families.put(of.familyid,of);
            of.children.add(o);
            o.myfamily = of;

            Omino no = o;
            while(true) {
                no = ominoes.get(no.clockwise().toString());
                if (no.myfamily != null) break;
                no.myfamily = of;
                of.children.add(no);
            }
        }


        for (OminoFamily of : families.values()) {
            if (of.mirrorfamily != null) continue;

            String mirror = of.children.get(0).mirror().toString();
            OminoFamily my_mirror = ominoes.get(mirror).myfamily;

            OminoFamily mf = new OminoFamily();
            mirrorfamilies.put(mf.familyid,mf);
            of.mirrorfamily = mf;
            mf.children.addAll(of.children);

            if (my_mirror != of) {
                my_mirror.mirrorfamily = mf;
                mf.children.addAll(my_mirror.children);
            }

            mf.children.stream().forEach(c->c.mirrorfamily = mf);


        }





//        for (String s : ominoes.keySet()) {
//            System.out.println(ominoes.get(s).myfamily.familyid + ": " + s);
//        }
//
//        for (char fid : families.keySet()) {
//            OminoFamily of = families.get(fid);
//            System.out.print(fid + ":");
//            for (Omino o : of.children) {
//                System.out.print(" " + o.toString());
//            }
//            System.out.println("");
//            System.out.println("Mirror: " + of.mirrorfamily.familyid);
//        }
    }


    // given a set of points any distance from origin
    // return the family that has that same shape
    public OminoFamily getFamilyMatchingPoints(Set<Point> points) {
        Omino testOmino = new Omino(points);
        Omino searchOmino = ominoes.get(testOmino.toString());
        if (searchOmino == null) return null;
        return searchOmino.myfamily;
    }

    // given a set of points any distance from origin
    // return the family that has that same shape
    public OminoFamily getMirrorFamilyMatchingPoints(Set<Point> points) {
        Omino testOmino = new Omino(points);
        Omino searchOmino = ominoes.get(testOmino.toString());
        if (searchOmino == null) return null;
        return searchOmino.mirrorfamily;
    }
}
