import grid.letter.LetterRotate;
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
        @Override public boolean drawGridNumbers() {  return true;  }
        @Override public boolean drawGridLines() { return true;  }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {

            Graphics2D g = (Graphics2D)bi.getGraphics();
            Color c = null;
            switch(b.getCell(cx,cy)) {
                case INSIDE: c = Color.GREEN; break;
                case OUTSIDE: c = Color.RED; break;
            }

            if (c != null) {
                g.setColor(c);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+b.getLetter(cx,cy));
            if (b.hasClue(cx,cy)) GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getClue(cx,cy));


            return true;
        }


    }
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        String[] lines = new String[] { "Adalogical", "Aenigma" };

        Solver s = new Solver(b);

        s.Solve(b);

        //b.setCell(3,2,CellType.OUTSIDE);
        //b.setCell(1,6,CellType.OUTSIDE);
        //b.setCell(1,1,CellType.INSIDE);
        //b.setCell(1,5,CellType.INSIDE);

        //ClueLogicStep cls = new ClueLogicStep(1,2,6);
        //System.out.println("Logic: " + cls.apply(b));




        System.out.println("# of Solutions: " + s.GetSolutions().size());

        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);
            StringBuffer sb = new StringBuffer();

            int runcount = -1;
            for (int y = 0 ; y < b.getHeight() ; ++y) {
                runcount = -1;
                for (int x = 0 ; x < b.getWidth() ; ++x) {
                    if (b.getCell(x,y) == CellType.INSIDE) {
                        runcount = -1;
                        continue;
                    }

                    if (runcount == -1) {
                        runcount = 0;
                        for (int q = x ; q < b.getWidth() ; ++q) {
                            if (b.getCell(q,y) == CellType.OUTSIDE) ++runcount;
                            else break;
                        }
                    }

                    sb.append(LetterRotate.Rotate(b.getLetter(x,y),runcount));
                }
            }
            lines[0] = sb.toString();
            lines[1] = b.gfr.getVar("SOLUTION");

        }


        MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #36 Solver",1200,800,myl);
    }


}
