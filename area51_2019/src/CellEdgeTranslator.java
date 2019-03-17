import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;

public class CellEdgeTranslator {
    public static String vertexName(Point p) {
        return "" + p.x + "/" + p.y;
    }

    public static String vertexName(int x,int y, Direction d) {
        return vertexName(cellToVertex(x,y,d));
    }

    public static Point cellToVertex(int x,int y,Direction d) {
        switch(d) {
            case NORTHWEST: return new Point(x,y);
            case NORTHEAST: return new Point(x+1,y);
            case SOUTHWEST: return new Point(x,y+1);
            case SOUTHEAST: return new Point(x+1,y+1);
            default: throw new RuntimeException("vertex is a cell corner");
        }
    }

    public static Point nameToVertex(String name) {
        String[] parts = name.split("/");
        return new Point(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
    }


    public static String edgeName(EdgeContainer.EdgeCoord ec) {
        return "" + ec.x + "/" + ec.y + "/" + ec.isV;
    }

    public static String edgeName(int x,int y,Direction d) {
        return edgeName(cellToEdge(x,y,d));
    }

    public static EdgeContainer.EdgeCoord cellToEdge(int x,int y,Direction d) {
        return EdgeContainer.getEdgeCoord(x,y,d);
    }

    public static EdgeContainer.EdgeCoord nameToEdge(String name) {
        String[] parts = name.split("/");
        return new EdgeContainer.EdgeCoord(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Boolean.getBoolean(parts[2]));
    }
}
