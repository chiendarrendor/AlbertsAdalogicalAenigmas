import com.sun.org.apache.regexp.internal.RE;
import grid.lambda.CellLambda;
import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import grid.spring.image.ArrowLine;
import javafx.geometry.Pos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class Main {

    private static final int INSET=5;
    private static final int ARROWLENGTH=15;
    private static final int DOTRADIUS=4;
    private static void drawTile(BufferedImage bi, PossibleDestination pd,Color tilecolor) {
        Graphics2D g = (Graphics2D)bi.getGraphics();
        g.setColor(tilecolor);
        g.draw(new RoundRectangle2D.Double(INSET,INSET,
                bi.getWidth()-2*INSET,bi.getHeight()-2*INSET,
                50,50));
        GridPanel.DrawStringInCorner(bi, Color.BLACK,""+pd.getLetter(), Direction.NORTHWEST);
        int cenx = bi.getWidth()/2;
        int ceny = bi.getHeight()/2;
        Point cenp = new Point(cenx,ceny);
        g.fillOval(cenx-DOTRADIUS,ceny-DOTRADIUS,2*DOTRADIUS,2*DOTRADIUS);

        for(Direction d: Direction.orthogonals()) {
            if (!pd.pointsTo(d)) continue;
            Point pointp = d.delta(cenx,ceny,ARROWLENGTH);
            ArrowLine.drawArrowLine(g,cenp,pointp,6,6);
        }
    }

    private static void lineFromCenter(BufferedImage bi,Direction d,boolean arrowed) {
        int cx = bi.getWidth()/2;
        int cy = bi.getHeight()/2;
        int ox,oy;

        Graphics g = bi.getGraphics();
        g.setColor(Color.BLACK);

        switch(d) {
            case NORTH: ox = cx; oy = 0; break;
            case SOUTH: ox = cx; oy = bi.getHeight(); break;
            case WEST: ox = 0; oy = cy; break;
            case EAST: ox = bi.getWidth() ; oy = cy; break;
            default: throw new RuntimeException("Invalid direction");
        }

        if (arrowed) {
            Point p1 = new Point(cx,cy);
            Point p2 = new Point(ox,oy);
            ArrowLine.drawArrowLine(g,p1,p2,10,10);
        } else {
            g.drawLine(cx,cy,ox,oy);
        }
    }





    private static class MyListener implements GridPanel.GridListener {
        String[] lines;
        Board b;

        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }


        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            // will have to be replaced with something more dynamic later
            //
            DestinationSet ds = b.getCellDestinations(cx,cy);
            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+ds.size(),Direction.SOUTHWEST);

            if (ds.size() == 0) {
                GridPanel.DrawStringInCell(bi,Color.RED,"?");
            } else if (ds.size() == 1) {
                PossibleDestination pd = ds.getOne();
                if (pd.isBlank()) {
                    Graphics2D g = (Graphics2D)bi.getGraphics();
                    g.setColor(Color.RED);
                    g.drawLine(INSET,INSET,bi.getWidth()-INSET,bi.getHeight()-INSET);
                    g.drawLine(INSET,bi.getHeight()-INSET,bi.getWidth()-INSET,INSET);
                } else if (pd.destx == cx && pd.desty == cy) {
                    drawTile(bi,pd,Color.GREEN);
                } else if (pd.sourcex == cx && pd.sourcey == cy) {
                    lineFromCenter(bi,pd.getMoveDirection(),pd.intermediates.size() == 0);
                } else {
                    // if we get here, we must be an intermediate
                    lineFromCenter(bi,pd.getMoveDirection().getOpp(),false);
                    Point lastp = pd.intermediates.get(pd.intermediates.size()-1);
                    boolean isLast = cx == lastp.x && cy == lastp.y;
                    lineFromCenter(bi,pd.getMoveDirection(),isLast);
                }
            } else if (b.hasTile(cx,cy)) {
                drawTile(bi,ds.getOne(),Color.BLACK);
            }





            return true;
        }


    }



    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        String[] lines = new String[] { "Adalogical Aenigma","#86 Solver" };
        Solver s = new Solver(b);



        s.Solve(b);





        System.out.println("# of Solutions: " + s.GetSolutions().size());
        if (s.GetSolutions().size() == 1) {
            b = s.GetSolutions().get(0);
            Board fb = b;

            CellContainer<PossibleDestination> destinations =
                    new CellContainer<PossibleDestination>(b.getWidth(),b.getHeight(),
                    (x,y)->{
                        PossibleDestination pd = fb.getCellDestinations(x,y).getOne();
                        if (pd.isBlank()) return null;
                        if (pd.destx == x && pd.desty == y) return pd;
                        return null;
                    }
            );

            StringBuffer sb = new StringBuffer();
            CellLambda.forEachCell(b.getWidth(),b.getHeight(),(x,y)->{
                if (destinations.getCell(x,y) == null) return;
                PossibleDestination pd = destinations.getCell(x,y);
                int minr = -1;
                int dsum = 0;
                for(Direction d : pd.tileDirections()) {
                    if (minr < 0)  {
                        for (int i = 1 ; ; ++i) {
                            Point op = d.delta(x,y,i);
                            if (destinations.getCell(op.x,op.y) == null) continue;
                            minr = i;
                            break;
                        }
                    }
                    Point op = d.delta(x,y,minr);
                    dsum += destinations.getCell(op.x,op.y).getMoveDistance();
                }
                sb.append(LetterRotate.Rotate(pd.getLetter(),dsum));
            });
            lines[0] = sb.toString();
            lines[1] = b.gfr.getVar("SOLUTION");
        }



        GridFrame gf = new GridFrame("Adalogical Aenigma #86 Solver",1800,1000,
                new MyListener(b,lines));
    }
}
