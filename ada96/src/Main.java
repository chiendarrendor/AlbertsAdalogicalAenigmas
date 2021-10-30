import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import grid.spring.GridPanelFactory;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static class MyListener implements GridPanel.GridListener, GridPanelFactory, OverlayGridPanel.CellPairGenerator {
        Board b;
        String[] lines;
        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }

        @Override public int getNumXCells() { return b.getWidth();  }
        @Override public int getNumYCells() { return b.getHeight();  }
        @Override public boolean drawGridNumbers() { return true;  }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringInCorner(bi, Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            }

            if (b.isKnight(cx,cy)) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.knightSize(cx,cy),Direction.SOUTH);
            }

            CellState cs = b.getCell(cx,cy);
            Color c = Color.WHITE;
            char ch = ' ';

            switch(cs) {
                case UNKNOWN: c = Color.LIGHT_GRAY; ch = '?'; break;
                case CANT_STOP_HERE: c = Color.RED; ch = 'X'; break;
                case MUST_HAVE_KNIGHT: c = Color.RED; ch = '!'; break;
                case POSITION_INITIAL: c = Color.LIGHT_GRAY; ch = '♞'; break;
                case POSITION_INTERMEDIATE: c = Color.BLUE; ch = '♞'; break;
                case POSITION_FINAL: c = Color.GREEN; ch = '♞'; break;
            }

            GridPanel.DrawStringInCell(bi,c,""+ch);


            return true;
        }

        @Override public List<OverlayGridPanel.CellPair> get() {
            List<OverlayGridPanel.CellPair> pairs = new ArrayList<>();
            for (int kid : b.getKnightKeys()) {
                Knight knight = b.getKnight(kid);
                if (knight.numPaths() == 0 || knight.numPaths() > 1) continue;
                List<Point> path = knight.getUniquePathList();

                for(int i = 1 ; i < path.size() ; ++i) {
                    Point cp = path.get(i);
                    Point pp = path.get(i-1);
                    pairs.add(new OverlayGridPanel.CellPair(pp.x,pp.y,cp.x,cp.y,Color.BLACK));
                }
            }
            return pairs;
        }


        @Override
        public GridPanel getGridPanel(int width, int height, GridPanel.GridListener listener, GridPanel.EdgeListener edgeListener) {
            return new OverlayGridPanel(width,height,listener,this);
        }


    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        String[] lines = new String[] { "Adalogical","Aenigma","#96 Solver"};

        Solver s = new Solver(b);
        s.Solve(b);


        System.out.println("Solution Size: " + s.GetSolutions().size());

        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);
            CellContainer<Integer> knightsMoveCounts = new CellContainer<Integer>(b.getWidth(),b.getHeight(),
                    (x,y)->0);

            for (int kid : b.getKnightKeys()) {
                Knight knight = b.getKnight(kid);
                List<Point> path = knight.getUniquePathList();
                int numJumps = path.size() - 1;
                for (int i = 1 ; i < path.size() ; ++i) {
                    Point jumpTarget = path.get(i);
                    knightsMoveCounts.setCell(jumpTarget.x,jumpTarget.y,numJumps);
                }
            }

            StringBuffer sb = new StringBuffer();
            for (int y = 0 ; y < b.getHeight() ; ++y) {
                for (int x = 0 ; x < b.getWidth() ; ++x) {
                    if (knightsMoveCounts.getCell(x,y) == 0) continue;
                    sb.append(LetterRotate.Rotate(b.getLetter(x,y),knightsMoveCounts.getCell(x,y)));
                }
            }

            lines[0] = b.gfr.getVar("PUZZLENAME");
            lines[1] = sb.toString();
            lines[2] = b.gfr.getVar("SOLUTION");

        } else if (s.GetSolutions().size() > 1) {
            b = s.GetSolutions().get(0);
        }



        MyListener myl = new MyListener(b,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #96 Solver", 1200,800, myl,(GridPanelFactory)myl);

    }


}

// this solver has a very different approach, due to the computation infeasibility of pre-calculating the list of
// all possible knights jumps, particularly for those knights that can make unlimited jumps.
//
// To note, we are not doing the Flatten Solving here, due to this computational infeasibility.
//
// Solution overview.   Knights contain trees of possible moves, where leaf nodes may or may not be expandable out to another move.
// During the KnightLogicStep, we expand out unexpanded leaves until the number of possible (feasible) solutions on the knight exceeds the
// Knight's allowable feasibles, which starts small.
//
// During the guessAlternatives process, we sort the knights first by smallest # of unexpanded leaves, and then by
// smallest number of feasible moves available in the tree.   Our guess is always with the knight with the smallest
// # of feaasible moves and no unexpanded leaves.   if the top knight in the sort has any unexpanded leaves, we increase
// (double in size) that knights allowable feasibles, and run the logic again.
//
// The important thing here is to keep the active trees as small as possible.