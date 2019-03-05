package grid.graph;

import com.sun.org.apache.regexp.internal.RE;
import grid.puzzlebits.CellContainer;

import java.awt.Color;
import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GridGraphArticulationTest {
    private static class MyReference implements GridGraph.GridReference {
        CellContainer<Boolean> cc;
        public MyReference(CellContainer<Boolean> cc) {this.cc = cc; }
        @Override public int getWidth() { return cc.getWidth(); }
        @Override public int getHeight() { return cc.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return cc.getCell(x,y); }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }



    public static void main(String[] args) {
        CellContainer<Boolean> ccon = new CellContainer<Boolean>(6,6,(x,y)->false);
        ccon.setCell(0,0,true);
        ccon.setCell(0,1,true);
        ccon.setCell(1,0,true);
        ccon.setCell(2,0,true);
        ccon.setCell(0,2,true);
        ccon.setCell(0,3,true);
        ccon.setCell(3,0,true);
        ccon.setCell(1,3,true);
        ccon.setCell(2,3,true);
        ccon.setCell(3,3,true);
        ccon.setCell(1,4,true);
        ccon.setCell(2,4,true);
        ccon.setCell(4,3,true);
        ccon.setCell(4,2,true);
        ccon.setCell(5,3,true);
        ccon.setCell(4,4,true);

        Color[] cols = new Color[] { Color.ORANGE,Color.GREEN,Color.BLUE,Color.PINK };


        GridGraph gg = new GridGraph(new MyReference(ccon));
        GGFrame ggf = new GGFrame();
        ggf.addGridGraph(gg,"Base GG");

        for (Point p : gg.getArticulationPoints()) {
            List<Set<Point>> parts = gg.getArticulationSet(p);
            Set<Point> arp = new HashSet<Point>();
            arp.add(p);

            GGFrame.GraphBlock gb = ggf.addGridGraph(gg,"Articulation of " + p).addSpecial(Color.RED,arp);
            for (int i = 0 ; i < parts.size() ; ++i) {
                gb.addSpecial(cols[i],parts.get(i));
            }

        }


        ggf.go();
    }
}
