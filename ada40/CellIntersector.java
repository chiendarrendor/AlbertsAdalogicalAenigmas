/**
 * Created by chien on 2/19/2017.
 */

/*
this class, given a base cell, will
a) determine if a new cell can be added
b) determine if there is a singular addition that has been made
c) determine if that addition is a delta
d) apply itself to the board
*/
public class CellIntersector
{
    CellInfo baseCell;
    boolean newCellBroken = false;
    CellInfo newCell = null;
    int changeCount = 0;

    public CellIntersector(CellInfo baseCell)
    {
        this.baseCell = baseCell;
    }

    // UNKNOWN RECTANGLE WALL EMPTY
    // type rectnum x y

    public boolean canMakeRectangle(int rectid)
    {
        if (baseCell.type == CellInfo.WALL) return false;
        if (baseCell.type == CellInfo.RECTANGLE && baseCell.rectnum != rectid) return false;
        return true;
    }

    public void makeRectangle(int rectid)
    {
        setNewCell(CellInfo.RECTANGLE,rectid);
    }

    public boolean canMakeWall()
    {
        if (baseCell.type == CellInfo.EMPTY) return false;
        if (baseCell.type == CellInfo.RECTANGLE) return false;
        return true;
    }

    public void makeWall()
    {
        setNewCell(CellInfo.WALL,-1);
    }

    private void setNewCell(int type,int rectid)
    {
        ++changeCount;
        if (newCellBroken) return;

        if (newCell == null)
        {
            newCell = new CellInfo(type,rectid,baseCell.x,baseCell.y);
        }
        else
        {
            if (type == newCell.type && rectid == newCell.rectnum) return;
            newCell = null;
            newCellBroken = true;
        }
    }

    public boolean changesBoard(int numRectangles)
    {
        if (numRectangles != changeCount) return false;
        if (newCell == null) return false;
        if (baseCell.type == newCell.type) return false;
        return true;
    }

    public void changeBoard()
    {
        baseCell.type = newCell.type;
        baseCell.rectnum = newCell.rectnum;
    }


}
