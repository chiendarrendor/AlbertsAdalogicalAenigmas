import grid.graph.GridGraph;
import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Main {

    private static class MyGridListener implements GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyGridListener(Board b, String[] strings) { this.b = b; this.lines = strings; }

        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        private static int INSET = 5;
        public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.getCell(cx,cy) != CellState.UNKNOWN) {
                g.setColor(b.getCell(cx,cy) == CellState.WHITE ? Color.WHITE : Color.BLACK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi, Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            if (b.isDot(cx,cy)) {
                g.setColor(Color.BLACK);
                g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
            }
            if (b.isNumber(cx,cy)) {
                GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getNumber(cx,cy));
            }


            return true;
        }

        public String getAnswerText() {
            StringBuffer sb = new StringBuffer();
            sb.append("<html><font size=\"5\">");
            Arrays.stream(lines).forEach(line->sb.append(line).append("<br>"));
            sb.append("</font></html>");
            return sb.toString();
        }
    }

    private static class TerminalGridReference implements GridGraph.GridReference {
        Board b;
        public TerminalGridReference(Board b) { this.b = b; }

        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return b.getCell(x,y) == CellState.WHITE && b.hasLetter(x,y); }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }



    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad command line");
            System.exit(1);
        }
        Board ob = new Board(args[0]);
        Solver s = new Solver(ob);
        s.Solve(ob);
        System.out.println("# of solutions found: " + s.GetSolutions().size());



        Board b = s.GetSolutions().get(0);

        GridGraph gg = new GridGraph(new TerminalGridReference(b));

        StringBuffer sb = new StringBuffer();
        b.forEachCell((x,y)-> {
            if (!b.hasLetter(x,y)) return;
            if (b.getCell(x,y) != CellState.WHITE) return;
            int setsize = gg.connectedSetOf(new Point(x,y)).size();

            sb.append(LetterRotate.Rotate(b.getLetter(x,y),setsize));
        });

        String[] strings = new String[] { sb.toString(), b.gfr.getVar("SOLUTION") };

        GridFrame gf = new GridFrame("Adalogical Aenigma #62 Solver",1200,800,
                new MyGridListener(b,strings));
    }


}
