import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class NearCenterSolution implements ReferenceHelper {
    private String result = null;
    private Set<RectangleRegion> used = new HashSet<>();
    CellContainer<Boolean> centers;

    public String getResult() { return result; }
    public boolean isCenter(int x,int y) { return centers.getCell(x,y); }
    public boolean isUsed(RectangleRegion rr) { return used.contains(rr); }

    private static Set<Integer> getCentersFor(int extent,int relative) {
        Set<Integer> result = new HashSet<>();
        if (extent % 2 == 1) {
            result.add(extent/2 + relative);
        } else {
            result.add(extent/2 + relative);
            result.add(extent/2-1 + relative);
        }
        return result;
    }


    public NearCenterSolution(Board b) {
        centers = new CellContainer<Boolean>(b.getWidth(),b.getHeight(),(x,y)->false);

        b.forEachCell((x,y)->{
            RectangleRegionHandler rrh = b.getHandler(x,y);
            if (rrh == null) return;

            RectangleRegion rr = rrh.currentRectangles.iterator().next();
            Set<Integer> widths = getCentersFor(rr.dimensions.x,rr.upperleft.x);
            Set<Integer> heights = getCentersFor(rr.dimensions.y,rr.upperleft.y);

            for (Point p : rr.realPoints) {
                if (widths.contains(p.x) && heights.contains(p.y) && b.hasLetter(p.x,p.y)) {
                    centers.setCell(p.x,p.y,true);
                    used.add(rr);
                }
            }
        });

        StringBuffer sb = new StringBuffer();
        b.forEachCell((x,y)-> {
            if (!centers.getCell(x,y)) return;
            RectangleRegion rr = b.cells.getCell(x,y).iterator().next();
            sb.append(LetterRotate.Rotate(b.getLetter(x,y),rr.dimensions.x*rr.dimensions.y));
        });

        result = sb.toString();

    }

    private static final int INSET = 8;
    @Override public void addToDrawing(BufferedImage bi, int x, int y) {
        if (centers.getCell(x,y)) {
            Graphics2D g = (Graphics2D) bi.getGraphics();
            g.setColor(Color.BLUE);
            g.setStroke(new BasicStroke(5.0f));
            g.drawOval(INSET, INSET, bi.getWidth() - 2 * INSET, bi.getHeight() - 2 * INSET);
        }
    }
}
