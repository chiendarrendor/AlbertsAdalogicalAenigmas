import grid.puzzlebits.Direction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chien on 12/11/2017.
 */
public class Ominoes
{
    private static Map<String,String[]> rawominoes = new HashMap<>();
    private static Map<Integer,Map<Character,Set<Omino>>> ominoes = new HashMap<>();
    private static Map<Integer,Map<Character,Set<Omino>>> borderominoes = new HashMap<>();

    static
    {
        rawominoes.put("4I",new String[]{
                "xxxx"
        });
        rawominoes.put("4L",new String[]{
                "xxx",
                "..x"
        });
        rawominoes.put("4O",new String[]{
                "xx",
                "xx"
        });
        rawominoes.put("4T",new String[]{
                "xxx",
                ".x."
        });
        rawominoes.put("4Z",new String[]{
                ".xx",
                "xx."
        });
        rawominoes.put("5F",new String[]{
                ".xx",
                "xx.",
                ".x."
        });

        rawominoes.put("5I",new String[]{
                "xxxxx"
        });
        rawominoes.put("5N",new String[]{
                ".xxx",
                "xx.."
        });
        rawominoes.put("5L",new String[]{
                "xxxx",
                "...x"
        });
        rawominoes.put("5P",new String[]{
                "xx",
                "xx",
                "x."
        });
        rawominoes.put("5T",new String[]{
                "xxx",
                ".x.",
                ".x."
        });
        rawominoes.put("5W",new String[]{
                "xx.",
                ".xx",
                "..x"
        });
        rawominoes.put("5X",new String[]{
                ".x.",
                "xxx",
                ".x."
        });
        rawominoes.put("5U",new String[]{
                "x.x",
                "xxx"
        });
        rawominoes.put("5V",new String[]{
                "x..",
                "x..",
                "xxx"
        });
        rawominoes.put("5Y",new String[]{
                "xxxx",
                "..x."
        });
        rawominoes.put("5Z",new String[]{
                ".xx",
                ".x.",
                "xx."
        });

        processRaw();
        makeBorders();


    }

    private static Omino expandOmino(Omino base)
    {
        int w = base.getWidth() + 2;
        int h = base.getHeight() + 2;
        MyOmino result = new MyOmino(base.getSize(),base.getShapeId(),base.getRotId(),w,h);
        base.forEachCell((x,y,b) -> {
            if (b != CellColor.BLACK) return;
            result.setCell(x+1,y+1);

            for (Direction d : Direction.orthogonals())
            {
                int nx = x + d.DX();
                int ny = y + d.DY();

                if (base.isIn(nx,ny) && base.isSet(nx,ny)) continue;
                result.offCell(nx+1,ny+1);
            }

        });
        return result;
    }


    private static Omino makeBaseOmino(String[] base, int size, char shape, int rot)
    {
        int w = base[0].length();
        int h = base.length;
        MyOmino result = new MyOmino(size,shape,rot,w,h);
        for (int y = 0 ; y < h ; ++y)
        {
            String curs = base[y];
            for (int x = 0 ; x < w; ++x)
            {
                char c = curs.charAt(x);
                if (c == 'x') result.setCell(x,y);
            }
        }
        return result;
    }

    private static Omino makeRotatedOmino(Omino base,int rot)
    {
        MyOmino result = new MyOmino(base.getSize(),base.getShapeId(),rot,base.getHeight(),base.getWidth());
        for(int bx = 0,ry = 0 ; bx < base.getWidth() ; ++bx,++ry)
        {
            for (int by = 0,rx = base.getHeight()-1 ; by < base.getHeight() ; ++by,--rx)
            {
                if (base.isSet(bx,by)) result.setCell(rx,ry);
            }
        }
        return result;
    }

    private static Omino makeMirrorOmino(Omino base, int rot)
    {
        MyOmino result = new MyOmino(base.getSize(),base.getShapeId(),rot,base.getWidth(),base.getHeight());
        for (int bx = 0, rx = base.getWidth()-1 ; bx < base.getWidth() ; ++bx,--rx)
        {
            for (int by = 0,ry = 0;by < base.getHeight() ; ++by,++ry)
            {
                if (base.isSet(bx,by)) result.setCell(rx,ry);
            }
        }
        return result;
    }






