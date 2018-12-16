import grid.graph.GridGraph;
import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import javafx.scene.transform.Rotate;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Set;

public class Main {

    private static class MyGridListener implements GridPanel.GridListener {
        Board b;
        public MyGridListener(Board b) { this.b = b; }

        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }

        private boolean exits(int x,int y,Direction d) {
            switch(b.getCell(x,y)) {
                case UNKNOWN: throw new RuntimeException("Should be no unknowns at this point!");
                case BLOCK: return false;
                case ORTHO_EMPTY: return true;
                case DIAG_EMPTY: return true;
                case OPEN_NE: return d == Direction.NORTH || d == Direction.EAST;
                case OPEN_NW: return d == Direction.NORTH || d == Direction.WEST;
                case OPEN_SE: return d == Direction.SOUTH || d == Direction.EAST;
                case OPEN_SW: return d == Direction.SOUTH || d == Direction.WEST;
                default: throw new RuntimeException("This should never happen!");
            }
        }



        private class MyGGReference implements GridGraph.GridReference {
            @Override public int getWidth() { return b.getWidth(); }
            @Override public int getHeight() { return b.getHeight(); }
            @Override public boolean isIncludedCell(int x, int y) {
                CellContents cc = b.getCell(x,y);
                return cc == CellContents.OPEN_NE || cc == CellContents.OPEN_NW || cc == CellContents.OPEN_SE
                        || cc == CellContents.OPEN_SW || cc == CellContents.ORTHO_EMPTY || cc == CellContents.DIAG_EMPTY;
            }

            @Override public boolean edgeExitsEast(int x, int y) {
                return exits(x,y,Direction.EAST) && exits(x+1,y,Direction.WEST);
            }

            @Override public boolean edgeExitsSouth(int x, int y) {
                return exits(x,y,Direction.SOUTH) && exits(x,y+1,Direction.NORTH);
            }
        }


        @Override public String[] getAnswerLines() {
            GridGraph gg = new GridGraph(new MyGGReference());
            StringBuffer sb = new StringBuffer();
            b.forEachCell((x,y) -> {
                CellContents cc = b.getCell(x,y);
                if (cc != CellContents.ORTHO_EMPTY && cc != CellContents.DIAG_EMPTY) return;
                Set<Point> groupies = gg.connectedSetOf(new Point(x,y));
                int ecount = (int)groupies.stream().filter(p->b.getCell(p.x,p.y)==CellContents.ORTHO_EMPTY || b.getCell(p.x,p.y)==CellContents.DIAG_EMPTY).count();
                sb.append(LetterRotate.Rotate(b.getLetter(x,y),ecount));
            });




            return new String[] { sb.toString() , b.gfr.getVar("SOLUTION")};
        }


        private static final int INSET = 10;

        private static void fill(Color c,BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            g.setColor(c);
            g.fillRect(0,0,bi.getWidth(),bi.getHeight());
        }

        private static void outlineOrtho(BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            g.setColor(Color.BLACK);
            g.drawLine(INSET,INSET*2,INSET,bi.getHeight() - INSET*2);
            g.drawLine(bi.getWidth()-INSET,INSET*2,bi.getWidth()-INSET,bi.getHeight() - INSET*2);
            g.drawLine(INSET*2,INSET,bi.getWidth() - INSET*2,INSET);
            g.drawLine(INSET*2,bi.getHeight()-INSET,bi.getWidth() - INSET*2,bi.getHeight()-INSET);
        }

        private static void outlineDiag(BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            g.setColor(Color.BLACK);
            g.drawLine(INSET,INSET*2,INSET*2,INSET);
            g.drawLine(INSET,bi.getHeight() - INSET*2,INSET*2,bi.getHeight()-INSET);
            g.drawLine(bi.getWidth()-INSET,INSET*2,bi.getWidth() - INSET*2,INSET);
            g.drawLine(bi.getWidth()-INSET,bi.getHeight() - INSET*2,bi.getWidth() - INSET*2,bi.getHeight()-INSET);
        }

        private static void triangle(BufferedImage bi, Direction corner) {
            int d1x,d1y;
            int d2x,d2y;
            int cx,cy;
            d1x = 0; d1y = 0;
            d2x = 0; d2y = 0;
            cx = 0; cy = 0;

            switch(corner) {
                case NORTHEAST: cx = bi.getWidth() ; cy = 0; break;
                case NORTHWEST: cx = 0; cy = 0; break;
                case SOUTHWEST: cx = 0; cy = bi.getHeight(); break;
                case SOUTHEAST: cx = bi.getWidth(); cy = bi.getHeight(); break;
            }

            switch(corner) {

                case NORTHWEST:
                case SOUTHEAST:
                    d1x = bi.getWidth() ; d1y = 0;
                    d2x = 0; d2y = bi.getHeight();
                    break;
                case NORTHEAST:
                case SOUTHWEST:
                    d1x = 0; d1y = 0;
                    d2x = bi.getWidth(); d2y = bi.getHeight();
                    break;
            }

            Graphics2D g = (Graphics2D)bi.getGraphics();
            g.setColor(Color.WHITE);
            g.fillPolygon(new int[] {d1x,d2x,cx},new int[] {d1y,d2y,cy},3);

        }


        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {


            switch(b.getCell(cx,cy)) {
                case UNKNOWN: break;
                case BLOCK: fill(Color.BLUE,bi); break;
                case ORTHO_EMPTY: fill(Color.WHITE,bi); outlineOrtho(bi); break;
                case DIAG_EMPTY: fill(Color.WHITE,bi); outlineDiag(bi); break;
                case OPEN_NE: fill(Color.BLUE,bi); triangle(bi, Direction.NORTHEAST); break;
                case OPEN_NW: fill(Color.BLUE,bi); triangle(bi, Direction.NORTHWEST); break;
                case OPEN_SE: fill(Color.BLUE,bi); triangle(bi, Direction.SOUTHEAST); break;
                case OPEN_SW: fill(Color.BLUE,bi); triangle(bi, Direction.SOUTHWEST); break;
            }

            if (b.hasLetter(cx, cy)) {
                GridPanel.DrawStringUpperLeftCell(bi, Color.RED,""+b.getLetter(cx,cy));
            }
            if (b.hasNumber(cx,cy)) {
                Graphics2D g = (Graphics2D)bi.getGraphics();
                g.setFont(g.getFont().deriveFont(g.getFont().getSize()*2.0f));
                GridPanel.DrawStringInCell(g,Color.GREEN,0,0,bi.getWidth(),bi.getHeight(),""+b.getNumber(cx,cy));
            }


            return true;
        }


    }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad command line");
            System.exit(1);
        }

        Board b = new Board(args[0]);



        GridFrame gf = new GridFrame("Adalogical Aenigma #14 Solution Displayer",1200,800,
                new MyGridListener(b));
    }


}
