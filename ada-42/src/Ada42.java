// for next time:
// 1) file -> New Module From Exisiting Sources, use the 'common' directory
// 2) Project Structure -> Add Dependency to project on module 'common'

import grid.file.GridFileReader;
import grid.graph.GridGraph;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.puzzlebits.PentominoData;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Ada42 {
    private static class MyGridListener implements GridGraph.GridReference {
        private BoardState bs;

        public MyGridListener(BoardState bs) {
            this.bs = bs;
        }

        @Override public int getWidth() {
            return bs.getWidth();
        }

        @Override public int getHeight() {
            return bs.getHeight();
        }

        @Override public boolean isIncludedCell(int x, int y) {
            return bs.HasDistrict(x, y);
        }

        private boolean sameRegion(int x, int y, Direction d) {
            Point op = d.delta(x, y, 1);
            return bs.GetDistrict(x, y) == bs.GetDistrict(op.x, op.y);
        }

        @Override public boolean edgeExitsEast(int x, int y) {
            return sameRegion(x, y, Direction.EAST);
        }

        @Override public boolean edgeExitsSouth(int x, int y) {
            return sameRegion(x, y, Direction.SOUTH);
        }
    }

    private static class BoardState {
        GridFileReader gfr;
        GridGraph gg;
        PentominoData pd;

        public BoardState(String filename) {
            gfr = new GridFileReader(filename);
            gg = new GridGraph(new MyGridListener(this));
            pd = new PentominoData();
        }

        public char GetLetter(int x, int y) {
            return gfr.getBlock("LETTERS")[x][y].charAt(0);
        }

        public boolean HasLetter(int x, int y) {
            return Character.isAlphabetic(GetLetter(x, y));
        }

        public char GetNumber(int x, int y) {
            return gfr.getBlock("NUMBERS")[x][y].charAt(0);
        }

        public boolean HasNumber(int x, int y) {
            return Character.isDigit(GetNumber(x, y));
        }

        public char GetDistrict(int x, int y) {
            return gfr.getBlock("AREAS")[x][y].charAt(0);
        }

        public boolean HasDistrict(int x, int y) {
            return Character.isAlphabetic(GetDistrict(x, y));
        }

        public void forEachCell(XYLambda xyl) {
            CellLambda.forEachCell(getWidth(), getHeight(), xyl);
        }

        public int getWidth() {
            return gfr.getWidth();
        }

        public int getHeight() {
            return gfr.getHeight();
        }

        public char getDistrictType(int x, int y) {
            return pd.getType(gg.connectedSetOf(new Point(x, y)));
        }

        public int getDistrictNumberCount(int x, int y) {
            Set<Point> points = gg.connectedSetOf(new Point(x, y));
            int result = 0;
            for (Point p : points) {
                if (HasNumber(p.x, p.y)) ++result;
            }
            return result;
        }

        public int getDistrictLetterCount(int x,int y) {
            Set<Point> points = gg.connectedSetOf(new Point(x, y));
            int result = 0;
            for (Point p : points) {
                if (HasLetter(p.x, p.y)) ++result;
            }
            return result;
        }

        public int getDistrictClueCount(int x,int y) {
            if (!gfr.hasVar("CLUETYPE")) throw new RuntimeException("file needs CLUETYPE");
            if (gfr.getVar("CLUETYPE").equals("AENIGMA")) return getDistrictNumberCount(x,y);
            if (gfr.getVar("CLUETYPE").equals("ADDENDA")) return getDistrictLetterCount(x,y);
            throw new RuntimeException("Unknown Clue Type!");
        }




        public boolean isActivePentominoTtype(char p) {
            return gfr.getVar("PENTOMINOTYPES").indexOf(p) != -1;
        }
    }


    public static class MyListener implements GridPanel.GridListener, GridPanel.EdgeListener {
        private BoardState bs;
        private String[] lines;

        public MyListener(BoardState bs, String[] lines) {
            this.bs = bs;
            this.lines = lines;
        }

        public int getNumXCells() {
            return bs.getWidth();
        }

        public int getNumYCells() {
            return bs.getHeight();
        }

        public boolean drawGridNumbers() {
            return true;
        }

        public boolean drawGridLines() {
            return true;
        }

        public boolean drawBoundary() {
            return true;
        }

        public String[] getAnswerLines() {
            return lines;
        }

        public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            if (bs.HasNumber(cx, cy)) GridPanel.DrawStringInCell(bi, Color.black, "" + bs.GetNumber(cx, cy));
            if (bs.HasLetter(cx, cy)) {
                GridPanel.DrawStringUpperLeftCell(bi, Color.black, "" + bs.GetLetter(cx, cy));
                boolean isBase = bs.isActivePentominoTtype(bs.getDistrictType(cx, cy));

                GridPanel.DrawStringInCell(bi,
                        isBase ? Color.red : Color.blue,
                        "" + LetterRotate.Rotate(bs.GetLetter(cx, cy), bs.getDistrictClueCount(cx, cy)));

            }

            if (!bs.HasDistrict(cx,cy)) {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0,0,bi.getWidth(),bi.getHeight());
            }

            return true;
        }

        public EdgeDescriptor onBoundary() {
            return new GridPanel.EdgeListener.EdgeDescriptor(Color.black, 5);
        }

        public EdgeDescriptor toEast(int x, int y) {
            return new EdgeDescriptor(Color.black, bs.GetDistrict(x, y) == bs.GetDistrict(x + 1, y) ? 1 : 5);
        }

        public EdgeDescriptor toSouth(int x, int y) {
            return new EdgeDescriptor(Color.black, bs.GetDistrict(x, y) == bs.GetDistrict(x, y + 1) ? 1 : 5);
        }
    }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        BoardState bs = new BoardState(args[0]);
        String[] lines = new String[]{"Adalogical Aenigma", "#42 Solver"};

        StringBuffer sb = new StringBuffer();
        bs.forEachCell((x, y) -> {
            if (!bs.HasDistrict(x, y)) return;
            if (!bs.HasLetter(x, y)) return;
            if (!bs.isActivePentominoTtype(bs.getDistrictType(x, y))) return;
            sb.append(LetterRotate.Rotate(bs.GetLetter(x, y), bs.getDistrictClueCount(x, y)));
        });

        lines[0] = sb.toString();
        lines[1] = bs.gfr.getVar("SOLUTION");


        MyListener myl = new MyListener(bs, lines);
        GridFrame gridFrame = new GridFrame("Adalogical Aenigma #42", 1300, 768, myl, myl);

        Map<Character,StringBuffer> sbmap = new HashMap<>();
        PentominoData pd = bs.pd;

        for(char c: pd.getNames()) {
            sbmap.put(c,new StringBuffer());
        }

        bs.forEachCell((x,y)->{
            if (!bs.HasDistrict(x, y)) return;
            if (!bs.HasLetter(x, y)) return;
            char ad = bs.getDistrictType(x,y);
            sbmap.get(ad).append( LetterRotate.Rotate(bs.GetLetter(x,y),bs.getDistrictClueCount(x,y)) );

        });

        for (char c : pd.getNames()) {
            if (bs.isActivePentominoTtype(c)) continue;
            System.out.println(c + ": " + sbmap.get(c).toString());
        }

        if (bs.gfr.hasVar("SECRETCOUNT")) {
            int secretcount = Integer.parseInt(bs.gfr.getVar("SECRETCOUNT"));
            for (int sidx = 1; sidx <= secretcount; ++sidx) {
                String sname = "SECRET" + sidx;
                StringBuffer sbuf = new StringBuffer();
                for (char c : bs.gfr.getVar(sname).toCharArray()) {
                    sbuf.append(sbmap.get(c));
                }
                System.out.println(sname + ": " + sbuf.toString());
            }
        }


    }

}
