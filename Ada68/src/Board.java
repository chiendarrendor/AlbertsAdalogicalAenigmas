import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow OminoSet ominoes;
    @Shallow OminoSet.OminoFamily ellOminoFamily;
    @Shallow OminoSet.OminoFamily squareOminoFamily;
    @Ignore Map<Character,Region> regionsById  = new HashMap<>();
    @Deep CellContainer<CellType> cells;
    @Shallow int unknowns;

    public Board(String filename) {
        gfr = new GridFileReader(filename);
        ominoes = new OminoSet(4);
        Point[] square = new Point[] { new Point(0,0),new Point(0,1),new Point(1,0),new Point(1,1) };
        Point[] ell = new Point[] { new Point(0,0),new Point(0,1),new Point(0,2),new Point(1,0) };
        ellOminoFamily = ominoes.getMirrorFamilyMatchingPoints(new HashSet<>(Arrays.asList(ell)));
        squareOminoFamily = ominoes.getFamilyMatchingPoints(new HashSet<>(Arrays.asList(square)));

        LambdaInteger unk = new LambdaInteger(0);
        cells = new CellContainer<CellType>(getWidth(),getHeight(),(x,y)-> {
            char rid = getRegionId(x,y);
            if (!regionsById.containsKey(rid)) {
                regionsById.put(rid,new Region(rid));
            }
            regionsById.get(rid).addCell(x,y);
            unk.inc();
            return CellType.UNKNOWN;
        });

        unknowns = unk.get();

        for (Region r : regionsById.values()) {
            r.calculateOverlappingOminoes(ominoes,squareOminoFamily);
        }




    }

    public Board(Board right) {
        CopyCon.copy(this,right);

        for(Character c : right.regionsById.keySet() ) {
            regionsById.put(c,new Region(right.regionsById.get(c)));
        }
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }

    public CellType getCellType(int x,int y) { return cells.getCell(x,y); }
    public void setCellType(int x,int y, CellType ct) { --unknowns; cells.setCell(x,y,ct);}
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }


    @Override public boolean isComplete() { return unknowns == 0; }

    private static class MyMove {
        int x;
        int y;
        CellType ct;
        public MyMove(int x,int y,CellType ct) { this.x = x; this.y = y; this.ct = ct; }

        public boolean applyMove(Board b) {
            CellType curct = b.getCellType(x,y);
            if (curct != CellType.UNKNOWN) return ct == curct;
            b.setCellType(x,y,ct);
            return true;
        }
    }



    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this);  }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (getCellType(x,y) != CellType.UNKNOWN) return null;

        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,CellType.BLACK);
        MyMove mm2 = new MyMove(x,y,CellType.WHITE);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }


}
