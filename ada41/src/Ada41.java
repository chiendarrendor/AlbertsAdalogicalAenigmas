import grid.file.GridFileReader;
import grid.logic.LogicStatus;
import grid.spring.GridPanel;
import grid.spring.HorizontalCenterPanel;
import grid.spring.SinglePanelFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Ada41
{
    private static Color[] depthColors = {
            Color.green,
            Color.blue,
            Color.red,
            Color.pink,
            Color.cyan
    };



    public static class MyGridListener implements GridPanel.GridListener
    {
        Board b;
        String lines[];
        public MyGridListener(Board b, String[] lines ) { this.b = b; this.lines = lines;}
        public int getNumXCells() { return b.getWidth(); }
        public int getNumYCells() { return b.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }
        public String[] getAnswerLines() { return lines; }

        // draws an arrow in the given direction from the given start location.
        public void DrawArrow(int length,int height,int x,int y,Board.ArrowDir dir,Graphics2D g)
        {
            // create a arrow pointing right
            Polygon arrow = new Polygon();
            arrow.addPoint(0,0);
            arrow.addPoint(length-height,0);
            arrow.addPoint(length-height,-height);
            arrow.addPoint(length,0);
            arrow.addPoint(length-height,height);
            arrow.addPoint(length-height,1);
            arrow.addPoint(0,1);
            arrow.addPoint(0,0);


            AffineTransform at = new AffineTransform();
            at.translate(x,y);
            switch(dir)
            {
                case EAST: break;
                case SOUTH: at.quadrantRotate(1,0,0); break;
                case WEST: at.quadrantRotate(2,0,0); break;
                case NORTH: at.quadrantRotate(3,0,0); break;
            }

            Shape s = at.createTransformedShape(arrow);

            g.fill(s);
        }



        public boolean drawCellContents(int cx, int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b.hasLetter(cx,cy)) { GridPanel.DrawStringUpperLeftCell(bi,Color.black, ""+b.getLetter(cx,cy)); }
            if (b.getCellType(cx,cy) != Board.CellType.EMPTY)
            {
                g.setColor(b.getCellType(cx,cy).getColor());
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());

                Board.ArrowInfo ai = b.getArrowInfo(cx,cy);
                int INSET = 5;

                if (ai != null)
                {
                    Color textColor = b.getCellType(cx,cy) != Board.CellType.WHITE ? Color.white : Color.black;
                    if (!b.validateArrow(cx,cy)) textColor = Color.red;

                    g.setColor(textColor);

                    switch(ai.dir)
                    {
                        case NORTH:
                            DrawArrow(bi.getHeight()-INSET*2, INSET,bi.getWidth()-INSET,bi.getHeight()-INSET, Board.ArrowDir.NORTH,g);
                            break;
                        case SOUTH:
                            DrawArrow(bi.getHeight()-INSET*2, INSET,bi.getWidth()-INSET,INSET, Board.ArrowDir.SOUTH,g);
                            break;
                        case EAST:
                            DrawArrow(bi.getWidth()-INSET*2,INSET,INSET,INSET, Board.ArrowDir.EAST,g);
                            break;
                        case WEST:
                            DrawArrow(bi.getWidth()-INSET*2,INSET,bi.getWidth()-INSET,INSET, Board.ArrowDir.WEST,g);
                            break;
                    }
                    GridPanel.DrawStringInCell(bi,textColor,""+ai.count);
                }
            }
            else
            {

                int QINSET=8;
                if (b.getULQuadCell(cx,cy) != Board.CellType.EMPTY)
                {
                    g.setColor(b.getULQuadCell(cx,cy).getColor());
                    g.fillRect(0+QINSET,0+QINSET,bi.getWidth()/2-2*QINSET,bi.getHeight()/2-2*QINSET);
                }
                if (b.getURQuadCell(cx,cy) != Board.CellType.EMPTY)
                {
                    g.setColor(b.getURQuadCell(cx,cy).getColor());
                    g.fillRect(bi.getWidth()/2+QINSET,0+QINSET,bi.getWidth()/2-2*QINSET,bi.getHeight()/2-2*QINSET);
                }
                if (b.getLLQuadCell(cx,cy) != Board.CellType.EMPTY)
                {
                    g.setColor(b.getLLQuadCell(cx,cy).getColor());
                    g.fillRect(0+QINSET,bi.getHeight()/2+QINSET,bi.getWidth()/2-2*QINSET,bi.getHeight()/2-2*QINSET);
                }
                if (b.getLRQuadCell(cx,cy) != Board.CellType.EMPTY)
                {
                    g.setColor(b.getLRQuadCell(cx,cy).getColor());
                    g.fillRect(bi.getWidth()/2+QINSET,bi.getHeight()/2+QINSET,bi.getWidth()/2-2*QINSET,bi.getHeight()/2-2*QINSET);
                }


                g.setStroke(new BasicStroke(5));
                Board.EdgeInfo ei = b.getEastEdgeInfo(cx, cy);
                if (ei != null && ei.getEdgeStatus() == Board.EdgeStatus.PATH)
                {
                    g.setColor(depthColors[ei.depth]);
                    g.drawLine(bi.getWidth() / 2, bi.getHeight() / 2, bi.getWidth(), bi.getHeight() / 2);
                }

                ei = b.getWestEdgeInfo(cx, cy);
                if (ei != null && ei.getEdgeStatus() == Board.EdgeStatus.PATH)
                {
                    g.setColor(depthColors[ei.depth]);
                    g.drawLine(bi.getWidth() / 2, bi.getHeight() / 2, 0, bi.getHeight() / 2);
                }

                ei = b.getNorthEdgeInfo(cx, cy);
                if (ei != null && ei.getEdgeStatus() == Board.EdgeStatus.PATH)
                {
                    g.setColor(depthColors[ei.depth]);
                    g.drawLine(bi.getWidth() / 2, bi.getHeight() / 2, bi.getWidth() / 2, 0);
                }

                ei = b.getSouthEdgeInfo(cx, cy);
                if (ei != null && ei.getEdgeStatus() == Board.EdgeStatus.PATH)
                {
                    g.setColor(depthColors[ei.depth]);
                    g.drawLine(bi.getWidth() / 2, bi.getHeight() / 2, bi.getWidth() / 2, bi.getHeight());
                }
            }
            return true;
        }
    }

    public static class MyEdgeListener implements GridPanel.EdgeListener
    {
        Board b;
        public MyEdgeListener(Board b) { this.b = b; }


        private boolean isBarrierEdge(int x1,int y1,int x2,int y2)
        {
            Board.CellType thisCell = b.getCellType(x1,y1);
            Board.CellType otherCell = b.getCellType(x2,y2);
            return thisCell != otherCell;
        }

        public EdgeDescriptor onBoundary() { return new EdgeDescriptor(Color.BLACK,5); }

        public EdgeDescriptor toEast(int x, int y)
        {
            EdgeDescriptor result = new EdgeDescriptor(Color.black,1);

            if (isBarrierEdge(x,y,x+1,y))
            {
                return new EdgeDescriptor(Color.black, 5);
            }

            if (b.getCellType(x,y) != Board.CellType.EMPTY) return result;
            Board.EdgeInfo ei = b.getEastEdgeInfo(x,y);
            if (ei.getEdgeStatus() == Board.EdgeStatus.WALL) result = new EdgeDescriptor(depthColors[ei.depth],3);

            return result;
        }

        public EdgeDescriptor toSouth(int x, int y)
        {
            EdgeDescriptor result = new EdgeDescriptor(Color.black,1);

            if (isBarrierEdge(x,y,x,y+1))
            {
                return new EdgeDescriptor(Color.black, 5);
            }

            if (b.getCellType(x,y) != Board.CellType.EMPTY) return result;
            Board.EdgeInfo ei = b.getSouthEdgeInfo(x,y);
            if (ei.getEdgeStatus() == Board.EdgeStatus.WALL) result = new EdgeDescriptor(depthColors[ei.depth],3);

            return result;        }
    }

    private static class BoardClickListener extends MouseAdapter
    {
        Board b;
        GridPanel gp;
        public BoardClickListener(Board b,GridPanel gp) { this.b = b ; this.gp = gp; }

        int accuracy = 10;
        enum LocType {SMALL,CENTER,LARGE};

        public void mouseReleased(MouseEvent mev)
        {
            int button = mev.getButton();
            if (button != 1 && button != 3) return;
            GridPanel.DrawParams params = gp.getParams();
            int relx = mev.getX() - params.INSET;
            int rely = mev.getY() - params.INSET;

            int cellx = relx / params.cellWidth;
            int celly = rely / params.cellHeight;
            int inx = relx % params.cellWidth;
            int iny = rely % params.cellHeight;

            LocType xloc;
            if (inx < accuracy) xloc = LocType.SMALL;
            else if (inx < params.cellWidth-accuracy) xloc = LocType.CENTER;
            else xloc = LocType.LARGE;

            LocType yloc;
            if (iny < accuracy) yloc = LocType.SMALL;
            else if (iny < params.cellHeight - accuracy) yloc = LocType.CENTER;
            else yloc = LocType.LARGE;

            if (xloc == LocType.SMALL && yloc == LocType.CENTER)
            {
                Board.EdgeInfo ei = b.getWestEdgeInfo(cellx,celly);
                if (ei == null) return;
                if (ei.getEdgeStatus() != Board.EdgeStatus.UNKNOWN) return;
                ei.setEdgeStatus(button == 1 ? Board.EdgeStatus.PATH : Board.EdgeStatus.WALL);
                params.repaint();
                return;
            }
            if (xloc == LocType.LARGE && yloc == LocType.CENTER)
            {
                Board.EdgeInfo ei = b.getEastEdgeInfo(cellx,celly);
                if (ei == null) return;
                if (ei.getEdgeStatus() != Board.EdgeStatus.UNKNOWN) return;
                ei.setEdgeStatus(button == 1 ? Board.EdgeStatus.PATH : Board.EdgeStatus.WALL);
                params.repaint();
                return;
            }
            if (xloc == LocType.CENTER && yloc == LocType.SMALL)
            {
                Board.EdgeInfo ei = b.getNorthEdgeInfo(cellx,celly);
                if (ei == null) return;
                if (ei.getEdgeStatus() != Board.EdgeStatus.UNKNOWN) return;
                ei.setEdgeStatus(button == 1 ? Board.EdgeStatus.PATH : Board.EdgeStatus.WALL);
                params.repaint();
                return;
            }
            if (xloc == LocType.CENTER && yloc == LocType.LARGE)
            {
                Board.EdgeInfo ei = b.getSouthEdgeInfo(cellx,celly);
                if (ei == null) return;
                if (ei.getEdgeStatus() != Board.EdgeStatus.UNKNOWN) return;
                ei.setEdgeStatus( button == 1 ? Board.EdgeStatus.PATH : Board.EdgeStatus.WALL);
                params.repaint();
                return;
            }


        }
    }

    private static class BackButtonListener implements ActionListener
    {
        Board b;
        GridPanel gp;
        public BackButtonListener(Board b,GridPanel gp) { this.b = b; this.gp = gp; }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (b.isCurDepthEmpty()) return;
            b.backUp();
            gp.repaint();
        }
    }

    private static class GuessButtonListener implements ActionListener
    {
        Board b;
        public GuessButtonListener(Board b) {this.b = b;}

        @Override public void actionPerformed(ActionEvent e)
        {
            b.increaseDepth();
        }
    }

    private static class UnGuessButtonListener implements ActionListener
    {
        Board b;
        GridPanel gp;
        public UnGuessButtonListener(Board b,GridPanel gp) { this.b = b; this.gp = gp; }

        @Override public void actionPerformed(ActionEvent e)
        {
            b.decreaseDepth();
            gp.repaint();
        }
    }

    private static class AnalyzePathButtonListener implements ActionListener
    {
        Board b;
        public AnalyzePathButtonListener(Board b) { this.b = b; }
        @Override public void actionPerformed(ActionEvent e)
        {
            Point p = b.getStartPoint();
            b.walkPath(p.x,p.y, Board.ArrowDir.EAST);
        }
    }


    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        GridFileReader gfr = new GridFileReader(args[0]);

        Board b = new Board(gfr);
        LogicStatus lstat = b.scanQuads();
        if (lstat == LogicStatus.CONTRADICTION)
        {
            System.out.println("No Solution");
            System.exit(1);
        }

        String[] lines = new String[] { "Adalogical Aenigma", "#41" , "Solver" };


        MyGridListener mgl = new MyGridListener(b,lines);
        MyEdgeListener mel = new MyEdgeListener(b);
        GridPanel gp = new GridPanel(1300,800,mgl,mel);
        gp.addMouseListener(new BoardClickListener(b,gp));

        JPanel jp = new JPanel(new BorderLayout());
        jp.add(gp,BorderLayout.CENTER);

        JButton backButton = new JButton("undo");
        backButton.addActionListener(new BackButtonListener(b,gp));

        JButton guessButton = new JButton("guess");
        guessButton.addActionListener(new GuessButtonListener(b));

        JButton collapseButton = new JButton("unguess");
        collapseButton.addActionListener(new UnGuessButtonListener(b,gp));

        JButton pathButton = new JButton("Analyze Path");
        pathButton.addActionListener(new AnalyzePathButtonListener(b));


        HorizontalCenterPanel hcp = new HorizontalCenterPanel(backButton,guessButton,collapseButton,pathButton);
        jp.add(hcp,BorderLayout.SOUTH);

        SinglePanelFrame spf = new SinglePanelFrame("AdaLogical Aenigma #41",jp);

    }
}
