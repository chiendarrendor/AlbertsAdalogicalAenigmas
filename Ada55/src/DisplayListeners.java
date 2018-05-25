import grid.puzzlebits.Direction;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DisplayListeners {
    public interface BoardHolder {
        Board getBoard();
    }

    public static class MyGridListener implements GridPanel.GridListener {
        BoardHolder bh;
        String line1;
        String line2;

        public MyGridListener(BoardHolder bh, String line1,String line2) { this.bh = bh; this.line1 = line1; this.line2 = line2; }
        public MyGridListener(BoardHolder bh) {
            this(bh,null,null);
        }
        private Board gb() { return bh.getBoard(); }
        public int getNumXCells() {
            return gb().getWidth();
        }
        public int getNumYCells() {
            return gb().getHeight();
        }
        public boolean drawGridNumbers() {
            return true;
        }
        public boolean drawGridLines() {
            return true;
        }
        public boolean drawBoundary() {
            return true;
        }

        public String getAnswerText() {
            StringBuffer sb = new StringBuffer();
            sb.append("<html><font size=\"5\">");
            sb.append(line1);
            sb.append("<br>");
            sb.append(line2);
            sb.append("</font></html>");
            return sb.toString();
        }

        public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D) bi.getGraphics();
            if (gb().getLetter(cx, cy) != '.') GridPanel.DrawStringInCell(bi, Color.BLACK, "" + gb().getLetter(cx, cy));
            if (gb().hasNumber(cx, cy)) GridPanel.DrawStringInCell(bi, Color.RED, "" + gb().getNumber(cx, cy));
            return true;
        }

    }

    public static class MyEdgeListener implements GridPanel.EdgeListener {
        BoardHolder bh;
        public MyEdgeListener(BoardHolder bh) { this.bh = bh; }
        private Board gb() { return bh.getBoard(); }

        @Override
        public EdgeDescriptor onBoundary() {
            return new EdgeDescriptor(Color.BLACK,5);
        }

        @Override
        public EdgeDescriptor toEast(int x, int y) {
            EdgeState es = gb().getEdge(x,y, Direction.EAST);
            return new EdgeDescriptor(es == EdgeState.PATH ? Color.GREEN : Color.BLACK,
                    es == EdgeState.WALL ? 5 : 1);
        }

        @Override
        public EdgeDescriptor toSouth(int x, int y) {
            EdgeState es = gb().getEdge(x,y, Direction.SOUTH);
            return new EdgeDescriptor(es == EdgeState.PATH ? Color.GREEN : Color.BLACK,
                    es == EdgeState.WALL ? 5 : 1);
        }
    }

}
