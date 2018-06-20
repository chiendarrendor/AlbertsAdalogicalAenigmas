import grid.assistant.BoardHolder;
import grid.spring.GridPanel;

import java.awt.Color;

class MyEdgeListener implements GridPanel.EdgeListener {
    BoardHolder<Board> bh;
    private Board b() { return bh.getBoard(); }
    public MyEdgeListener(BoardHolder<Board> bh) { this.bh = bh;}

    @Override
    public EdgeDescriptor onBoundary() { return new EdgeDescriptor(Color.BLACK,5); }
    public EdgeDescriptor toEast(int x, int y) {
        return new EdgeDescriptor(Color.BLACK,b().getRegionId(x,y) == b().getRegionId(x+1,y) ? 1 : 5); }
    public EdgeDescriptor toSouth(int x, int y) {
        return new EdgeDescriptor(Color.BLACK,b().getRegionId(x,y) == b().getRegionId(x,y+1) ? 1 : 5); }
}
