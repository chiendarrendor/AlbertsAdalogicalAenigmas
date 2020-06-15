import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {
    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;
        public MyListener(Board b,String[] lines) { this.b = b ; this.lines = lines; }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static EdgeDescriptor UNKNOWN = new EdgeDescriptor(Color.BLACK,1);
        private static EdgeDescriptor PATH = new EdgeDescriptor(Color.RED,1);
        @Override public EdgeDescriptor onBoundary() { return WALL; }
        private EdgeDescriptor inDirection(int x,int y,Direction d) {
            switch(b.getEdge(x,y,d)) {
                case WALL: return WALL;
                case UNKNOWN: return UNKNOWN;
                case PATH: return PATH;
                default: throw new RuntimeException("Unknown Edge type " + b.getEdge(x,y,d));
            }
        }
        @Override public EdgeDescriptor toEast(int x, int y) { return inDirection(x,y,Direction.EAST); }
        @Override public EdgeDescriptor toSouth(int x, int y) { return inDirection(x,y,Direction.SOUTH); }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D) bi.getGraphics();
            g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 2.0f));
            g.setColor(Color.BLACK);

            if (b.hasAnimal(cx,cy)) {
                String animal = "";
                switch(b.getAnimal(cx,cy)) {
                    case 'F': animal = "\uD83E\uDD8A"; break;
                    case 'G': animal = "\uD83D\uDC10"; break;
                }

                GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getHeight(),animal);
            }

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getHeight(),""+b.getLetter(cx,cy));


            for (Direction d : Direction.diagonals()) {
                if (b.postInCorner(cx,cy,d)) GridPanel.DrawStringInCorner(bi,Color.BLACK,"#",d);
            }





            return true;
        }
    }



    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        String[] lines = new String[] { "Adalogical Aenigma" , "#80" };

        Solver s = new Solver(b);
        s.Solve(b);

        System.out.println("# Solutions found: " + s.GetSolutions().size());
        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);

            Board statb = b;
            StringBuffer sb = new StringBuffer();
            statb.forEachCell((x,y)-> {
                if (!statb.hasLetter(x,y)) return;
                char c = statb.getLetter(x,y);

                int wcount = 0;
                for(Direction d : Direction.orthogonals()) {
                    if (statb.getEdge(x,y,d) == EdgeType.WALL)  ++wcount;
                }
                if (wcount == 3) sb.append(LetterRotate.Rotate(c,3));
            });

            lines[0] = sb.toString();
            lines[1] = statb.gfr.getVar("SOLUTION");

        }


        MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #80 Solver", 1200,800,myl,myl);

    }
}
