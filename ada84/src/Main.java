import grid.graph.GridGraph;
import grid.letter.LetterRotate;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

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


        private static int INSET = 5;
        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (b.getCell(cx,cy) == CellType.PATH) {
                g.setColor(Color.GREEN);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.getCell(cx,cy) == CellType.WALL) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                return true;
            }

            if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
            if (b.hasClue(cx,cy)) {
                if (b.isTerminal(cx,cy)) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                }
                g.setColor(Color.BLACK);
                g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                int clue = b.getClue(cx,cy);
                if (clue > 0) {
                    GridPanel.DrawStringInCell(bi,Color.BLACK,""+clue);
                }
            }

            return true;
        }


    }

    private static class ShortestPathListener implements GridGraph.GridReference {
        Board b;
        public ShortestPathListener(Board b) { this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return b.getCell(x,y) == CellType.PATH; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
	    String[] lines = new String[] { "Adalogical" , "Aenigma" };

	    FlattenLogicer<Board> s = new Solver(b);
	    s.Solve(b);
        //s.testRecursion(b);


        //b.setCell(0,1,CellType.WALL);
        //StraightPathOutLogicStep spols = new StraightPathOutLogicStep(0,0,3);
        //System.out.println("SPOLS: " + spols.apply(b));
        //StraightPathOutLogicStep spols2 = new StraightPathOutLogicStep(0,2,2);
        //System.out.println("SPOLS2: " + spols2.apply(b));




	    System.out.println("# of Solutions: " + s.GetSolutions().size());

	    if (s.GetSolutions().size() == 1) {
	        b = s.GetSolutions().get(0);
	        Point p1 = b.getStart();
	        Point p2 = b.getEnd();

	        GridGraph gg = new GridGraph(new ShortestPathListener(b));
	        List<Point> path = gg.shortestPathBetween(p1,p2);
	        StringBuffer sb = new StringBuffer();
	        for (Point p : path) {
	            if (!b.hasLetter(p.x,p.y)) continue;
	            sb.append(LetterRotate.Rotate(b.getLetter(p.x,p.y),b.getAdjacentPaths(p.x,p.y)));
            }
            lines[0] = sb.toString();
	        lines[1] = b.getSolution();
        }

        GridFrame gf = new GridFrame("Adalogical Aenigma #84 Solver",1200,800,new MyListener(b,lines));

    }



}
