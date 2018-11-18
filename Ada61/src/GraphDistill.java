import grid.puzzlebits.CellContainer;
import grid.puzzlebits.EdgeContainer;

public class GraphDistill {
    int width;
    int height;
    EdgeContainer<EdgeState> edges;
    CellContainer<LetterContainer> letters;

    public GraphDistill(RegionGraph rg) {
        width = rg.width;
        height = rg.height;
        edges = new EdgeContainer<EdgeState>(rg.width,rg.height,EdgeState.CLOSED,
                (x,y,isV)->EdgeState.OPEN,
                (x,y,isV,r)->r
        );

        letters = new CellContainer<LetterContainer>(rg.width,rg.height,(x,y)->null);

        for (Edge e : rg.edges) {
            for (EdgeContainer.CellCoord cc : e.edges) {
                edges.setEdge(cc.x,cc.y,cc.d,e.getState());
            }
        }

        for (Region r: rg.regions.values()) {
            LetterContainer lc = r.getLetterContainer();
            letters.setCell(lc.x,lc.y,lc);
        }
    }
}
