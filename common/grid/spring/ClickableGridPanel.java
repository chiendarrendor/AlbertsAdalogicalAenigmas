package grid.spring;

import grid.puzzlebits.Direction;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class ClickableGridPanel extends GridPanel implements MouseListener {

    public interface EdgeClicked {
        void clicked(ClickInfo ci);
    }

    public interface CellClicked {
        void clicked(ClickInfo ci);
    }

    List<EdgeClicked> edgeclickers = new ArrayList<>();
    List<CellClicked> cellclickers = new ArrayList<>();

    public ClickableGridPanel(int width, int height, GridListener listener, EdgeListener edgeListener) {
        super(width, height, listener, edgeListener);
        addMouseListener(this);
    }

    public ClickableGridPanel(int width,int height,GridListener listener) {
        this(width,height,listener,null);
    }

    public void addEdgeClicker(EdgeClicked ec) { edgeclickers.add(ec); }
    public void addCellClicker(CellClicked cc) { cellclickers.add(cc); }




    private final static int BORDER = 7;
    public final static int OUTOFBOUNDS = -1;
    public final static int INCORNER = -2;
    @Override
    public void mouseClicked(MouseEvent e) {
        DrawParams dp = getParams();

        int cellx = (e.getX()-dp.INSET)/dp.cellWidth;
        int celly = (e.getY()-dp.INSET)/dp.cellHeight;
        int relx = e.getX() - (dp.INSET + cellx * dp.cellWidth);
        int rely = e.getY() - (dp.INSET + celly * dp.cellHeight);

        Direction d = null;
        if (e.getX() < dp.INSET || e.getY() < dp.INSET || cellx >= dp.numXCells || celly >= dp.numYCells) {
            cellx = OUTOFBOUNDS;
            celly = OUTOFBOUNDS;
        } else {
            boolean xset = false;
            boolean yset = false;

            if (relx < BORDER) { yset = true; d = Direction.WEST; }
            if (relx > dp.cellWidth - BORDER) { yset = true; d = Direction.EAST; }
            if (rely < BORDER) { xset = true; d = Direction.NORTH; }
            if (rely > dp.cellHeight - BORDER) { xset = true; d = Direction.SOUTH; }
            if (xset && yset) { cellx = INCORNER; celly = INCORNER; d = null; }
        }
        ClickInfo ci = new ClickInfo();
        ci.cellx = cellx;
        ci.celly = celly;
        ci.d = d;
        ci.cellwidth = dp.cellWidth;
        ci.cellheight = dp.cellHeight;
        ci.dxloc = relx;
        ci.dyloc = rely;
        ci.me = e;

        if (cellx == OUTOFBOUNDS || cellx == INCORNER) return;
        for (CellClicked cc : cellclickers) cc.clicked(ci);
        if (d == null) return;
        for (EdgeClicked ec : edgeclickers) ec.clicked(ci);
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
