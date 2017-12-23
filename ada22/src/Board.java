import grid.file.GridFileReader;
import grid.logic.astar.AStarSolvable;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.Solvable;

import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 5/18/2017.
 */
public class Board implements Solvable<Board>, AStarSolvable<Board>, FlattenSolvable<Board>
{
    CellState cells[][];
    Clue clues[][];
    int numsolved;
    Vector<Point> blackCells = new Vector<>();
    Vector<Clue> clueList = new Vector<>();
    String why = null;

    public void setWhy(String s) { why = s;}
    public String getWhy() { return why; }


    GridFileReader gfr;
    public Board(String fname)
    {
        gfr = new GridFileReader(fname);
        if (!gfr.hasBlock("LETTERS")) throw new RuntimeException("Data File has no LETTERS");
        if (!gfr.hasBlock("CLUES")) throw new RuntimeException("Data File has no CLUES");

        cells = new CellState[getWidth()][getHeight()];
        clues = new Clue[getWidth()][getHeight()];
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y  = 0 ; y < getHeight() ; ++y)
            {
                cells[x][y] = CellState.UNKNOWN;
                clues[x][y] = null;
                String clue = gfr.getBlock("CLUES")[x][y];
                if (clue.equals("..")) continue;
                clues[x][y] = new Clue(clue,x,y);
                clueList.add(clues[x][y]);
            }
        }
        numsolved = 0;
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        numsolved  = right.numsolved;
        clues = right.clues;

        cells = new CellState[getWidth()][getHeight()];
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                cells[x][y] = right.cells[x][y];
            }
        }
        blackCells.addAll(right.blackCells);
        clueList = right.clueList;
    }


    public int getWidth() { return gfr.getWidth();}
    public int getHeight() { return gfr.getHeight();}

    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.';}
    public char getLetter(int x,int y)
    {
        return gfr.getBlock("LETTERS")[x][y].charAt(0);
    }

    public Clue getClue(int x,int y) { return clues[x][y]; }

    public CellState getCell(int x,int y) { return cells[x][y];}

    public void setCellBlack(int x,int y)
    {
        if (getCell(x,y) != CellState.UNKNOWN) throw new RuntimeException("Cant setCell on non unknown");
        ++numsolved;

        cells[x][y] = CellState.BLACK;
        blackCells.add(new Point(x,y));
    }

    public void setCellWhite(int x,int y)
    {
        if (getCell(x,y) != CellState.UNKNOWN) throw new RuntimeException("Cant setCell on non unknown");
        ++numsolved;

        cells[x][y] = CellState.WHITE;
    }

    public List<Point>getBlackCells() { return blackCells; }


    @Override
    public boolean isSolution()
    {
        return numsolved == (getWidth() * getHeight());
    }

    private List<Board> successorsAtCell(int x,int y)
    {
        Vector<Board> result = new Vector<>();
        Board b1 = new Board(this);
        b1.setCellBlack(x,y);
        b1.setWhy("(" + x + "," + y + "): black");
        result.add(b1);

        Board b2 = new Board(this);
        b2.setCellWhite(x,y);
        result.add(b2);
        b2.setWhy("(" + x + "," + y + "): white");
        return result;
    }




    @Override
    public List<Board> guessAlternatives()
    {
        for (Clue c : getClues())
        {
            if (getCell(c.x,c.y) != CellState.UNKNOWN) continue;
            return successorsAtCell(c.x,c.y);
        }


        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                if (getCell(x,y) != CellState.UNKNOWN) continue;
                return successorsAtCell(x,y);
            }
        }
        throw new RuntimeException("Should not call Successors if board is solved!");
    }

    private Point[] deltas = { new Point(1,0) , new Point(0,1), new Point(-1,0), new Point(0,-1)};

    public boolean onBoard(Point p)
    {
        return p.x >= 0 && p.y >= 0 && p.x < getWidth() && p.y < getHeight();
    }

    public List<Point> adjacents(int x,int y)
    {
        Vector<Point> result = new Vector<>();
        for (Point p : deltas)
        {
            Point np = new Point(x+p.x,y+p.y);
            if (!onBoard(np)) continue;
            result.add(np);
        }
        return result;
    }

    public List<Clue> getClues() { return clueList; }

    @Override
    public int winGrade()
    {
        return getWidth() * getHeight();
    }

    @Override
    public int grade()
    {
        return numsolved;
    }

    @Override
    public List<Board> successors()
    {
        Vector<Board> result = new Vector<>();
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                if (getCell(x,y) != CellState.UNKNOWN) continue;
                result.addAll(successorsAtCell(x,y));
            }
        }

        return result;
    }

    @Override
    public String canonicalKey()
    {
        StringBuffer sb = new StringBuffer();
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                switch (getCell(x, y))
                {
                    case UNKNOWN:
                        sb.append('U');
                        break;
                    case BLACK:
                        sb.append('B');
                        break;
                    case WHITE:
                        sb.append('W');
                        break;
                }
            }
        }
        return sb.toString();
    }

    @Override
    public boolean isComplete()
    {
        return isSolution();
    }

    private static class MyBoardMove
    {
        int x;
        int y;
        CellState cs;
        public MyBoardMove(int x,int y,CellState cs) { this.x = x; this.y = y; this.cs = cs;}
        public void apply(Board b)
        {
            if (cs == CellState.BLACK) b.setCellBlack(x,y);
            if (cs == CellState.WHITE) b.setCellWhite(x,y);
        }
    }


    @Override
    public List<FlattenSolvableTuple<Board>> getSuccessorTuples()
    {
        Vector<FlattenSolvableTuple<Board>> result = new Vector<>();
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                if (getCell(x,y) != CellState.UNKNOWN) continue;
                // this is a pair, black first, then white
                List<Board> pair = successorsAtCell(x,y);
                FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>(
                        pair.get(0),new MyBoardMove(x,y,CellState.BLACK),
                        pair.get(1),new MyBoardMove(x,y,CellState.WHITE)
                );
                result.add(fst);

            }
        }
        return result;
    }

    @Override
    public void applyMove(Object o)
    {
        MyBoardMove mbm = (MyBoardMove)o;
        mbm.apply(this);

    }
}
