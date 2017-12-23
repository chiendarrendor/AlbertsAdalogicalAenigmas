import grid.letter.LetterRotate;
import grid.logic.flatten.FlattenLogicer;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Ada50
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
            Graphics2D g = (Graphics2D)bi.getGraphics();

            int num = b.getNumber(cx,cy);
            if (num != -1) GridPanel.DrawStringUpperLeftCell(bi,Color.black,Integer.toString(num));

            CellColor cc = b.ob.getCellColor(cx,cy);
            if (cc == CellColor.UNKNOWN) return true;

            int SIZE = 25;
            int cenx = bi.getWidth()/2;
            int ceny = bi.getHeight()/2;
            g.setColor(cc == CellColor.BLACK ? Color.BLACK : Color.white);
            g.fillOval(cenx-SIZE/2,ceny-SIZE/2,SIZE,SIZE);
            g.setColor(Color.black);
            g.setStroke(new BasicStroke(4.0f));
            g.drawOval(cenx-SIZE/2,ceny-SIZE/2,SIZE,SIZE);

            return true;
        }
    }

    public static void OminoPlaceCount(Board b)
    {
        int ctr = 0;
        for (OminoBoard.OminoPlaceSet ops : b.ob.ominosets.values())
        {
            ctr += ops.ominoes.size();
        }
        System.out.println("places: " + ctr);
    }




    public static void ShowOne(Board b,int x,int y)
    {
        OminoBoard.OminoCell os = b.ob.cells[x][y];
        System.out.println("On Ominos: ");
        for (OminoBoard.OminoPlaceSet.OminoPlace op : os.onplaces)
        {
            System.out.println("x: " + op.x + " y: " + op.y + "\n" + op.omino.toString());
        }
        System.out.println("Off Ominos: ");
        for (OminoBoard.OminoPlaceSet.OminoPlace op : os.offplaces)
        {
            System.out.println("x: " + op.x + " y: " + op.y + "\n" + op.omino.toString());
        }


    }

    private static class MyEdgeListener implements GridPanel.EdgeListener
    {
        Board b;
        EdgeDescriptor thick = new EdgeDescriptor(Color.black,5);
        EdgeDescriptor thin = new EdgeDescriptor(Color.black,1);

        public MyEdgeListener(Board b) { this.b = b; }

        public EdgeDescriptor onBoundary() { return thick;}


        @Override
        public EdgeDescriptor toEast(int x, int y)
        {
            OminoBoard.OminoPlaceSet.OminoPlace osh = b.ob.cells[x][y].thePlace;
            OminoBoard.OminoPlaceSet.OminoPlace oso = b.ob.cells[x+1][y].thePlace;

            if (osh == null && oso == null) return thin;
            if (osh == null ^ oso == null) return thick;
            return thin;
        }

        @Override
        public EdgeDescriptor toSouth(int x, int y)
        {
            OminoBoard.OminoPlaceSet.OminoPlace osh = b.ob.cells[x][y].thePlace;
            OminoBoard.OminoPlaceSet.OminoPlace oso = b.ob.cells[x][y+1].thePlace;

            if (osh == null && oso == null) return thin;
            if (osh == null ^ oso == null) return thick;
            return thin;
        }
    }

    public static void PlaceDown(OminoBoard ob,int x,int y,int size,char shape,int rotid)
    {
        OminoBoard.OminoCell oc = ob.cells[x][y];
        for (OminoBoard.OminoPlaceSet.OminoPlace op : oc.onplaces)
        {
            Omino o = op.omino;
            if (o.getSize() == size && o.getShapeId() == shape && o.getRotId() == rotid)
            {
                op.placeDown();
                return;
            }
        }
        throw new RuntimeException("Can't PlaceDown this thing!");
    }


    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("bad command line");
            System.exit(1);
        }

        Board b = new Board(args[0]);

        Solver s = new Solver();
        s.Solve(b);

        final Board fb = s.GetSolutions().get(0);

        fb.forEachCell((x,y)->{
            if (fb.getNumber(x,y) == -1) return;
            if (fb.ob.getCellColor(x,y) != CellColor.BLACK) return;
            System.out.print(LetterRotate.Rotate(fb.ob.cells[x][y].thePlace.omino.getShapeId(),fb.getNumber(x,y)));
        });
        System.out.println("");



        GridFrame gf = new GridFrame("Ada #50 solver",1200,800,new MyGridListener(fb),new MyEdgeListener(fb));

    }



}
