import java.util.*;

/**
 * Created by chien on 12/16/2017.
 */

// this class, as a subordinate class of Board, maintains the set of all
// possible omino positions and how many ominos of that type there are,
// and the locations of known ominoes

public class OminoBoard
{
    Board b;
    int size;
    int delcount = 0;

    public void clearDelCount() { delcount = 0; }
    public int getDelCount() { return delcount; }

    public int solvedPlaceSize()
    {
        int result = 0;

        for (OminoPlaceSet ops : ominosets.values())
        {
            result += ops.donePlaces.size();
        }
        return result;
    }


    public class OminoPlaceSet
    {
        public class OminoPlace
        {
            // this is the coordinate, in board space, of the 1,1 cell in omino space
            int x;
            int y;
            Omino omino;
            public OminoPlace(Omino omino, int x,int y,boolean isDone)
            {
                this.omino = omino;
                this.x = x;
                this.y = y;

                if (isDone) addToDone();
                else addToBoard();
            }

            // will only pass cells that are black or white on the omino, and
            // on the main board.  Will throw an exception if there is an off-board black
            public void forEachCell(OminoLambda ol)
            {
                omino.transformForEachCell(1,1,x,y,(x,y,c) -> {
                    if (!b.onBoard(x,y))
                    {
                        if (c == CellColor.BLACK) throw new RuntimeException("Off board black?");
                        return;
                    }
                    if (c == CellColor.UNKNOWN) return;
                    ol.operation(x,y,c);
                });
            }

            public void addToBoard()
            {
                forEachCell((x,y,c) -> {
                    if (c == CellColor.BLACK) cells[x][y].onplaces.add(this);
                    else cells[x][y].offplaces.add(this);
                });
                ominoes.add(this);
            }

            public void addToDone()
            {
                forEachCell((x,y,c) -> {
                    if (c == CellColor.BLACK)
                    {
                        cells[x][y].thePlace = this;
                    }
                });
                donePlaces.add(this);
            }

            public void removeFromBoard()
            {
                ++delcount;
                forEachCell((x,y,c) -> {
                    if (c == CellColor.BLACK) cells[x][y].onplaces.remove(this);
                    else cells[x][y].offplaces.remove(this);
                });
                ominoes.remove(this);
            }

            public void placeDown()
            {
                forEachCell((x,y,c) -> {
                    safeIterateOminoPlaceSet(cells[x][y].onplaces,a->a.removeFromBoard());
                    if (c == CellColor.BLACK)
                    {
                        cells[x][y].thePlace = this;
                    }
                    cells[x][y].requiredColor = c;
                });
                donePlaces.add(this);
                --count;
            }

        }

        char id;
        int count;
        Set<OminoPlace> ominoes = new HashSet<>();
        Vector<OminoPlaceSet.OminoPlace> donePlaces = new Vector<>();




        public OminoPlaceSet(char id)
        {
            this.id = id; count = 1;
            for (Omino o : Ominoes.getBorderOminoSet(size,id))
            {
                int maxx = b.getWidth() - o.getWidth() + 2;
                int maxy = b.getHeight() - o.getHeight() + 2;
                b.forEachCell((x,y)->{
                    if (x > maxx) return;
                    if (y > maxy) return;
                    new OminoPlace(o,x,y,false);
                });
            }

        }

        public OminoPlaceSet(OminoPlaceSet right)
        {
            this.id = right.id;
            this.count = right.count;
            for (OminoPlace op : right.ominoes) new OminoPlace(op.omino,op.x,op.y,false);
            for (OminoPlace op : right.donePlaces) new OminoPlace(op.omino,op.x,op.y,true);
        }

        public void incrementCount() { ++count; }

    }

    public class OminoCell
    {
        OminoPlaceSet.OminoPlace thePlace = null;
        Set<OminoPlaceSet.OminoPlace> onplaces = new HashSet<>();
        Set<OminoPlaceSet.OminoPlace> offplaces = new HashSet<>();
        CellColor requiredColor = CellColor.UNKNOWN;
    }


    OminoCell[][] cells;
    Map<Character, OminoPlaceSet> ominosets = new HashMap<>();

    public OminoBoard(Board b,int size,String ominoids)
    {
        this.b = b;
        this.size = size;

        cells = new OminoCell[b.getWidth()][b.getHeight()];
        b.forEachCell((x,y) -> {
            cells[x][y] = new OminoCell();
            cells[x][y].requiredColor = b.getOrigCellColor(x,y);
        });

        for(char c : ominoids.toCharArray())
        {
            if(ominosets.containsKey(c)) ominosets.get(c).incrementCount();
            else ominosets.put(c,new OminoPlaceSet(c));
        }
    }

    public OminoBoard(Board b,OminoBoard right)
    {
        this.b = b;
        this.size = right.size;

        cells = new OminoCell[b.getWidth()][b.getHeight()];
        b.forEachCell((x,y) -> {
            cells[x][y] = new OminoCell();
            cells[x][y].requiredColor = right.cells[x][y].requiredColor;
        });

        for (OminoPlaceSet ops : right.ominosets.values())
        {
            OminoPlaceSet newops = new OminoPlaceSet(ops);
            ominosets.put(newops.id,newops);
        }

    }

    public void setCellColor(int x,int y, CellColor cc) { cells[x][y].requiredColor = cc; }
    public CellColor getCellColor(int x,int y) { return cells[x][y].requiredColor; }


    public interface SIOPS { void op(OminoPlaceSet.OminoPlace place); }

    static void safeIterateOminoPlaceSet(Set<OminoPlaceSet.OminoPlace> oms,SIOPS op)
    {
        OminoBoard.OminoPlaceSet.OminoPlace[] lops = new OminoBoard.OminoPlaceSet.OminoPlace[0];
        lops = oms.toArray(lops);
        for(OminoPlaceSet.OminoPlace place : lops) op.op(place);
    }

}
