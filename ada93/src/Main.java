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

        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }

        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }


        private static final int INSET = 5;

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.hasSolution() && b.isSolutionCell(cx,cy)) {
                g.setColor(Color.CYAN);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringInCorner(bi, Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            }

            if (b.hasClue(cx,cy)) {
                g.setColor(Color.black);
                g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                if (b.getClue(cx,cy) > 0) {
                    GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getClue(cx,cy));
                }
            }

            return true;
        }


    }


    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        String[] lines = new String[] { "Adalogical Aenigma","#93 Solution Parser"};

        if (args.length > 1) {
            b.applySolution(args[1]);

            StringBuffer sb = new StringBuffer();

            int curnum = -1;
            for(char c : b.gfr.getVar("RAWSOLUTION").toCharArray()) {
                if (Character.isDigit(c)) {
                    curnum = Integer.parseInt(""+c);
                } else if (Character.isAlphabetic(c)){
                    sb.append(LetterRotate.Rotate(c,curnum));
                } else {
                    sb.append(c);
                }
            }




            lines[0] = sb.toString();
            lines[1] = b.gfr.getVar("SOLUTION");

        }



        MyListener myl = new MyListener(b,lines);
	    GridFrame gf = new GridFrame("Adalogical Aenigma #93 Solution Parser",1200,800,myl);
    }


}
