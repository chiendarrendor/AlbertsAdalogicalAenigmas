import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static class MyGridListener implements GridPanel.MultiGridListener {
        List<Board> blist;
        int curidx = 0;
        Board b;
        String[] lines;
        CellContainer<Integer> stretchcount;

        private void updateB() {
            b = blist.get(curidx);
        }


        public MyGridListener(List<Board> blist,String[] lines,CellContainer<Integer>stretchcount) {
            this.blist = blist;
            this.lines = lines;
            updateB();

            this.stretchcount = stretchcount;
        }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean hasNext() { return curidx < blist.size() - 1; }
        @Override public boolean hasPrev() { return curidx > 0; }
        @Override public void moveToNext() { ++curidx; updateB(); }
        @Override public void moveToPrev() { --curidx; updateB(); }




        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {

            for (Direction d : Direction.orthogonals()) {
                EdgeState es = b.getEdge(cx,cy,d);
                if (es == EdgeState.WALL) {
                    GridPanel.DrawStringInCorner(bi,Color.RED,"X",d);
                }
                if (es == EdgeState.PATH) {
                    Graphics2D g = (Graphics2D)bi.getGraphics();
                    g.setColor(Color.GREEN);
                    g.setStroke(new BasicStroke(5.0f));
                    int cex = bi.getWidth()/2;
                    int cey = bi.getHeight()/2;
                    switch(d) {
                        case NORTH: g.drawLine(cex,cey,cex,0); break;
                        case SOUTH: g.drawLine(cex,cey,cex,bi.getHeight()); break;
                        case WEST: g.drawLine(cex,cey,0,cey); break;
                        case EAST: g.drawLine(cex,cey,bi.getWidth(),cey); break;
                    }

                }
            }

            if (b.isMate(cx,cy)) {
                Graphics2D g = (Graphics2D)bi.getGraphics();
                g.setFont(g.getFont().deriveFont(40.0f));
                g.setColor(Color.BLACK);
                GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getHeight(),"♡");
            }

            if (b.isLover(cx,cy)) {
                Graphics2D g = (Graphics2D)bi.getGraphics();
                g.setColor(Color.BLACK);
                int INSET = 10;
                g.setStroke(new BasicStroke(3.0f));
                g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                g.setFont(g.getFont().deriveFont(20.0f));
                GridPanel.DrawStringInCell(g,0,0,bi.getWidth(),bi.getHeight(),""+b.getLoverNumber(cx,cy));
            }


            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringUpperLeftCell(bi, Color.BLACK,""+b.getLetter(cx,cy));
            }



            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+stretchcount.getCell(cx,cy),Direction.NORTHEAST);

            return true;
        }


    }



    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }
        Board b = new Board(args[0]);

        Solver s = new Solver(b);
        s.Solve(b);






        System.out.println("# of solutions: " + s.GetSolutions().size());
        String solstring = "unknown";
        String secondstring = "line 2";
        CellContainer<Integer> stretchcount = new CellContainer<Integer>(b.getWidth(), b.getHeight(), (x, y) -> -1);

        if (s.GetSolutions().size() == 1) {
            Board calb = s.GetSolutions().get(0);

            for (Path p : calb.getPaths()) {
                Path.Cursor curs = p.getCursor(p.endOne().x, p.endOne().y);
                int straightcount = 0;
                while (curs.hasNext()) {
                    if (curs.hasPrev()) {
                        if (Turns.isBend(Turns.makeTurn(curs.getPrev(), curs.get(), curs.getNext()))) {
                            stretchcount.setCell(curs.get().x, curs.get().y, 0);
                            for (int i = 0; i < straightcount; ++i) {
                                curs.prev();
                                stretchcount.setCell(curs.get().x, curs.get().y, straightcount);
                            }
                            for (int i = 0; i < straightcount; ++i) curs.next();

                            straightcount = 0;
                        } else {
                            ++straightcount;
                        }
                    }
                    curs.next();
                }
                // if we get here, curs is pointing to to the last element. if the last element was a bend, everything
                // is fine and straightcount will be zero.  if straightcount is non-zero, then we have a little more work to do.
                for (int i = 0 ; i < straightcount; ++i) {
                    curs.prev();
                    stretchcount.setCell(curs.get().x,curs.get().y,straightcount);
                }


            }

            StringBuffer solution = new StringBuffer();
            calb.forEachCell((x, y) -> {
                if (stretchcount.getCell(x, y) <= 0) return;
                solution.append(LetterRotate.Rotate(calb.getLetter(x, y), stretchcount.getCell(x, y)));
            });
            solstring = solution.toString();
            secondstring = calb.gfr.getVar("SOLUTION");
        }

        //if (s.GetSolutions().size() == 0) {
        //    System.out.println("No Solutions to show");
        //    System.exit(1);
        //}

        String[] solutions = new String[] { solstring, secondstring };

        GridFrame gf = new GridFrame("Adalogical Aenigma #64 Solver",1200,800,
                new MyGridListener(s.GetSolutions(),solutions,stretchcount));
    }
}
