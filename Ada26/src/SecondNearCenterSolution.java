import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class SecondNearCenterSolution implements ReferenceHelper {
    String solution = null;
    CellContainer<Boolean> solutioncells;

    public SecondNearCenterSolution(NearCenterSolution ncs, Board b) {
        solutioncells = new CellContainer<Boolean>(b.getWidth(),b.getHeight(),(x,y)->false);

        b.forEachCell((x,y)-> {
            RectangleRegionHandler rrh = b.getHandler(x, y);
            if (rrh == null) return;

            RectangleRegion rr = rrh.currentRectangles.iterator().next();
            if (ncs.isUsed(rr)) return;
            rr.realPoints.stream().filter(p->b.hasLetter(p.x,p.y)).forEach(p->solutioncells.setCell(p.x,p.y,true));
        });

        StringBuffer sb = new StringBuffer();
        b.forEachCell((x,y)->{
            if (!solutioncells.getCell(x,y)) return;
            RectangleRegion rr = b.cells.getCell(x,y).iterator().next();
            sb.append(LetterRotate.Rotate(b.getLetter(x,y),rr.dimensions.x*rr.dimensions.y));
        });
        solution = sb.toString();

    }

    public String getResult() { return solution; }

    private static final int INSET = 8;
    @Override public void addToDrawing(BufferedImage bi, int x, int y) {
        if (solutioncells.getCell(x,y)) {
            Graphics2D g = (Graphics2D) bi.getGraphics();
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(5.0f));
            g.drawOval(INSET, INSET, bi.getWidth() - 2 * INSET, bi.getHeight() - 2 * INSET);
        }

    }
}
