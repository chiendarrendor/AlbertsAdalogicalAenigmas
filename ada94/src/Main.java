import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import grid.spring.ListMultiListener;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static class MyListener extends ListMultiListener<Board> implements GridPanel.EdgeListener {
        String[] lines;

        public MyListener(List<Board> b, String[] lines) { super(b); this.lines = lines; }
        @Override public int getNumXCells() { return b().getWidth(); }
        @Override public int getNumYCells() { return b().getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
        private static final EdgeDescriptor FUTON = new EdgeDescriptor(Color.WHITE,1);
        private static final EdgeDescriptor UNKNOWN = new EdgeDescriptor(Color.BLACK,1);

        private EdgeDescriptor inDirection(int x,int y, Direction d) {
            Point myP = new Point(x,y);
            Point op = d.delta(myP,1);
            FutonCell mycell = b().getFutonCell(myP.x,myP.y);
            FutonCell ocell = b().getFutonCell(op.x,op.y);
            if (!mycell.isUnique() || !ocell.isUnique()) return UNKNOWN;
            if (mycell.getUniqueFuton().getUuid() != ocell.getUniqueFuton().getUuid()) return UNKNOWN;
            return FUTON;
        }

        @Override public EdgeDescriptor onBoundary() { return WALL; }
        @Override public EdgeDescriptor toEast(int x, int y) { return inDirection(x,y,Direction.EAST); }
        @Override public EdgeDescriptor toSouth(int x, int y) { return inDirection(x,y,Direction.SOUTH); }

        private static final int INSET = 5;

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();

            if (b().hasPillar(cx,cy)) {
                g.setColor(Color.GRAY);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                g.setColor(Color.WHITE);
                g.fillOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                g.setColor(Color.BLACK);
                g.drawOval(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                if (b().hasNumericPillar(cx,cy)) {
                    GridPanel.DrawStringInCell(bi,Color.BLACK,""+b().getNumericPillarValue(cx,cy));
                }

                return true;
            }

            Cell c = b().getCell(cx,cy);
            if (c.isDone()) {
                if (c.getDoneType() == CellType.AISLE) {
                    g.setColor(Color.BLACK);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                }
                if (c.getDoneType() == CellType.PILLOW || c.getDoneType() == CellType.FUTON) {
                    g.setColor(Color.PINK);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                }
                if (c.getDoneType() == CellType.PILLOW) {
                    g.setColor(Color.BLACK);
                    g.drawRect(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET);
                }
            }



            GridPanel.DrawStringInCell(bi,Color.BLACK,""+b().getLetter(cx,cy));

            return true;
        }


    }


    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
	    String[] lines = new String[] { "Adalogical Aenigma","#94 Solver"};

        Solver s = new Solver(b);
        s.Solve(b);

        List<Board> list;
        if (s.GetSolutions().size() == 0) {
            list = new ArrayList<>();
            list.add(b);
        } else {
            list = s.GetSolutions();
        }

        if (s.GetSolutions().size() == 1) {
            StringBuffer sb = new StringBuffer();
            final Board fb = s.GetSolutions().get(0);

            fb.forEachCell((x,y)-> {
                Cell c = fb.getCell(x,y);
                if (c.getDoneType() != CellType.PILLOW) return;
                FutonPair fp = fb.getFutonCell(x,y).getPillows().iterator().next();
                int aislecount = 0;

                for(Point np : fp.getAdjacents()) {
                    if (fb.getCell(np.x,np.y).getDoneType() == CellType.AISLE) ++aislecount;
                }
                sb.append(LetterRotate.Rotate(fb.getLetter(x,y),aislecount));
            });
            lines[0] = sb.toString();
            lines[1] = fb.gfr.getVar("SOLUTION");
        }


	    MyListener myl = new MyListener(list,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #94 Solver",1200,800,myl,myl);
    }


}
