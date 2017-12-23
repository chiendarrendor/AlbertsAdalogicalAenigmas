import grid.file.GridFileReader;
import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

public class Ada49
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

        private String clueString(int clue)
        {
            if (clue == -1) return "?";
            return Integer.toString(clue);
        }

        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.getCell(cx,cy) == CellState.BLACK)
            {
                g.setColor(Color.BLUE);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            if (b.getCell(cx,cy) == CellState.WHITE)
            {
                g.setColor(Color.YELLOW);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            char c = b.getLetter(cx,cy);
            if (c != '.')  GridPanel.DrawStringUpperLeftCell(bi, Color.gray,""+c);
            Clues.VInt vi = b.clues.clues[cx][cy];
            if (vi == null) return true;

            Font curFont = g.getFont();
            Font newFont = curFont.deriveFont(Font.BOLD,curFont.getSize() * 2.0F);
            g.setFont(newFont);

            if (vi.size() == 1)
            {
                GridPanel.DrawStringInCell(g,Color.black,0,0,bi.getWidth(),bi.getHeight(),
                        clueString(vi.firstElement()));
            }
            if (vi.size() == 2)
            {
                GridPanel.DrawStringInCorner(g,Color.black,0,0,bi.getWidth(),bi.getHeight(),
                        clueString(vi.firstElement()),Direction.NORTHWEST);
                GridPanel.DrawStringInCorner(g,Color.black,0,0,bi.getWidth(),bi.getHeight(),
                        clueString(vi.lastElement()),Direction.SOUTHEAST);
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
        System.out.println("# solutions: " + s.GetSolutions().size());

        ClueUnrolled[] singles = new ClueUnrolled[9];
        for (int i = 1 ; i <= 8 ; ++i)
        {
            Vector<Integer> v = new Vector<>();
            v.add(i);
            singles[i] = new ClueUnrolled(v);
        }

        b.forEachCell((x,y)->{
            if (b.getCell(x,y) == CellState.BLACK || b.clues.clues[x][y] != null) return;
            StringBuffer sb = new StringBuffer();
            for (Direction d : Direction.values())
            {
                int nx = x + d.DX();
                int ny = y + d.DY();
                if (b.onBoard(nx, ny) && b.getCell(nx, ny) == CellState.BLACK) sb.append('B');
                else sb.append('W');
            }

            for (int i = 1 ; i <= 8 ; ++i)
            {
                if (singles[i].matches(sb.toString()))
                {
                    System.out.print(LetterRotate.Rotate(b.getLetter(x,y),i));
                    return;
                }
            }
        });

        System.out.println("");



        GridFrame gf = new GridFrame("Ada49 Solver",1200,800,new MyGridListener(b));


    }


}
