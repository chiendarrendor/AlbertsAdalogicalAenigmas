package grid.assistant;

import grid.spring.ClickInfo;

//  modify board cur, edge (x,y,d) when it is clicked on, and return the antimove object to the change
// if orig already has this edge set, this code should make no change to cur and return null.
public interface EdgeClicker<T> {
    MovePair<T> click(T orig, T cur,ClickInfo clickInfo);
}
