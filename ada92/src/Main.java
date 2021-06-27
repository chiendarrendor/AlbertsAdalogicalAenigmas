import grid.file.GridFileReader;
import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import javafx.scene.layout.GridPane;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static final EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,1);
        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return b.getRegionId(x,y) == b.getRegionId(x+1,y) ? PATH : WALL;}
        @Override public EdgeDescriptor toSouth(int x, int y) { return b.getRegionId(x,y) == b.getRegionId(x,y+1) ? PATH : WALL; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);

            // this is going to be replaced with a more dynamic soldier mechaniam when we have it
            if (b.hasSoldier(cx,cy)) {
                int sid = b.getSoldier(cx,cy);
                String s = ""+sid;
                if (sid == -1) s = "?";
                Graphics2D g = (Graphics2D)bi.getGraphics();
                g.setColor(Color.BLACK);
                Font f = g.getFont();
                Font nf = f.deriveFont(Font.BOLD,f.getSize()*3);
                g.setFont(nf);
                GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getHeight(),s);
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
        String[] lines = new String[] { "Adalogical Aenigma","#92 Solver"};

        if (args.length == 2) {
            GridFileReader gfr = new GridFileReader(args[1]);

            StringBuffer sb = new StringBuffer();
            b.forEachCell((x,y)->{
                if (!b.hasLetter(x,y)) return;
                if (gfr.getBlock("SOLUTION")[x][y].equals(".")) return;
                sb.append(LetterRotate.Rotate(b.getLetter(x,y),Integer.parseInt(gfr.getBlock("SOLUTION")[x][y])));
            });

            lines[0] = sb.toString();
            lines[1] = gfr.getVar("SOLUTION");

        }





        MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #92 Solver",1200,800,myl,myl);
    }
}
