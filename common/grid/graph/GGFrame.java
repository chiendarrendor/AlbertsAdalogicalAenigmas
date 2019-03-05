package grid.graph;// this class is designed to display the structure of one or more GridGraph objects

import grid.graph.GridGraph;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GGFrame {
    public static class GraphBlock {
        GridGraph graph;
        String title;

        Map<Point,Color> specials = new HashMap<>();

        public GraphBlock(GridGraph graph, String title) {
            this.graph = graph;
            this.title = title;
        }

        public GraphBlock addSpecial(Color c,Set<Point> specset) {
            specset.stream().forEach(p->specials.put(p,c));
            return this;
        }
    }




    private static class GGFGridListener implements GridPanel.MultiGridListener,GridPanel.EdgeListener {
        List<GraphBlock> graphblocks = new ArrayList<>();
        int curi = 0;
        private GraphBlock curGB() { return graphblocks.get(curi); }
        private GridGraph curGG() { return curGB().graph; }
        private GridGraph.GridReference curGR() { return curGG().getGridReference(); }

        public GGFGridListener() {
        }

        public GridPanel.EdgeListener getEdgeListener() {
            return this;
        }


        public void addGraphBlock(GraphBlock gb) {
            graphblocks.add(gb);
        }

        // GridListener stuff
        @Override public int getNumXCells() { return curGR().getWidth(); }
        @Override public int getNumYCells() { return curGR().getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return new String[] { curGB().title }; }

        private static final int INSET = 10;
        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            Point curp = new Point(cx,cy);
            g.setFont(g.getFont().deriveFont(20.0f));

            if (!curGR().isIncludedCell(cx,cy)) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (curGB().specials.containsKey(curp)) {
                g.setColor(curGB().specials.get(curp));
                g.fillRect(INSET,INSET,bi.getWidth() - 2 * INSET, bi.getHeight() - 2 * INSET);
            }

            if (!curGR().isIncludedCell(cx,cy)) return true;

            List<Set<Point>> consets = curGG().connectedSets();
            g.setColor(Color.BLACK);
            for (int i = 0 ; i < consets.size() ; ++i) {
                if (consets.get(i).contains(curp)) {
                    GridPanel.DrawStringInCell(g, 0, 0, bi.getWidth(), bi.getHeight(), "" + i);
                    break;
                }
            }

            if (consets.size() == 1) {
                Set<Point> arts = curGG().getArticulationPoints();
                if (arts.contains(curp)) {
                    GridPanel.DrawStringInCorner(bi,Color.BLACK, ""+Direction.SOUTHEAST.getSymbol(),Direction.NORTHWEST);
                    GridPanel.DrawStringInCorner(bi,Color.BLACK, ""+Direction.SOUTHWEST.getSymbol(),Direction.NORTHEAST);

                    GridPanel.DrawStringInCorner(bi,Color.BLACK, ""+Direction.NORTHEAST.getSymbol(),Direction.SOUTHWEST);
                    GridPanel.DrawStringInCorner(bi,Color.BLACK, ""+Direction.NORTHWEST.getSymbol(),Direction.SOUTHEAST);
                }
            }

            return true;
        }

        // MultiGridListener Stuff
        @Override public boolean hasNext() { return curi < graphblocks.size() - 1; }
        @Override public void moveToNext() { ++curi; }
        @Override public boolean hasPrev() { return curi > 0; }
        @Override public void moveToPrev() { --curi; }

        // EdgeListener stuff
        private static EdgeDescriptor OPEN = new EdgeDescriptor(Color.BLACK,1);
        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.RED,5);
        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return curGR().edgeExitsEast(x,y) ? OPEN : WALL; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return curGR().edgeExitsSouth(x,y) ? OPEN : WALL; }
    }

    private GGFGridListener gl;
    GridFrame gf;


    public GGFrame() {
        gl = new GGFGridListener();
    }

    public void go() {
        gf = new GridFrame("GridGraph Visualizer",1200,800,gl,gl.getEdgeListener());
    }


    public GraphBlock addGridGraph(GridGraph graph,String title) {
        GraphBlock gb = new GraphBlock(graph,title);
        gl.addGraphBlock(gb);
        return gb;
    }


}
