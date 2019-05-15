import grid.lambda.CellLambda;
import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Main {
    private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
        Board b;
        String[] lines;

        public MyListener(Board b, String[] lines) { this.b = b; this.lines = lines; }
        @Override public int getNumXCells() { return b.getWidth(); }
        @Override public int getNumYCells() { return b.getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return lines; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            if (b.isBlock(cx,cy)) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
                return true;
            }

            int siz = b.possibles.getCell(cx,cy).possibles().size();
            GridPanel.DrawStringInCorner(bi,Color.BLACK,""+siz,Direction.SOUTHEAST);


            TemplatePointer tp = b.getCell(cx,cy);

            if (tp != null) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,"R:"+tp.getRegionId(),Direction.NORTH);
                GridPanel.DrawStringInCell(bi, Color.BLACK,"S:"+tp.getTemplate().id);
            }

            if (b.isLetter(cx,cy)) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy),Direction.NORTHWEST);
            }

            return true;
        }

        private static EdgeDescriptor UNKNOWN = new EdgeDescriptor(Color.BLACK,1);
        private static EdgeDescriptor PATH = new EdgeDescriptor(Color.BLACK,0);
        private static EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);

        private EdgeDescriptor toDir(int x0,int y0,int x1,int y1) {
            TemplatePointer tp0 = b.getCell(x0,y0);
            TemplatePointer tp1 = b.getCell(x1,y1);

            if (tp0 == null && tp1 == null) return UNKNOWN;
            if (tp0 != null && tp1 != null) {
                return tp0.getRegionId() == tp1.getRegionId() ? PATH : WALL;
            }
            return WALL;
        }


        @Override public EdgeDescriptor onBoundary() { return WALL; }

        @Override public EdgeDescriptor toEast(int x, int y) {
            return toDir(x,y,x+1,y);
        }

        @Override public EdgeDescriptor toSouth(int x, int y) {
            return toDir(x,y,x,y+1);
        }


    }

    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("bad command line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
        //Board b = Region.makeTestBoard();


        Solver s = new Solver(b);
        s.Solve(b);
        System.out.println("# of Solutions: " + s.GetSolutions().size());

        /*
        while(true) {
            FlattenLogicer.RecursionStatus rs = s.recursiveApplyLogic(b);
            System.out.println("RAL 1 " + rs);
            if (rs != FlattenLogicer.RecursionStatus.GO) break;

            LogicStatus ls = s.applyTupleSuccessors(b);
            System.out.println("ATS " + ls);
            if (ls != LogicStatus.LOGICED) break;

            FlattenLogicer.RecursionStatus rs2 = s.recursiveApplyLogic(b);
            System.out.println("RAL 2 " + rs2);
            if (rs2 != FlattenLogicer.RecursionStatus.GO) break;
        }
*/

        StringBuffer sb = new StringBuffer();
        final Board fb = s.GetSolutions().get(0);
        fb.forEachCell((x,y)-> {
            if (fb.isBlock(x,y)) return;
            if (!fb.getCell(x,y).getTemplate().isCenter()) return;
            sb.append(LetterRotate.Rotate(fb.getLetter(x,y),fb.getCell(x,y).getTemplate().getCenterCount()));
        });



	    String[] lines = new String[] { sb.toString(), fb.gfr.getVar("SOLUTION") };

	    MyListener myl = new MyListener(fb,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #67 Solver",1200,800,
                myl,myl);
    }


}
