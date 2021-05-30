import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {
    private static class MyListener implements GridPanel.GridListener{
        Board b;
        String[] lines;
        public MyListener(Board b,String[] lines) { this.b = b; this.lines = lines; }

        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true;  }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true;  }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (!b.hasLetter(cx,cy)) {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                return true;
            }
            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            StringBuffer sb = new StringBuffer();
            CellState cs = b.getCell(cx,cy);
            for(int poss : cs.getSet()) sb.append(""+poss);
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
	    String[] lines = new String[] { "Adalogical Aenigma", "#91 Solver"};
	    Solver s = new Solver(b);

        s.Solve(b);

        System.out.println("# of solutions: " + s.GetSolutions().size());

        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);
            Board fb = b;
            StringBuffer sb = new StringBuffer();
            fb.forEachCell((x,y)-> {
                if (!fb.hasLetter(x,y)) return;
                char c = fb.getLetter(x,y);
                int v = fb.getCell(x,y).unique();
                if (v%2 == 1) return;
                sb.append(LetterRotate.Rotate(c,v));
            });
            lines[0] = sb.toString();
            lines[1] = fb.gfr.getVar("SOLUTION");


        }



	    MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #91 Solver",1200,800,myl);

    }
}
