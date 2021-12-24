import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {
    private static class MyListener implements GridPanel.GridListener {
        Board b;
        String[] lines;

        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines;    }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            Color backColor = Color.CYAN;
            CellData cd = null;
            boolean doContents = false;
            if (!b.inBounds(cx,cy)) {
                backColor = Color.BLACK;
            } else {
                cd = b.getCellData(cx, cy);
                if (cd == null) {
                    backColor = Color.LIGHT_GRAY;
                } else {
                    backColor = Color.WHITE;
                    doContents = true;
                }
            }

            g.setColor(backColor);
            g.fillRect(0,0,bi.getWidth(),bi.getHeight());

            if (!doContents) return true;

            String contents = "";
            if (!cd.isValid()) {
                contents = "!";
            } else {
                StringBuffer sb = new StringBuffer();
                for (int i = b.getMinNumber() ; i <= b.getMaxNumber() ; ++i) {
                    if (cd.has(i)) sb.append(i);
                }
                contents = sb.toString();
            }

            GridPanel.DrawStringInCell(bi,Color.BLACK,contents);

            return true;
        }


    }



    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad command line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
	    String[] lines = new String[] { b.getGridName(),"Adalogical Aenigma","#98 Solver"};

	    Solver s = new Solver(b);
	    //s.debug();
	    //s.testRecursion(b);

        s.Solve(b);
        System.out.println("# of solutions: " + s.GetSolutions().size());

        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);
            StringBuffer sb = new StringBuffer();
            String solclue = b.getSolutionClue();
            for (int x = 0 ; x < b.getWidth();  ++x) {
                int solutionsum = 0;
                for (int y = 0 ; y < b.getHeight() ; ++y) {
                    CellData cd = b.getCellData(x,y);
                    if (cd == null) continue;
                    if (!b.isSolutionNumber(cd.getValue())) continue;
                    solutionsum += cd.getValue();
                }

                sb.append(solclue.charAt(x) == '-' ? solclue.charAt(x) : LetterRotate.Rotate(solclue.charAt(x),solutionsum));
            }
            lines[1] = sb.toString();
            lines[2] = b.getSolution();
        }





	    MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #98 Solver",1200,800,myl);
    }


}
