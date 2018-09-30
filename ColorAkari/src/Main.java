import grid.copycon.Shallow;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Main {
    private static class MyGridListener implements GridPanel.GridListener {
        private Board b;

        public MyGridListener(Board b) { this.b = b; }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        private static final int OVALINSET = 3;
        private static final int CENTERSET = 7;
        private static final int BIGSIZE = 12;
        private static final int SMALLSIZE = 6;

        private void drawSmall(BufferedImage bi, LightCell cell, LightState state, Direction drawdir) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (!cell.contains(state)) return;
            Point perturbed = drawdir.delta(new Point(bi.getWidth()/2,bi.getHeight()/2),CENTERSET);
            g.setColor(state.getColor());
            g.fillOval(perturbed.x-SMALLSIZE/2,perturbed.y-SMALLSIZE/2,SMALLSIZE,SMALLSIZE);
        }


        public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            GroundState gs = b.getGroundState(cx,cy);
            if (gs.blocksLight()) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }
            if (gs.isTarget()) {
                g.setColor(gs.getColor());
                g.fillOval(OVALINSET,OVALINSET,bi.getWidth()-2*OVALINSET,bi.getHeight()-2*OVALINSET);
                GridPanel.DrawStringInCell(bi,Color.BLACK,"" + gs.getColorLetter());
            }

            if (gs != GroundState.TILE) return true;
            LightCell cell = b.getLightCell(cx,cy);
            if (cell.isComplete()) {
                LightState ls = cell.getSingle();
                g.setColor(ls.getColor());
                int cenx = bi.getWidth() / 2;
                int ceny = bi.getHeight() / 2;
                g.fillOval(cenx - BIGSIZE/2,ceny-BIGSIZE/2,BIGSIZE,BIGSIZE);
            } else {
                drawSmall(bi,cell,LightState.NOLIGHT, Direction.NORTH);
                drawSmall(bi,cell,LightState.REDLIGHT,Direction.WEST);
                drawSmall(bi,cell,LightState.GREENLIGHT,Direction.EAST);
                drawSmall(bi,cell,LightState.BLUELIGHT,Direction.SOUTH);
            }






            return true;
        }


    }
    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
	    Solver s = new Solver(b);
	    //s.Solve(b);

        System.out.println("RAL: " + s.recursiveApplyLogic(b));
        System.out.println("ATS: " + s.applyTupleSuccessors(b));
        System.out.println("RAL2: " + s.recursiveApplyLogic(b));

        GridFrame gf = new GridFrame("Color Akari",1000,1000,new MyGridListener(b));

    }


}
