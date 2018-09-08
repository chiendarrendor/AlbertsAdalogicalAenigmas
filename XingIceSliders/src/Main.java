import OnTheFlyAStar.AStar;
import OnTheFlyAStar.AStarNode;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.puzzlebits.CellContainer;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class Main {

    private static class MyGridListener implements GridPanel.MultiGridListener {
        private List<Board> blist;
        int bindex;
        private Board b;
        public MyGridListener(List<Board> blist) {
            this.blist = blist;
            b = blist.get(bindex);
        }

        public int getNumXCells() { return b.getWidth();  }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }
        public String getAnswerText() { return b.getMove(); }

        public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            Color fcolor = Color.BLACK;
            switch(b.getFloor(cx,cy)) {
                case WALL: fcolor = Color.BLACK; break;
                case PATH: fcolor = Color.WHITE; break;
                case ICE: fcolor = Color.CYAN; break;
            }
            g.setColor(fcolor);
            g.fillRect(0,0,bi.getWidth(),bi.getHeight());

            int inset = 10;
            if (b.isGoal(cx,cy)) {
                g.setColor(Color.RED);
                g.drawRect(inset, inset, bi.getWidth() - 2 * inset, bi.getHeight() - 2 * inset);
            }

            if (b.isIce(cx,cy)) {
                GridPanel.DrawStringInCell(bi,Color.BLACK,"⛰");
            }

            if (b.isPillar(cx,cy)) {
                GridPanel.DrawStringInCell(bi,Color.BLACK,"▮");
            }

            return true;
        }

        @Override public boolean hasNext() {
            return bindex < blist.size() - 1;
        }

        @Override public void moveToNext() {
            ++bindex;
            b = blist.get(bindex);

        }

        @Override public boolean hasPrev() {
            return bindex > 0;
        }

        @Override public void moveToPrev() {
            --bindex;
            b = blist.get(bindex);

        }
    }



    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);

        AStar.AStarSolution<Board> path = AStar.execute(b);
        List<Board> solution = path.solution;


        GridFrame gf = new GridFrame("XING Ice Sliders Puzzle",600,800,new MyGridListener(solution));
    }


}
