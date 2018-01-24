import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Addenda9
{
    private static class MyGridListener implements GridPanel.GridListener {
        private Board b;
        public MyGridListener(Board b) { this.b = b;  }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }
        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            switch(b.getCell(cx,cy))
            {
                case ONPATH: g.setColor(new Color(152,251,152)); g.fillRect(0,0,bi.getWidth(),bi.getHeight()); break;
                case NOTPATH: g.setColor(new Color(255,182,193)); g.fillRect(0,0,bi.getWidth(),bi.getHeight()); break;
                case UNKNOWN: break;
            }

            int INSET=15;
            if (b.getCellColor(cx,cy) == CellColor.BLACK)
            {
                g.setColor(Color.BLACK);
                g.fillOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
            }

            if (b.getCellColor(cx,cy) == CellColor.WHITE)
            {
                g.setColor(Color.WHITE);
                g.fillOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(5));
                g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
            }

            if (b.getCellColor(cx,cy) == CellColor.UNKNOWN)
            {
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(5));
                g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
            }




            char c = b.getLetter(cx,cy);
            if (c != '.') GridPanel.DrawStringInCorner(bi,Color.BLACK,""+c, Direction.NORTHWEST);


            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(1));
            if (b.getEdge(cx,cy,Direction.NORTH) == EdgeType.NOTPATH) g.drawLine(0,1,bi.getWidth(),1);
            if (b.getEdge(cx,cy,Direction.SOUTH) == EdgeType.NOTPATH) g.drawLine(0,bi.getHeight()-2,bi.getWidth(),bi.getHeight()-2);
            if (b.getEdge(cx,cy,Direction.WEST) == EdgeType.NOTPATH) g.drawLine(1,0,1,bi.getHeight());
            if (b.getEdge(cx,cy,Direction.EAST) == EdgeType.NOTPATH) g.drawLine(bi.getWidth()-2,0,bi.getWidth()-2,bi.getHeight());

            g.setColor(Color.GREEN);
            g.setStroke(new BasicStroke(5));
            int cenx = bi.getWidth()/2;
            int ceny = bi.getHeight()/2;
            if (b.getEdge(cx,cy,Direction.NORTH) == EdgeType.PATH) g.drawLine(cenx,ceny,cenx,0);
            if (b.getEdge(cx,cy,Direction.SOUTH) == EdgeType.PATH) g.drawLine(cenx,ceny,cenx,bi.getHeight());
            if (b.getEdge(cx,cy,Direction.WEST) == EdgeType.PATH) g.drawLine(cenx,ceny,0,ceny);
            if (b.getEdge(cx,cy,Direction.EAST) == EdgeType.PATH) g.drawLine(cenx,ceny,bi.getWidth(),ceny);



            return true;
        }
    }


    private static void twoStep(Board b,int x,int y,Direction d)
    {
        b.setEdge(x,y,d,EdgeType.PATH);
        b.setEdge(x+d.DX(),y+d.DY(),d,EdgeType.PATH);
    }

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Bad command line");
            System.exit(1);
        }
        Board b = new Board(args[0]);

        Solver s = new Solver(b);

//        int loop = 1;
//        while(true)
//        {
//            System.out.println("Loop #: " + loop);
//            FlattenLogicer.RecursionStatus rs1 = s.recursiveApplyLogic(b);
//            System.out.println("rs1: " + rs1);
//            if (rs1 != FlattenLogicer.RecursionStatus.GO) break;
//
//            LogicStatus ls = s.applyTupleSuccessors(b);
//            System.out.println("ls: " + ls);
//            if (ls == LogicStatus.CONTRADICTION) break;
//
//            FlattenLogicer.RecursionStatus rs2 = s.recursiveApplyLogic(b);
//            System.out.println("rs2: " + rs2);
//            if (rs2 != FlattenLogicer.RecursionStatus.GO) break;
//
//            if (ls == LogicStatus.STYMIED) break;
//            ++loop;
//        }

        s.Solve(b);
        System.out.println("# of Solutions: " + s.GetSolutions().size());
        b = s.GetSolutions().get(0);
        Path p = b.gpc.iterator().next();
        System.out.println("Path size: " + p.size());
        Path.Cursor pc = p.getCursor(0,0);
        for (int i = 0 ; i < p.size(); ++i)
        {
            Point prev = pc.getPrev();
            Point cur = pc.get();
            Point next = pc.getNext();
            pc.next();

            if (Turns.makeTurn(prev,cur,next) != Turns.STRAIGHT) continue;
            if (b.getLetter(cur.x,cur.y) != '.') System.out.print(b.getLetter(cur.x,cur.y));
        }
        System.out.println("");



        GridFrame gf = new GridFrame("Addenda #9 Solver",1200,800,new MyGridListener(b));


    }
}
