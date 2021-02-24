import grid.puzzlebits.Direction;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

class MyListener implements GridPanel.GridListener
{
    Board b;
    String[] lines;

    public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines;}


    public int getNumXCells()
    {
        return b.getWidth();
    }
    public int getNumYCells()
    {
        return b.getHeight();
    }
    public boolean drawGridNumbers()
    {
        return true;
    }
    public boolean drawGridLines()
    {
        return true;
    }
    public boolean drawBoundary()
    {
        return true;
    }
    public String[] getAnswerLines() { return lines; }


    public boolean drawCellContents(int cx, int cy, BufferedImage bi)
    {
        Graphics2D g = (Graphics2D)bi.getGraphics();
        if(b.hasLetter(cx,cy))  GridPanel.DrawStringInCorner(bi, Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(5));
        int cenx = bi.getWidth()/2;
        int ceny = bi.getHeight()/2;

        if (b.hasPath(cx,cy,Direction.NORTH)) g.drawLine(cenx,ceny,cenx,0);
        if (b.hasPath(cx,cy,Direction.SOUTH)) g.drawLine(cenx,ceny,cenx,bi.getHeight());
        if (b.hasPath(cx,cy,Direction.WEST)) g.drawLine(cenx,ceny,0,ceny);
        if (b.hasPath(cx,cy,Direction.EAST)) g.drawLine(cenx,ceny,bi.getWidth(),ceny);

        g.setColor(Color.red);
        g.setStroke(new BasicStroke(1));
        int INSET = 5;
        int unxi = bi.getWidth() - INSET;
        int unyi = bi.getHeight() - INSET;
        if (b.hasWall(cx,cy,Direction.NORTH)) g.drawLine(INSET,INSET,unxi,INSET);
        if (b.hasWall(cx,cy,Direction.SOUTH)) g.drawLine(INSET,unyi,unxi,unyi);
        if (b.hasWall(cx,cy,Direction.EAST)) g.drawLine(unxi,INSET,unxi,unyi);
        if (b.hasWall(cx,cy,Direction.WEST)) g.drawLine(INSET,INSET,INSET,unyi);


        if (b.getSolverID() == 74) {
            if (b.hasNumericClue(cx,cy)) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getNumericClue(cx,cy),Direction.SOUTHEAST);
            }
        }



        return true;
    }
}
