import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static class MyGridListener implements GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyGridListener(Board b,String[] lines) { this.b = b; this.lines = lines;   }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return false; }
        public boolean drawBoundary() { return false; }

        private void drawInDir(Direction d, BufferedImage bi, Color color, Stroke stroke,int x,int y) {
            int cenx = bi.getWidth()/2;
            int ceny = bi.getHeight()/2;

            Graphics2D g = (Graphics2D)bi.getGraphics();
            g.setColor(color);
            if (stroke != null) g.setStroke(stroke);

            switch(d) {
                case NORTH: g.drawLine(cenx,ceny,cenx,0); break;
                case SOUTH:   g.drawLine(cenx,ceny,cenx,bi.getHeight()); break;
                case WEST:  g.drawLine(cenx,ceny,0,ceny); break;
                case EAST:   g.drawLine(cenx,ceny,bi.getWidth(),ceny); break;
            }
        }

        private static final Stroke unknownStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
                new float[]{5.0f,5.0f}, 0);
        private static final Stroke pathStroke = new BasicStroke(5.0f);
        private static final int DOTINSET = 10;

        public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            for (Direction d: Direction.orthogonals()) {
                switch(b.getEdge(cx,cy,d)) {
                    case PATH:
                        drawInDir(d,bi,Color.GREEN,pathStroke,cx,cy);
                        break;
                    case WALL:
                        break;
                    case UNKNOWN:
                        drawInDir(d,bi, Color.DARK_GRAY, unknownStroke,cx,cy);
                        break;
                }

            }

            // path stuff goes here.

            if (b.isDot(cx,cy)) {
                g.setColor(Color.WHITE);
                g.fillOval(DOTINSET,DOTINSET,bi.getWidth()-2*DOTINSET,bi.getHeight()-2*DOTINSET);
                g.setColor(Color.BLACK);
                g.drawOval(DOTINSET,DOTINSET,bi.getWidth()-2*DOTINSET,bi.getHeight()-2*DOTINSET);
                GridPanel.DrawStringInCell(bi,Color.BLACK,"" + b.getNumber(cx,cy) + b.getLetter(cx,cy));
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


    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }
        Board b = new Board(args[0]);
        Solver s = new Solver(b);

        s.Solve(b);

        System.out.println("# of solutions: " + s.GetSolutions().size());
        b = s.GetSolutions().get(0);

        PathWalkerEngine pwe = new PathWalkerEngine(b);

        String lines[] = new String[] { pwe.getSolution(), b.getVar("SOLUTION")};
        GridFrame gf = new GridFrame("Adalogical Aenigma #21 Solver",1200,800,new MyGridListener(b,lines));
    }


}
