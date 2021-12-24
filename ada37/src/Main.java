import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {
    private static class MyListener implements GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }

        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            CellData cd = b.getCellData(cx,cy);
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (cd.isWall()) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                return true;
            } else if (cd.isPath()) {
                g.setColor(Color.WHITE);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i <= 4 ; ++i) {
                if (cd.has(i)) sb.append(i);
            }
            GridPanel.DrawStringInCell(bi,Color.BLACK,sb.toString());

            return true;
        }


    }



    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
	    String[] lines = new String[] { b.getName(),"Adalogical Aenigma","#37 solver"};

	    Solver s = new Solver(b);
	    s.Solve(b);

	    if (s.GetSolutions().size() == 1) {
	        b = s.GetSolutions().get(0);
	        switch(b.getSolutionType()) {
                case ADA37: lines[1] = SolutionGenerator.generateAda37Solution(b); break;
                case ADDENDUM32: lines[1] = SolutionGenerator.generateAddendum32Solution(b);  break;
                default:
                    throw new RuntimeException("Unknnown Solution Type requested");
            }
            lines[2] = b.getSolution();
        }



	    MyListener myl = new MyListener(b,lines);
	    GridFrame gfr = new GridFrame("Adalogical Aenigma #37 solver",1200,800,myl);
    }
}
