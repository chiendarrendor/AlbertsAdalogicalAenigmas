/**
 * Created by chien on 12/12/2017.
 */
public interface Omino
{
    int getSize();
    char getShapeId();
    int getRotId();
    int getWidth();
    int getHeight();
    String getUID();

    boolean isSet(int x,int y);
    boolean isBlank(int x,int y);
    CellColor getCellColor(int x,int y);
    boolean isIn(int x,int y); // returns true if x,y is on the Omino

    // will call the given omino lambda for each cell on the Omino, where
    // the upper left corner is 0,0
    void forEachCell(OminoLambda ol);

    // given ox and oy on the grid of this omino, will iterate over
    // all cells on the Omino, but return the x and y coordinate of that Omino cell
    // transformed onto the grid such that Omino cell ox,oy has coordinate cx,cy
    void transformForEachCell(int ox,int oy,int cx,int cy, OminoLambda ol);
}
