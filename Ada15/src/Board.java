import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.puzzlebits.CellContainer;

public class Board {
    GridFileReader gfr;
    CellContainer<CellContents> cells;


    public Board(String arg) {
        gfr = new GridFileReader(arg);

        cells = new CellContainer<CellContents>(getWidth(),getHeight(),
                (x,y) -> {
                    char ic = gfr.getBlock("INITIAL")[x][y].charAt(0);
                    char sc = '.';
                    if (gfr.hasBlock("SOLUTION")) {
                        sc = gfr.getBlock("SOLUTION")[x][y].charAt(0);
                    }

                    if (ic != '.' && sc != '.') throw new RuntimeException("SOLUTION should not override INITIAL");
                    if (hasNumber(x,y) && ic != '#') throw new RuntimeException("numbers should be on INITIAL blocks!");
                    char fc = '.';
                    if (ic != '.') fc = ic;
                    if (sc != '.') fc = sc;

                    switch(fc) {
                        case '1': return CellContents.OPEN_SW;
                        case '3': return CellContents.OPEN_SE;
                        case '7': return CellContents.OPEN_NW;
                        case '9': return CellContents.OPEN_NE;
                        case '#': return CellContents.BLOCK;
                        case '0': return CellContents.ORTHO_EMPTY;
                        case '5': return CellContents.DIAG_EMPTY;
                        case '.': return CellContents.UNKNOWN;
                        default: throw new RuntimeException("Unknown cell character " + fc + " at " + x + "," + y);
                    }
                });


    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getLetter(int x, int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }

    public boolean hasNumber(int x,int y) { return gfr.getBlock("NUMBERS")[x][y].charAt(0) != '.';}
    public int getNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }

    public CellContents getCell(int x,int y) { return cells.getCell(x,y); }

    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

}
