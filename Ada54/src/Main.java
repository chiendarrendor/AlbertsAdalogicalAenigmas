import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.GridPathContainer;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static class MyGridListener implements GridPanel.GridListener {
        Board b;
        String[] lines;

        public MyGridListener(Board b,String[] lines) { this.b = b; this.lines = lines; }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }
        public String getAnswerText() {
            StringBuffer sb = new StringBuffer();
            sb.append("<html><font size=\"5\">");
            Arrays.stream(lines).forEach(line->sb.append(line).append("<br>"));
            sb.append("</font></html>");
            return sb.toString();
        }

        public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            Color c = Color.WHITE;
            if (b.hasBlock(cx,cy)) c = Color.BLACK;
            if (!b.hasBlock(cx,cy) && b.isCellComplete(cx,cy)) c = Color.YELLOW;
            g.setColor(c);
            g.fillRect(0,0,bi.getWidth(),bi.getHeight());


            if (b.getArrow(cx,cy) != null) {
                GridPanel.DrawStringUpperLeftCell(bi,Color.MAGENTA,"" + b.getArrow(cx,cy).getSymbol());
            }

            if (b.hasLetter(cx,cy)) {
                GridPanel.DrawStringUpperLeftCell(bi, Color.BLACK,""+b.getLetter(cx,cy));
            }

            if (b.getStartPoint().x == cx && b.getStartPoint().y == cy) {
                g.setColor(Color.BLUE);
                g.drawOval(0,0,bi.getWidth(),bi.getHeight());
            }

            for (Direction d : Direction.orthogonals()) {
                EdgeInfo ei = b.getEdge(cx,cy,d);
                if (ei.isWall()) {
                    GridPanel.DrawStringInCorner(bi,Color.RED,"X",d);
                    continue;
                }

                c = Color.BLUE;
                if (ei.isUsed()) {
                    c = new Color(0x009f75);
                }
                if (ei.canGo(d)) GridPanel.DrawStringInCorner(bi,c,""+d.getSymbol(),d);
                if (ei.canGo(d.getOpp())) GridPanel.DrawStringInCorner(bi,c,""+d.getOpp().getSymbol(),d);
            }




            return true;
        }
    }


    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad command line, need filename");
	        System.exit(1);
        }


        Board b = new Board(args[0]);
	    Solver s = new Solver(b);

	    s.Solve(b);
	    System.out.println("Solution Count: " + s.GetSolutions().size());

        b = s.GetSolutions().get(0);

        Path p = b.getPaths().iterator().next();
        int sx = b.getStartPoint().x;
        int sy = b.getStartPoint().y;

        // check to see if we need to reverse the path
        Path.Cursor tcursor = p.getCursor(sx,sy);
        Direction sd = Direction.fromTo(tcursor.get().x,tcursor.get().y,tcursor.getNext().x,tcursor.getNext().y);
        EdgeInfo ei = b.getEdge(sx,sy,sd);
        if (ei.getSynopsis(sd) != EdgeSynopsis.USED_OUT) p.reverse();




        GridFileReader gfr = b.getReader();


        StringBuffer sbl = new StringBuffer();
        StringBuffer sbr = new StringBuffer();
        Path.Cursor cursor = p.getCursor(sx,sy);
        do {
            Point prev = cursor.getPrev();
            Point cur = cursor.get();
            Point next = cursor.getNext();

            if (Turns.makeTurn(prev,cur,next) == Turns.LEFT) {
                sbl.append(b.getLetter(cur.x,cur.y));
            }

            if (Turns.makeTurn(prev,cur,next) == Turns.RIGHT) {
                sbr.append(b.getLetter(cur.x,cur.y));
            }

            cursor.next();
        } while(cursor.get().x != sx || cursor.get().y != sy);

        String[] solutions = new String[4];
        solutions[0] = "Left Turns: " + sbl.toString();
        solutions[1] = "Right Turns: " + sbr.toString();
        solutions[2] = gfr.getVar("SOLUTION1");
        solutions[3] = gfr.getVar("SOLUTION2");

        GridFrame gf = new GridFrame(args[0] + " Solver",1500,950,new MyGridListener(b,solutions));
    }


}
