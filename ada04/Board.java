import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by chien on 6/16/2017.
 */
public class Board implements FlattenSolvable<Board>
{
    GridFileReader gfr;
    char[][] grid;
    Vector<Clue> clues = new Vector<>();

    public class Clue
    {
        int idx;
        Set<Point> priors = new HashSet<>();
        int curx;
        int cury;
        Set<Direction> validdirs = new HashSet<>();
        int char_to_do;
        String string;

        public Point getCur() { return new Point(curx,cury);}


        public Clue(int idx,int sx,int sy,Direction dir,String s)
        {
            this.idx = idx;
            string = s;
            char_to_do = 0;

            // process initial space specially.
            if (!canLand(sx,sy,string.charAt(0))) throw new RuntimeException("Can't even start " + string);
            setChar(sx,sy,string.charAt(0));
            char_to_do = 1;
            validdirs.add(dir);
            curx = sx;
            cury = sy;
            priors.add(new Point(sx,sy));
        }

        public Clue(Clue right)
        {
            idx = right.idx;
            for (Point p : right.priors) priors.add(p);
            curx = right.curx;
            cury = right.cury;
            for (Direction d : right.validdirs) validdirs.add(d);
            char_to_do = right.char_to_do;
            string = right.string;
        }

        public boolean isDone() { return char_to_do == string.length(); }
        public boolean canLand(int x,int y,char c)
        {
            if (priors.contains(new Point(x,y))) return false;
            if (isEmpty(x,y)) return true;
            if (charAt(x,y) == c) return true;
            return false;
        }

        public boolean canLandNext(int x,int y)
        {
            return canLand(x,y,string.charAt(char_to_do));
        }



        public void go(Direction d)
        {
            if (isDone()) throw new RuntimeException("Can't go on a done!");
            if (!validdirs.contains(d)) throw new RuntimeException("can't go in an illegal direction!");
            Point np = d.goDir(curx,cury);
            char nc = string.charAt(char_to_do);
            if (!canLand(np.x,np.y,nc)) throw new RuntimeException("Can't land there!");
            priors.add(np);
            curx = np.x;
            cury = np.y;
            setChar(curx,cury,nc);
            ++char_to_do;
            validdirs.clear();
            for(Direction dd : Direction.values())
            {
                Point nnp = dd.goDir(np);
                if (nnp.x < 0 || nnp.y < 0 || nnp.x >= getWidth() || nnp.y >= getHeight()) continue;
                validdirs.add(dd);
            }
        }

        public Set<Direction> validDirs() { return validdirs; }
        public void clearDirection(Direction d)
        {
            if (!validdirs.contains(d)) throw new RuntimeException("Can't clear dir, it' gone!");
            validdirs.remove(d);
        }

        private class AntiMove
        {
            int idx;
            Direction dir;
            public AntiMove(int idx,Direction dir) { this.idx = idx ; this.dir = dir; }
        }


        public FlattenSolvableTuple<Board> getSuccessors()
        {
            if (isDone()) return null;
            FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
            for(Direction d : validdirs)
            {
                Board b = new Board(Board.this);
                b.getClue(idx).go(d);
                AntiMove am = new AntiMove(idx,d);
                fst.addTuple(b,am);
            }
            return fst;
        }

        public void applyMove(Object o)
        {
            AntiMove am = (AntiMove)o;
            if (am.idx != idx) return;
            clearDirection(am.dir);
        }


    }


    public Board(String filename)
    {
        gfr = new GridFileReader(filename);
        grid = new char[getWidth()][getHeight()];
        Map<Integer,Point> starting = new HashMap<>();

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                grid[x][y] = '.';
                String startc = gfr.getBlock("STARTS")[x][y];
                if(!startc.equals(".")) starting.put(Integer.parseInt(startc),new Point(x,y));

            }
        }

        int numclues = Integer.parseInt(gfr.getVar("CLUECOUNT" ));
        for (int i = 1 ; i <= numclues ; ++i)
        {
            String var = gfr.getVar("CLUE" + i);
            String[] parts = var.split(" ");
            if (parts.length != 3) throw new RuntimeException("Can't parse clue " + i);
            Point p = starting.get(Integer.parseInt(parts[0]));
            if (p == null) throw new RuntimeException("Unknown start id " + parts[0]);
            Direction d;
            if (parts[1].equals("NORTH")) d = Direction.NORTH;
            else if (parts[1].equals("SOUTH")) d = Direction.SOUTH;
            else if (parts[1].equals("EAST")) d = Direction.EAST;
            else if (parts[1].equals("WEST")) d = Direction.WEST;
            else throw new RuntimeException("Unknown direction " + parts[1]);
            clues.add(new Clue(clues.size(),p.x,p.y,d,parts[2]));
        }
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        grid = new char[getWidth()][getHeight()];
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                grid[x][y] = right.grid[x][y];
            }
        }
        for (Clue c : right.clues) clues.add(new Clue(c));
    }



    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public char charAt(int x,int y) { return grid[x][y];}
    public boolean isEmpty(int x,int y) { return charAt(x,y) == '.'; }
    public void setChar(int x,int y,char c) { grid[x][y] = c; }

    public Clue getClue(int x) { return clues.elementAt(x); }
    public int getNumClues() { return clues.size(); }

    public char getMark(int x,int y){ return gfr.getBlock("MARKS")[x][y].charAt(0); }


    public void show()
    {
        for (int y = 0 ; y < getHeight() ; ++y)
        {
            for (int x = 0 ; x < getWidth() ; ++x)
            {
                System.out.print(grid[x][y]);
            }
            System.out.println("");
        }
    }




    @Override
    public boolean isComplete()
    {
        for(Clue c : clues) if (!c.isDone()) return false;
        return true;
    }

    @Override
    public List<FlattenSolvableTuple<Board>> getSuccessorTuples()
    {
        List<FlattenSolvableTuple<Board>> result = new Vector<>();
        for(Clue c : clues)
        {
            FlattenSolvableTuple<Board> fst = c.getSuccessors();
            if (fst == null) continue;
            result.add(fst);
        }
        return result;
    }

    @Override
    public void applyMove(Object o)
    {
        for(Clue c  : clues) c.applyMove(o);
    }

    @Override
    public List<Board> guessAlternatives()
    {
        List<Board> result = new Vector<>();
        for(Clue c : clues)
        {
            if (c.isDone()) continue;
            FlattenSolvableTuple<Board> fst = c.getSuccessors();
            result.addAll(fst.choices);
            return result;
        }
        throw new RuntimeException("No alternatives available?");
    }
}
