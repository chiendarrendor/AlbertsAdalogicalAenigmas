import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Main {

    private static class MyGridListener implements GridPanel.GridListener {
        Board b;
        String[] labels;

        public MyGridListener(Board b,String[] labels) { this.b = b; this.labels = labels; }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines(){ return true; }
        public boolean drawBoundary() { return true; }


        @Override public String getAnswerText() {
            StringBuffer sb = new StringBuffer();
            sb.append("<html><font size=\"5\">");
            Arrays.stream(labels).forEach(line->sb.append(line).append("<br>"));
            sb.append("</font></html>");
            return sb.toString();
        }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (b.hasLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi, Color.BLACK,""+b.getLetter(cx,cy));
            if (b.hasNumber(cx,cy)) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                GridPanel.DrawStringInCell(bi,Color.WHITE,""+b.getNumber(cx,cy));
            }

            g.setColor(Color.GREEN);
            g.setStroke(new BasicStroke(5));
            if (b.getCell(cx,cy) == CellState.HORIZONTAL) {
                g.drawLine(0,bi.getHeight()/2,bi.getWidth(),bi.getHeight()/2);
            }

            if (b.getCell(cx,cy) == CellState.VERTICAL) {
                g.drawLine(bi.getWidth()/2,0,bi.getWidth()/2,bi.getHeight());
            }


            return true;
        }
    }

    public static void main(String[] args) {
	    if (args.length != 1) {
	        throw new RuntimeException("Bad Command Line");
        }

        Board b = new Board(args[0]);
	    Solver s = new Solver(b);

	    s.Solve(b);

	    System.out.println("# of Solutions: " + s.GetSolutions().size());

        b = s.GetSolutions().get(0);
        final Board fb = b;

        StringBuffer sb = new StringBuffer();
        b.forEachCell((x,y)-> {
            Point p = new Point(x,y);
            if (!fb.hasLetter(x,y)) return;
            CellState cs = fb.getCell(x,y);
            Direction[] ds = cs == CellState.HORIZONTAL ?
                    new Direction[]{ Direction.EAST,Direction.WEST} :
                    new Direction[] {Direction.NORTH,Direction.SOUTH};

            for (Direction d : ds) {
                Point dp = d.delta(p,1);
                if (fb.inBounds(dp.x,dp.y) && fb.getCell(dp.x,dp.y) == cs) return;
            }
            sb.append(LetterRotate.Rotate(fb.getLetter(x,y),1));


        });

	    String[] lines = new String[2];
	    lines[0] = sb.toString();
	    lines[1] = b.getSolution();







	    GridFrame gf = new GridFrame("Ada #57 Solver",1000,800,new MyGridListener(b,lines));
    }


}
