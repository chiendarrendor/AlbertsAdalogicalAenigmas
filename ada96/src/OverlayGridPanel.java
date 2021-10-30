import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

public class OverlayGridPanel extends GridPanel {

    static public class CellPair {
        int x1;
        int y1;
        int x2;
        int y2;
        Color c;
        public CellPair(int x1,int y1,int x2,int y2, Color c) { this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.c = c; }
    }

    public interface CellPairGenerator {
        List<CellPair> get();
    }

    CellPairGenerator cpg;

    public OverlayGridPanel(int width, int height,GridListener listener, CellPairGenerator cpg) { super(width,height,listener); this.cpg = cpg; }

    private Point getCenterOfCell(int x, int y) {
        DrawParams params = getParams();

        return new Point(params.INSET + x * params.cellWidth+1 + params.cellWidth/2,params.INSET + y * params.cellHeight+1 + params.cellHeight/2);
    }

    public void paint(Graphics g) {
        super.paint(g);
        DrawParams dp = super.getParams();

        for (CellPair cp : cpg.get()) {
            g.setColor(cp.c);
            Point p1 = getCenterOfCell(cp.x1,cp.y1);
            Point p2 = getCenterOfCell(cp.x2,cp.y2);
            g.drawLine(p1.x,p1.y,p2.x,p2.y);
        }

    }

}
