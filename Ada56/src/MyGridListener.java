import grid.assistant.BoardHolder;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class MyGridListener implements GridPanel.GridListener {
    BoardHolder<Board> bh;
    String[] lines;

    private Board b() { return bh.getBoard(); }

    public MyGridListener(BoardHolder<Board> bh) { this(bh,null);}
    public MyGridListener(BoardHolder<Board> bh,String[] lines) {
        this.bh = bh; this.lines = lines;
    }
    public int getNumXCells() { return b().getWidth(); }
    public int getNumYCells() { return b().getHeight(); }
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
        Graphics2D g = (Graphics2D) bi.getGraphics();

        CellType ct = b().getCell(cx,cy);
        if (ct != CellType.UNKNOWN) {
            Color c = ct == CellType.WALL ? Color.DARK_GRAY : Color.GREEN;
            g.setColor(c);
            g.fillRect(0,0,bi.getWidth(),bi.getHeight());
        }



        if (b().getShape(cx,cy) == CellShape.TRIANGLE) Main.drawCharInCell(bi,Color.BLACK,'△',20,false);
        if (b().getShape(cx,cy) == CellShape.CIRCLE) Main.drawCharInCell(bi,Color.BLACK,'◯',20,false);
        if (b().isStart(cx,cy)) Main.drawCharInCell(bi,Color.BLACK,'S',20,true);
        if (b().isEnd(cx,cy)) Main.drawCharInCell(bi,Color.BLACK,'G',20,true);
        if (b().isOnPath(cx,cy)) Main.drawCharInCell(bi,Color.RED,'O',20,false);
        if (b().notOnPath(cx,cy)) Main.drawCharInCell(bi,Color.RED,'X',20,false);

        if (b().hasLetter(cx,cy)) {
            GridPanel.DrawStringUpperLeftCell(bi, Color.GRAY,""+b().getLetter(cx,cy));
        }


        return true;
    }
}
