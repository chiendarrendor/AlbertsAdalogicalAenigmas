package grid.assistant;

//  modify board cur, cell (x,y) when it is clicked on, and return the antimove object for the move made.
// if orig already has this cell set, this code should make no change to cur and return null.
public interface CellClicker<T> {
    MovePair<T> click(T orig, T cur, int x, int y);
}
