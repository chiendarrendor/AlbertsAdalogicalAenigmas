import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.SubReader;
import grid.puzzlebits.CellContainer;

public class SubBoard {
    @Shallow SubReader gfr;
    @Shallow int id;
    @Shallow int[] hclues;
    @Shallow int[] vclues;
    @Deep CellContainer<CellState> cells;
    @Shallow int unknowns;

    public SubBoard(int id, SubReader gfr) {
        this.id = id;
        this.gfr = gfr;

        hclues = new int[getHeight()];
        vclues = new int[getWidth()];

        for (int x = 0 ; x < getWidth() ; ++x) {
            String s = gfr.getCell("VCLUES",x,0);
            if (s.equals(".")) vclues[x] = -1;
            else vclues[x] = Integer.parseInt(s);
        }

        for (int y = 0 ; y < getHeight() ; ++y) {
            String s = gfr.getCell("HCLUES",0,y);
            if (s.equals(".")) hclues[y] = -1;
            else hclues[y] = Integer.parseInt(s);
        }

        cells = new CellContainer<CellState>(getWidth(),getHeight(),(x,y)->CellState.UNKNOWN);
        unknowns = getWidth() * getHeight();
    }

    public SubBoard(SubBoard right) {
        CopyCon.copy(this,right);
    }

    public int getVClue(int x) { return vclues[x]; }
    public int getHClue(int y) { return hclues[y]; }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public int getId() { return id; }
    public char getLetter(int x,int y) { return gfr.getCell("LETTERS",x,y).charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }

    public CellState getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y, CellState cs) { cells.setCell(x,y,cs); --unknowns; }
    public int getUnknowns() { return unknowns; }

}
