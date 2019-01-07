package grid.file;

// this class is a wrapper around a GridFileReader to provide a GFR-like interface
// to only a rectangular subsection of the grids of a GFR

import java.awt.Point;

public class SubReader {
    GridFileReader gfr;
    int ulx;
    int uly;
    int width;
    int height;

    public SubReader(GridFileReader gfr,int ulx,int uly,int width,int height) {
        this.gfr = gfr;
        this.ulx = ulx;
        this.uly = uly;
        this.width = width;
        this.height = height;
        if (!gfr.inBounds(ulx,uly)) throw new RuntimeException("UL point must be inside Grid!");
        if (!gfr.inBounds(ulx+width-1,uly+height-1)) throw new RuntimeException("LR point must be inside Grid!");

    }

    public int getWidth() { return width;}
    public int getHeight() { return height; }
    public boolean inBounds(int x,int y) {
        if (x < 0) return false;
        if (y < 0) return false;
        if (x >= width) return false;
        if (y >= height) return false;
        return true;
    }
    public boolean inBounds(Point p) { return inBounds(p.x,p.y); }
    public boolean hasBlock(String bname) { return gfr.hasBlock(bname); }
    public boolean hasVar(String vname) { return gfr.hasVar(vname); }
    public String getVar(String vname) { return gfr.getVar(vname); }
    public String getCell(String bname,int x,int y) {
        return gfr.getBlock(bname)[x+ulx][y+uly];
    }

}
