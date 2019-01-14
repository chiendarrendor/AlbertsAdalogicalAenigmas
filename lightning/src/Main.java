import grid.logic.LogicStatus;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {
    public static class MyGridListener implements GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyGridListener(Board b,String[] lines) { this.b = b; this.lines = lines; }

        @Override public int getNumXCells() { return b.getWidth() * b.getBoardCount(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int rcx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            int cx = rcx % b.getWidth();
            int bid = rcx / b.getWidth();
            SubBoard sb = b.getSubBoard(bid);

            if (sb.isBlocker(cx,cy)) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                if (sb.isNumber(cx,cy)) {
                    g.setFont(g.getFont().deriveFont(20.0f));
                    GridPanel.DrawStringInCell(g,Color.WHITE,0,0,bi.getWidth(),bi.getHeight(),""+sb.getNumber(cx,cy));
                }
            } else {
                if (sb.getLightState(cx,cy) == LightState.LIGHT) {
                    g.setColor(Color.GREEN);
                    g.drawOval(0,0,bi.getWidth(),bi.getHeight());
                }
                if (sb.getLightState(cx,cy) == LightState.NOLIGHT) {
                    g.setColor(Color.RED);
                    g.drawOval(0,0,bi.getWidth(),bi.getHeight());
                }
                GridPanel.DrawStringInCell(bi,Color.BLACK,sb.getString(cx,cy));
            }
            return true;
        }
    }

    private static class MyEdgeListener implements GridPanel.EdgeListener {
        Board b;
        public MyEdgeListener(Board b) { this.b = b; }
        private static EdgeDescriptor thin = new EdgeDescriptor(Color.BLACK,1);
        private static EdgeDescriptor thick = new EdgeDescriptor(Color.BLACK,5);


        @Override public EdgeDescriptor onBoundary() { return thick; }
        @Override public EdgeDescriptor toEast(int x, int y) { return x % b.getWidth() == b.getWidth()-1 ? thick : thin; }
        @Override public EdgeDescriptor toSouth(int x, int y) { return thin; }
    }






    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad command line");
            System.exit(1);
        }
        Board b = new Board(args[0]);
        Solver s = new Solver(b);

        s.Solve(b);
        System.out.println("# of Solutions: " + s.GetSolutions().size());

        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);
        } else {
            System.out.println("Not Right");
            System.exit(1);
        }

        Board fb = b;


        StringBuffer sb = new StringBuffer();

        for (int i = 0 ; i < b.getBoardCount() ; ++i ) {
            SubBoard myb = b.getSubBoard(i);
            SubBoard ob = b.getSubBoard(i == 0 ? 1 : 0);
            b.forEachCell((x,y)-> {
                if (myb.getLightState(x,y) == LightState.LIGHT && ob.getLightState(x,y ) == LightState.LIGHT) {
                    sb.append(myb.getString(x,y) + " ");
                }
            });
        }






        String[] lines = new String[] {sb.toString(),b.gfr.getVar("SOLUTION")};

	    GridFrame gf = new GridFrame("Pavel's Lightning Puzzle",1200,800,
                new MyGridListener(b,lines),new MyEdgeListener(b));
    }
}
