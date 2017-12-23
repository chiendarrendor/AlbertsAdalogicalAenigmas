import grid.letter.LetterRotate;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class Ada22
{
    public static class MyListener implements GridPanel.GridListener
    {
        private Board b;
        public MyListener(Board b)
        {
            this.b = b;
        }

        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        public boolean drawCellContents(int cx,int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            Color textColor = Color.black;

            if (b.getCell(cx,cy) == CellState.BLACK)
            {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                textColor = Color.WHITE;
            }
            if (b.getCell(cx,cy) == CellState.WHITE)
            {
                g.setColor(Color.WHITE);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.hasLetter(cx,cy))
            {
                GridPanel.DrawStringInCell(bi,textColor,""+b.getLetter(cx,cy));
            }

            Clue clue = b.getClue(cx,cy);
            if (clue != null)
            {
                GridPanel.DrawStringInCell(bi,textColor,""+clue.getDist() + clue.getDir().getArrow());
            }


            return true;
        }
    }

    private static int adjacentBlack(Board b,int x,int y)
    {
        List<Point> adjs = b.adjacents(x,y);
        int result = 0;
        for (Point p : adjs)
        {
            if (b.getCell(p.x,p.y) == CellState.BLACK) ++result;
        }
        return result;
    }

    public static void main(String[] args)
    {

        if (args.length != 1)
        {
            System.out.println("Bad command line");
            System.exit(1);
        }
        Board b = new Board(args[0]);
        FlattenSolver s = new FlattenSolver();

        s.Solve(b);

        b = s.GetSolutions().get(0);

        StringBuffer one = new StringBuffer();
        StringBuffer two = new StringBuffer();
        StringBuffer three = new StringBuffer();

        for (int y = 0 ; y < b.getHeight() ; ++y)
        {
            for (int x = 0 ; x < b.getWidth() ; ++x)
            {
                if (b.getCell(x,y) == CellState.BLACK) continue;
                if (!b.hasLetter(x,y)) continue;
                int ab = adjacentBlack(b,x,y);
                if (ab == 1) one.append(LetterRotate.Rotate(b.getLetter(x,y),1));
                if (ab == 2) two.append(LetterRotate.Rotate(b.getLetter(x,y),2));
                if (ab == 3) three.append(LetterRotate.Rotate(b.getLetter(x,y),3));
            }
        }
        System.out.println("Answer: " + one.toString());
        System.out.println("Twos? " + two.toString());
        System.out.println("Threes? " + three.toString());


        new GridFrame("Adalogical Aenigma #22", 1300, 768,
                new MyListener(b));
    }
}
