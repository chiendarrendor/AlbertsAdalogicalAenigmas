import grid.lambda.CellLambda;
import grid.puzzlebits.Direction;

/**
 * Created by chien on 10/22/2017.
 */
public class EdgeSet
{
    int width;
    int height;
    EdgeType[][] verts;
    EdgeType[][] horiz;
    int unknowncounter;

    public EdgeSet(int width, int height)
    {
        this.width = width;
        this.height = height;
        verts = new EdgeType[width-1][height];
        horiz = new EdgeType[width][height-1];
        unknowncounter = (width-1)*height + width*(height-1);

        CellLambda.forEachCell(width,height,(x,y)->{
            if(x < width - 1) verts[x][y] = EdgeType.UNKNOWN;
            if(y < height-1) horiz[x][y] = EdgeType.UNKNOWN;
        });
    }

    public EdgeSet(EdgeSet right)
    {
        width = right.width;
        height = right.height;
        unknowncounter = right.unknowncounter;
        verts = new EdgeType[width-1][height];
        horiz = new EdgeType[width][height-1];

        CellLambda.forEachCell(width,height,(x,y)->{
            if(x < width - 1) verts[x][y] = right.verts[x][y];
            if(y < height-1) horiz[x][y] = right.horiz[x][y];
        });
    }

    // directions off the board must always be WALL type.
    public EdgeType getEdge(int x, int y, Direction d)
    {
        switch(d)
        {
            case NORTH:
                if(y == 0) return EdgeType.WALL;
                return horiz[x][y-1];
            case SOUTH:
                if(y == height-1) return EdgeType.WALL;
                return horiz[x][y];
            case EAST:
                if(x == width-1) return EdgeType.WALL;
                return verts[x][y];
            case WEST:
                if(x == 0) return EdgeType.WALL;
                return verts[x-1][y];
        }
        throw new RuntimeException("bad direction!");
    }
    public void setEdge(int x, int y, Direction d, EdgeType type)
    {
        switch(d)
        {
            case NORTH:
                if(y == 0) throw new RuntimeException("trying to set a board edge!");
                horiz[x][y-1] = type;
                break;
            case SOUTH:
                if(y == height-1) throw new RuntimeException("trying to set a board edge!");
                horiz[x][y] = type;
                break;
            case EAST:
                if(x == width-1) throw new RuntimeException("trying to set a board edge!");
                verts[x][y] = type;
                break;
            case WEST:
                if(x == 0) throw new RuntimeException("trying to set a board edge!");
                verts[x-1][y] = type;
                break;
            default:
                throw new RuntimeException("bad direction!");
        }
        --unknowncounter;
    }

    public boolean isFilled() { return unknowncounter == 0; }

}