    private static void processRaw()
    {
        for (Map.Entry<String,String[]> ent : rawominoes.entrySet())
        {
            int size = Integer.parseInt(ent.getKey().substring(0,1));
            char shape = ent.getKey().charAt(1);
            Set<Omino> rotset = new HashSet<Omino>();

            Omino r0 = makeBaseOmino(ent.getValue(),size,shape,rotset.size());
            rotset.add(r0);
            Omino m0 = makeMirrorOmino(r0,rotset.size());
            if (!rotset.contains(m0)) rotset.add(m0);

            Omino r1 = makeRotatedOmino(r0,rotset.size());
            if (!rotset.contains(r1)) rotset.add(r1);
            Omino m1 = makeMirrorOmino(r1,rotset.size());
            if (!rotset.contains(m1)) rotset.add(m1);

            Omino r2 = makeRotatedOmino(r1,rotset.size());
            if (!rotset.contains(r2)) rotset.add(r2);
            Omino m2 = makeMirrorOmino(r2,rotset.size());
            if (!rotset.contains(m2)) rotset.add(m2);

            Omino r3 = makeRotatedOmino(r2,rotset.size());
            if (!rotset.contains(r3)) rotset.add(r3);
            Omino m3 = makeMirrorOmino(r3,rotset.size());
            if (!rotset.contains(m3)) rotset.add(m3);

            if (!ominoes.containsKey(size)) ominoes.put(size,new HashMap<>());
            ominoes.get(size).put(shape,rotset);
        }
    }

    private static void makeBorders()
    {
        for (int size : ominoes.keySet())
        {
            borderominoes.put(size,new HashMap<Character,Set<Omino>>());
            for (char type : ominoes.get(size).keySet())
            {
                borderominoes.get(size).put(type,new HashSet<Omino>());
                for (Omino om : ominoes.get(size).get(type))
                {
                    borderominoes.get(size).get(type).add(expandOmino(om));
                }
            }
        }
    }



    public static Set<Integer> getSizes()
    {
        return ominoes.keySet();
    }
    public static Set<Character> getShapesForSize(int size)
    {
        return ominoes.get(size).keySet();
    }
    public static Set<Omino> getOminoSet(int size,char type)
    {
        return ominoes.get(size).get(type);
    }
    public static Set<Omino> getBorderOminoSet(int size,char type) { return borderominoes.get(size).get(type);}



    private static class MyOmino implements Omino
    {
        private int size;
        private char shape;
        private int rotid;
        private int width;
        private int height;
        private CellColor grid[][];
        String uid = null;

        public boolean equals(Object o)
        {
            if (!(o instanceof MyOmino)) return false;
            MyOmino mo = (MyOmino)o;
            return getUID().equals(mo.getUID());
        }

        public int hashCode() { return getUID().hashCode(); }


        public MyOmino(int size,char shape,int rot,int width,int height)
        {
            this.size = size;
            this.shape = shape;
            this.rotid = rot;
            this.width = width;
            this.height = height;
            this.grid = new CellColor[width][height];

            forEachCell((x,y,b) -> grid[x][y] = CellColor.UNKNOWN);
        }

        public void setCell(int x,int y) { grid[x][y] = CellColor.BLACK; uid = null; }
        public void offCell(int x,int y) { grid[x][y] = CellColor.WHITE; uid = null; }

        public int getSize() { return size; }
        public char getShapeId() { return shape; }
        public int getRotId() { return rotid; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public boolean isSet(int x,int y) { return grid[x][y] == CellColor.BLACK; }
        public boolean isBlank(int x,int y) { return grid[x][y] == CellColor.WHITE; }
        public CellColor getCellColor(int x,int y) { return grid[x][y]; }
        public boolean isIn(int x,int y) { return x >= 0 && x < getWidth() && y >= 0 && y < getHeight(); }

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append(getSize()).append(getShapeId()).append(getRotId()).append("\n");
            forEachCell((x,y,b)->{
                sb.append(b.getChar());
                if (x+1 == getWidth()) sb.append("\n");
            });
            return sb.toString();
        }

        public String getUID()
        {
            if (uid == null)
            {
                StringBuffer sb = new StringBuffer();
                forEachCell((x,y,b) -> {
                    sb.append(b.getChar());
                    if (x+1 == getWidth()) sb.append("/");
                });
                uid = sb.toString();
            }
            return uid;
        }



        public void forEachCell(OminoLambda ol) { transformForEachCell(0,0,0,0,ol);}

        public void transformForEachCell(int ox, int oy, int cx, int cy, OminoLambda ol)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                int ty = cy - oy + y;
                for (int x = 0 ; x < getWidth() ; ++x)
                {
                    int tx = cx - ox + x;
                    ol.operation(tx,ty,grid[x][y]);
                }
            }
        }
    }


}
