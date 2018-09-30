import grid.assistant.BoardHolder;
import grid.puzzlebits.Direction;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class MyGridListener implements GridPanel.GridListener {
    private BoardHolder<Board> holder;
    private String[] lines;
    private BufferedImage evenbi;
    private BufferedImage oddbi;
    private Rectangle anchor;


    public MyGridListener(BoardHolder<Board> holder, String[] lines) {
        this.holder = holder; this.lines = lines;

        anchor = new Rectangle(0,0,5,5);

        evenbi = new BufferedImage(5,5,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)evenbi.getGraphics();
        g.setColor(new Color(0,0,0,0));
        g.fillRect(0,0, evenbi.getWidth(),evenbi.getHeight());
        g.setColor(new Color(0xff,0,0,255));
        g.drawLine(0,0, evenbi.getWidth(), evenbi.getHeight());

        oddbi = new BufferedImage(5,5,BufferedImage.TYPE_INT_ARGB);
        g = (Graphics2D)oddbi.getGraphics();
        g.setColor(new Color(0,0,0,0));
        g.fillRect(0,0, oddbi.getWidth(),oddbi.getHeight());
        g.setColor(new Color(0x00,0xff,0,255));
        g.drawLine(oddbi.getWidth(),0, 0, oddbi.getHeight());

    }
    private Board b() { return holder.getBoard(); }

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
        List<Rectangle> boxes = b().getBoxes();
        Graphics2D g = (Graphics2D)bi.getGraphics();
        Cell c = b().getCell(cx,cy);

        if (c == null) {
            g.setColor(Color.BLACK);
            //g.fillRect(0,0,bi.getWidth(),bi.getHeight());
        } else {
            boolean iseven = false;
            for (int i = 0; i < boxes.size(); i += 2) {
                if (boxes.get(i).contains(cx, cy)) iseven = true;
            }
            boolean isodd = false;
            for (int i = 1; i < boxes.size(); i += 2) {
                if (boxes.get(i).contains(cx, cy)) isodd = true;
            }

            if (iseven) {
                g.setPaint(new TexturePaint(evenbi, anchor));
                g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
            }

            if (isodd) {
                g.setPaint(new TexturePaint(oddbi, anchor));
                g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
            }
        }

        Font origfont = g.getFont();

        g.setFont(g.getFont().deriveFont(Font.BOLD));

        String hclue = b().getHorizontalClue(cx,cy);
        String vclue = b().getVerticalClue(cx,cy);
        String clue = vclue + "/" + hclue;
        if (!clue.equals("./.")) {
            GridPanel.DrawStringInCorner(g,Color.BLUE,0,0,bi.getWidth(),bi.getHeight(),clue,Direction.NORTH);
        }

        if (c == null) return true;

        GridPanel.DrawStringUpperLeftCell(bi,Color.GRAY,""+b().getLetter(cx,cy));

        if (c.isWall()) {
            g.setFont(origfont.deriveFont(Font.BOLD).deriveFont(24.0f));
            GridPanel.DrawStringInCell(g, Color.BLACK,0,0,bi.getWidth(),bi.getHeight(),"W");
        } else if (c.isComplete()) {
            g.setFont(origfont.deriveFont(Font.BOLD).deriveFont(24.0f));
            GridPanel.DrawStringInCell(g, Color.BLUE,0,0,bi.getWidth(),bi.getHeight(),"" + c.getSingleNumber());
        } else {
            if (c.contains(Cell.WALLID)) GridPanel.DrawStringInCorner(bi, Color.BLACK, "W", Direction.NORTHEAST);
            if (c.contains(1)) GridPanel.DrawStringInCorner(bi, Color.BLACK, "1", Direction.WEST);
            if (c.contains(2)) GridPanel.DrawStringInCell(bi, Color.BLACK, "2");
            if (c.contains(3)) GridPanel.DrawStringInCorner(bi, Color.BLACK, "3", Direction.EAST);
            if (c.contains(4)) GridPanel.DrawStringInCorner(bi, Color.BLACK, "4", Direction.SOUTHWEST);
            if (c.contains(5)) GridPanel.DrawStringInCorner(bi, Color.BLACK, "5", Direction.SOUTH);
            if (c.contains(6)) GridPanel.DrawStringInCorner(bi, Color.BLACK, "6", Direction.SOUTHEAST);
        }





        return true;
    }

}
