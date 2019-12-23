import grid.letter.LetterRotate;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static class MyListener implements GridPanel.GridListener {
        AdaBoard b;
        String[] lines;

        public MyListener(AdaBoard b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics g = bi.getGraphics();
            if (b.isClue(cx,cy)) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                if (b.isNumberedClue(cx,cy)) {
                    GridPanel.DrawStringInCell(bi,Color.WHITE,""+b.getClueNumber(cx,cy));
                }
            } else {
                if (b.isLit(cx,cy)) {
                    g.setColor(new Color(0x5dfc0a));
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                }
                GridPanel.DrawStringUpperLeftCell(bi,Color.BLACK,""+b.getLetter(cx,cy));

                if (b.getCell(cx,cy) != CellType.EMPTY) {
                    int diam = 5;
                    if (b.getCell(cx, cy) == CellType.BULB) diam = 20;
                    g.setColor(new Color(0x308014));
                    g.fillOval((bi.getWidth() - diam) / 2, (bi.getHeight() - diam) / 2, diam, diam);
                }
            }

            return true;
        }


    }

    public static String getRawClue(AdaBoard b) {
        StringBuffer sb = new StringBuffer();
        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0; x< b.getWidth() ; ++x) {
                if (b.getCell(x,y) == CellType.BULB) sb.append(b.getLetter(x,y));
            }
        }
        return sb.toString();
    }

    public static String getNumDirLitClue(AdaBoard b) {
        StringBuffer sb = new StringBuffer();
        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0; x < b.getWidth(); ++x) {
                if (b.getCell(x,y) != CellType.BULB) continue;
                List<Point> adjacents = b.getVisibleCells(x,y,true);
                sb.append(LetterRotate.Rotate(b.getLetter(x,y),adjacents.size()));
            }
        }
        return sb.toString();
    }

    public static String getEachSizeClue(AdaBoard b) {
        List<StringBuffer> sbl = new ArrayList<>();
        for (int i = 0 ; i <= 4 ; ++i) { sbl.add(new StringBuffer()); }

        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0; x < b.getWidth(); ++x) {
                if (b.getCell(x,y) != CellType.BULB) continue;
                List<Point> adjacents = b.getVisibleCells(x,y,true);
                sbl.get(adjacents.size()).append(b.getLetter(x,y));
            }
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i <= 4 ; ++i) { sb.append(sbl.get(i).toString()).append("/"); }

        return sb.toString();
    }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad command line");
            System.exit(1);
        }

        AdaBoard newboard = new AdaBoard(args[0]);
        Solver s = new Solver(newboard);
        s.Solve(newboard);

        AdaBoard b = null;


        if (s.GetSolutions().size() == 0) {
            System.out.println("No Solutions");
            System.exit(1);
            b = newboard;
        } else {
            b = s.GetSolutions().get(0);
        }

        if (s.GetSolutions().size() > 1) {
            System.out.println("Non unique solution");
        }

        String cluetype = b.gfr.getVar("CLUETYPE");
        String result = null;
        if (cluetype == null) {
            result = "No CLUETYPE specified";
        } else if (cluetype.equals("RAW")) {
            result = getRawClue(b);
        } else if (cluetype.equals("NUMDIRLIT")) {
            result = getNumDirLitClue(b);
        } else if (cluetype.equals("EACHSIZE")) {
            result = getEachSizeClue(b);
        } else {
            throw new RuntimeException("Unknown Clue Type " + cluetype);
        }






        String[] lines = new String[] { result, b.gfr.getVar("SOLUTION"),b.gfr.getVar("SOLUTION2"),b.gfr.getVar("SOLUTION3") };

        GridFrame gf = new GridFrame("Adalogical Aenigma #6 Solver",1200,800,new MyListener(b,lines));
    }


}
