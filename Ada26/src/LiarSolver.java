import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class LiarSolver implements ReferenceHelper {
    String solution = null;
    CellContainer<Boolean> cluecells;

    public LiarSolver(Board b) {
        cluecells = new CellContainer<Boolean>(b.getWidth(),b.getHeight(),(x,y)->false);
        b.forEachCell((x,y)->{
            RectangleRegionHandler rrh = b.getHandler(x,y);
            if (rrh == null) return;
            if (rrh.realness != Realness.FAKE) return;

            for (Direction d : Direction.orthogonals()) {
                Point p = d.delta(x,y,1);
                if (!b.inBounds(p)) continue;
                if (!b.hasLetter(p.x,p.y)) continue;
                cluecells.setCell(p.x,p.y,true);
            }
        });

        StringBuffer sb = new StringBuffer();
        b.forEachCell((x,y)->{
            if (!cluecells.getCell(x,y)) return;
            RectangleRegion rr = b.cells.getCell(x,y).iterator().next();
            sb.append(LetterRotate.Rotate(b.getLetter(x,y),rr.dimensions.x*rr.dimensions.y));
        });
        solution = sb.toString();
    }

    public String getResult() { return solution; }

    private static final int INSET=8;
    @Override public void addToDrawing(BufferedImage bi, int x, int y) {
        if (cluecells.getCell(x,y)) {
            Graphics2D g = (Graphics2D) bi.getGraphics();
            g.setColor(Color.BLUE);
            g.setStroke(new BasicStroke(5.0f));
            g.drawOval(INSET, INSET, bi.getWidth() - 2 * INSET, bi.getHeight() - 2 * INSET);
        }
    }
}
