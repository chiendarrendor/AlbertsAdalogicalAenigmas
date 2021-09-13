import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {
    // 20 distinct colors from https://sashamaps.net/docs/resources/20-colors/

    private static Color[] backColors = {
            new Color(0x000000),
            new Color(0x800000),
            new Color(0x9a6324),
            new Color(0x808000),
            new Color(0x469990),
            new Color(0x000075),
            new Color(0xe6194b),
            new Color(0xf58231),
            new Color(0xffe119),
            new Color(0xbfef45),
            new Color(0x3cb44b),
            new Color(0x42d4f4),
            new Color(0x4363d8),
            new Color(0x911eb4),
            new Color(0xf032e6),
            new Color(0xfabed4),
            new Color(0xffd8b1),
            new Color(0xfffac8),
            new Color(0xaaffc3),
            new Color(0xdcbeff),
    };
    private static Color[] frontColors = {
            Color.WHITE,
            Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE,
            Color.WHITE,Color.WHITE,Color.BLACK,Color.BLACK,Color.WHITE,Color.BLACK,Color.WHITE,Color.WHITE,Color.WHITE,
            Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK
    };



    private static class MyReference implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;

        public MyReference(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static final EdgeDescriptor UNKNOWN = new EdgeDescriptor(Color.BLACK,1);
        private static final EdgeDescriptor PATH = new EdgeDescriptor(Color.GREEN,1);

        @Override public EdgeDescriptor onBoundary() { return WALL; }

        @Override public EdgeDescriptor toEast(int x, int y) {
            switch(b.getEdge(x,y, Direction.EAST)) {
                case PATH: return PATH;
                case WALL: return WALL;
                case UNKNOWN: return UNKNOWN;
                default: throw new RuntimeException("What Edge?");
            }
        }

        @Override public EdgeDescriptor toSouth(int x, int y) {
            switch(b.getEdge(x,y, Direction.SOUTH)) {
                case PATH: return PATH;
                case WALL: return WALL;
                case UNKNOWN: return UNKNOWN;
                default: throw new RuntimeException("What Edge?");
            }
        }

        private static final int DOTINSET = 25;
        private static final int HALFINSET = DOTINSET/2;

        private void drawCornerDot(BufferedImage bi, int x, int y, Direction d,Color color) {
            if(!b.hasDotInCorner(x,y,d)) return;
            Graphics2D g = (Graphics2D)bi.getGraphics();
            g.setColor(color);

            int ulx = Integer.MAX_VALUE;
            int uly = Integer.MAX_VALUE;
            int startangle = Integer.MAX_VALUE;
            int arcAngle = 90; // counterclockwise from startangle

            switch(d) {
                case NORTHWEST: ulx = -HALFINSET; uly = -HALFINSET; startangle = 270; break;
                case SOUTHWEST: ulx = -HALFINSET; uly = bi.getHeight() - HALFINSET ; startangle = 0; break;
                case NORTHEAST: ulx = bi.getWidth()-HALFINSET; uly = -HALFINSET; startangle = 180;  break;
                case SOUTHEAST: ulx = bi.getWidth()-HALFINSET; uly = bi.getHeight()-HALFINSET; startangle = 90; break;
                default: throw new RuntimeException("Corner dots should be in corners!");
            }
            g.fillArc(ulx,uly,DOTINSET,DOTINSET,startangle,arcAngle);

        }



        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Color frontColor = Color.BLACK;
            District d = b.getDistrict(cx,cy);
            if (!d.isBroken && d.isNumbered) {
                int number = d.number;
                int colorindex = number % backColors.length;
                frontColor = frontColors[colorindex];
                Graphics2D g = (Graphics2D)bi.getGraphics();
                g.setColor(backColors[colorindex]);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }



            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCell(bi,frontColor,""+b.getLetter(cx,cy));
            if (b.hasNumber(cx,cy)) {
                Graphics2D g = (Graphics2D)bi.getGraphics();
                g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 3.0f).deriveFont(Font.BOLD));
                g.setColor(frontColor);

               GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getHeight(),""+b.getNumber(cx,cy));
            }

            for (Direction dir : Direction.diagonals()) {
                drawCornerDot(bi,cx,cy,dir,frontColor);
            }


            GridPanel.DrawStringInCorner(bi,frontColor,""+d.getId(),Direction.SOUTH);
            if (d.isBroken) {
                GridPanel.DrawStringInCorner(bi,frontColor,"X",Direction.NORTH);
            } else if (!d.isNumbered) {
                GridPanel.DrawStringInCorner(bi,frontColor,"?",Direction.NORTH);
            } else {
                GridPanel.DrawStringInCorner(bi,frontColor,""+d.number,Direction.NORTH);
            }


            return true;
        }


    }

    public static void main(String[] args) {
        if (args.length < 1 ) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        String[] lines = new String[] {"Adalogical Aenigma","#95 Solver"};
        Solver s = new Solver(b);

        if (args.length > 1) {
            BoardLoader.Load(args[1],b);
        }

        //s.debug();
        //s.testRecursion(b);

        s.Solve(b);

        if (s.GetSolutions().size() == 1) {
            Board solution = s.GetSolutions().get(0);
            StringBuffer sb = new StringBuffer();
            for (int y = 0 ; y < solution.getHeight() ; ++y) {
                for (int x = 0 ; x < solution.getWidth() ; ++x) {
                    if (!solution.hasLetter(x,y)) continue;
                    List<Direction> walls = new ArrayList<>();
                    for (Direction d : Direction.orthogonals()) {
                        if (solution.getEdge(x,y,d) == EdgeState.WALL) walls.add(d);
                    }
                    if (walls.size() > 2) continue;
                    if (walls.size() < 2 || walls.get(0) != walls.get(1).getOpp()) {
                        sb.append(LetterRotate.Rotate(solution.getLetter(x,y),solution.getDistrict(x,y).number));
                    }
                }
            }
            lines[0] = sb.toString();
            lines[1] = solution.gfr.getVar("SOLUTION");
        }



        MyReference myr = new MyReference(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #95 solver",1200,800,myr,myr);
    }


}
