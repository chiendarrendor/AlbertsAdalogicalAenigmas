import grid.puzzlebits.Direction;

import java.awt.Color;

public class StripPtr {
    Strip s;
    boolean reversed;
    Direction stripdir; // either EAST or SOUTH
    public StripPtr(Strip s,boolean reversed,Direction stripdir) { this.s = s; this.reversed = reversed; this.stripdir = stripdir; }

    public int stripIndexOfCell(int x,int y) {
        int index = stripdir == Direction.EAST ? x : y;
        if (reversed) index = 9 - index;
        return index;
    }

    public boolean isWhite(int x,int y) { return s.isWhiteList.get(stripIndexOfCell(x,y)); }
    public char getChar(int x,int y) { return s.characterList.get(stripIndexOfCell(x,y)); }
    public Color getColor(int x,int y) { return s.colorList.get(stripIndexOfCell(x,y)); }



}
