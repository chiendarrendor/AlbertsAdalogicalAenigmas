import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    @Shallow GridFileReader gfr;
    @Shallow Map<Character,List<Point>> regioncells;
    @Deep CellContainer<CellState> cells;

    private void organizeRegions() {
        regioncells = new HashMap<>();
        forEachCell((x,y)->{
            char rid = getRegionId(x,y);
            if (!regioncells.containsKey(rid)) regioncells.put(rid,new ArrayList<>());
            regioncells.get(rid).add(new Point(x,y));
        });
    }




    public Board(String fname) {
        gfr = new GridFileReader(fname);
        organizeRegions();

        cells = new CellContainer<CellState>(getWidth(),getHeight(),(x,y)-> hasSoldier(x,y) ? CellState.SOLDIER_START : CellState.UNKNOWN);



    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl);}
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public boolean hasSoldier(int x,int y) { return !gfr.getBlock("SOLDIERS")[x][y].equals("."); }
    public int getSoldier(int x,int y) {
        String s = gfr.getBlock("SOLDIERS")[x][y];
        if (s.equals("?")) return -1;
        return Integer.parseInt(s);
    }
}
