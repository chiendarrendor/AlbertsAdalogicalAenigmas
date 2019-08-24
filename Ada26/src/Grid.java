import java.awt.Point;

public interface Grid {
    int getWidth();
    int getHeight();
    boolean inBounds(int x,int y);
    boolean inBounds(Point p);
    void addToGrid(int x,int y,RectangleRegion rr);
    void removeFromGrid(int x,int y,RectangleRegion rr);
}
