import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Ada52
{
    private static class MyGridListener implements GridPanel.GridListener
    {
        Board b;
        public MyGridListener(Board b) { this.b = b; }
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            CellType ct = b.getCell(cx,cy);
            Graphics2D g = (Graphics2D)bi.getGraphics();
            switch(ct)
            {
                case WHITE:
                    g.setColor(Color.WHITE);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                    break;
                case GREY:
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                    break;
                case NUMBER:
                    g.setColor(Color.BLACK);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                    GridPanel.DrawStringInCell(bi,Color.WHITE,""+b.getNumber(cx,cy));
                    break;
                case OFFBOARD:
                    g.setColor(Color.BLACK);
                    g.drawLine(0,0,bi.getWidth(),bi.getHeight());
                    g.drawLine( 0,bi.getHeight(),bi.getWidth(),0);
                    break;
            }

            CellNumbers cn = b.getWorkBlock(cx,cy);
            if (cn != null)
            {
                Color c = Color.blue;
                if (!cn.isValid()) c = Color.red;
                else if (cn.isDone()) c = b.getCell(cx,cy) == CellType.WHITE ? new Color(0,102,51) : Color.GREEN;
                GridPanel.DrawStringInCell(bi,c,cn.toString());
            }



            return true;
        }
    }



    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("Bad Command Line");
            System.exit(1);
        }
        Board b = new Board(args[0]);
        Solver s = new Solver(b);
        s.Solve(b);

        System.out.println("# of Solutions: " + s.GetSolutions().size());

        b = s.GetSolutions().get(0);
        for (int x = 0 ; x < b.getWidth() ; ++x)
        {
            int gs = 0;
            for (int y = 0 ; y < b.getHeight() ; ++y )
            {
                if (b.getCell(x,y) != CellType.GREY) continue;
                gs += b.getWorkBlock(x,y).doneNumber();
            }
            System.out.print(LetterRotate.Rotate(b.gfr.getVar("LETTERS").charAt(x),gs));
        }
        System.out.println("");




        GridFrame gf = new GridFrame("Ada52 Solver",1200,800,new MyGridListener(b));
    }
}
