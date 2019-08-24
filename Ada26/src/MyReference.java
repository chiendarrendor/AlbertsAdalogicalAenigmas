import grid.puzzlebits.Direction;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MyReference implements GridPanel.EdgeListener, GridPanel.GridListener {
    Board b;
    List<String> lines;
    List<ReferenceHelper> helpers = new ArrayList<>();

    public MyReference(Board b, List<String> lines ) { this.b = b; this.lines = lines; }
    @Override public int getNumXCells() { return b.getWidth(); }
    @Override public int getNumYCells() { return b.getHeight(); }
    @Override public boolean drawGridNumbers() { return true; }
    @Override public boolean drawGridLines() { return true; }
    @Override public boolean drawBoundary() { return true; }
    @Override public String[] getAnswerLines() { return lines.toArray(new String[0]); }

    public void addHelper(ReferenceHelper rh) { helpers.add(rh); }


    private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
    private static EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);
    private static int INSET=8;

    @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
        Graphics2D g = (Graphics2D)bi.getGraphics();
        if (b.hasLetter(cx,cy)) {
            GridPanel.DrawStringInCell(bi, Color.BLACK, "" + b.getLetter(cx, cy));
        }
        if (b.hasNumber(cx,cy)) {
            RectangleRegionHandler rrh = b.getHandler(cx,cy);
            Color circolor = null;
            Color letcolor = null;
            switch (rrh.realness) {
                case REAL: circolor = Color.GREEN; letcolor = Color.BLACK; break;
                case FAKE: circolor = Color.RED; letcolor = Color.BLACK; break;
                case UNKNOWN: circolor = Color.BLACK; letcolor = Color.WHITE; break;
            }

            g.setColor(circolor);
            g.fillOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
            GridPanel.DrawStringInCell(bi,letcolor,""+b.getNumber(cx,cy));

            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+rrh.currentRectangles.size(),Direction.SOUTHEAST);
        }

        RectangleRegionSet rrs = b.cells.getCell(cx,cy);
        GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+rrs.size());

        for (ReferenceHelper rh : helpers) {
            rh.addToDrawing(bi,cx,cy);
        }

        return true;
    }

    @Override public EdgeDescriptor onBoundary() { return WALL; }
    @Override public EdgeDescriptor toEast(int x, int y) { return b.regionsMatch(x,y, Direction.EAST) ? PATH : WALL; }
    @Override public EdgeDescriptor toSouth(int x, int y) { return b.regionsMatch(x,y,Direction.SOUTH) ? PATH : WALL; }
}
