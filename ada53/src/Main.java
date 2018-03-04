import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Main {

    private static class MyGridListener implements GridPanel.GridListener
    {
        private final Board b;

        public MyGridListener(Board b) { this.b = b;}
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        public final int INSET=10;

        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (b.hasLetter(cx,cy)) { GridPanel.DrawStringInCorner(bi, Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST); }

            CellState cs = b.getCellState(cx,cy);
            switch(cs) {
                case INITIAL:
                    int cn = b.getCircleNumber(cx,cy);
                    if (cn != -1) GridPanel.DrawStringInCell(bi,Color.BLACK,""+cn);
                    g.setColor(Color.GREEN);
                    g.setStroke(new BasicStroke(5));
                    g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                    break;
                case TERMINAL:
                    Path p = b.getSetPath(cx,cy);
                    int tcn = p.getDist();
                    if (tcn != -1) GridPanel.DrawStringInCell(bi,Color.BLACK,""+tcn);
                    g.setColor(Color.RED);
                    g.setStroke(new BasicStroke(5));
                    g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                    break;
                case PATH:
                    Path pp = b.getSetPath(cx,cy);
                    if (pp.initial.x == cx && pp.initial.y == cy) {
                        g.setStroke(new BasicStroke(2.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,10.0f,new float[]{5.0f,5.0f},0.0f));
                        g.setColor(Color.GREEN);
                        g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                    } else {
                        g.setColor(Color.yellow);
                        g.fillOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                    }


                    GridPanel.DrawStringInCell(bi,Color.BLACK,""+pp.dir.getSymbol());

            }




            return true;
        }
    }

    private static Path pathWithTerminal(Circle c,int x,int y) {
        return c.paths.stream().filter((p)->p.terminal.x == x && p.terminal.y == y).findFirst().get();
    }


    public static void main(String[] args) {

        if (args.length != 1) {
            throw new RuntimeException("Bad Command Line");
        }
        Board b = new Board(args[0]);

        Solver s = new Solver(b);
        s.Solve(b);

        //s.recursiveApplyLogic(b);
        //s.applyTupleSuccessors(b);
        //s.recursiveApplyLogic(b);


        System.out.println("# of Solutions: " + s.GetSolutions().size());
        if (s.GetSolutions().size() > 0) b = s.GetSolutions().get(0);

        final Board fb = b;
        StringBuffer sb = new StringBuffer();
        b.forEachCell((x,y)->{
            Path p = fb.getSetPath(x,y);
            if (p == null) return;
            if (x == p.initial.x && y == p.initial.y) return;
            if (x == p.terminal.x && y == p.terminal.y) return;
            if (!fb.hasLetter(x,y)) return;
            sb.append(LetterRotate.Rotate(fb.getLetter(x,y).charAt(0),p.getLength()));
        });

        System.out.println("result: " + sb.toString());


	    GridFrame gf = new GridFrame("Adalogical Aenigma #53",1000,800,new MyGridListener(b));
    }


}
