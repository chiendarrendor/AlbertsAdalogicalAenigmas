import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.List;
import java.util.Set;

public class Board implements StandardFlattenSolvable<Board> {

    @Shallow  GridFileReader gfr;
    @Deep CellContainer<TemplatePointer> cells;
    @Deep CellContainer<Possibles> possibles;
    @Shallow int nextregionid;
    @Shallow int unknowns;


    public Board(String arg) {
        gfr = new GridFileReader(arg);

        nextregionid = 1;

        LambdaInteger unk = new LambdaInteger(0);

        cells = new CellContainer<TemplatePointer>(getWidth(),getHeight(),(x,y)-> {
            if (isLetter(x,y)) unk.inc();
            return null;
        });

        possibles = new CellContainer<Possibles>(getWidth(),getHeight(),
                (x,y)->new Possibles(),
                (x,y,r)->new Possibles(r)
        );



        unknowns = unk.get();
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }

    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean isLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean isBlock(int x,int y) { return !isLetter(x,y); }

    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public boolean isPossible(int x,int y,int tempid) { return possibles.getCell(x,y).isPossible(tempid); }
    public void makePossible(int x,int y, int tempid) { possibles.getCell(x,y).set(tempid); }
    public void makeImpossible(int x,int y,int tempid) { possibles.getCell(x,y).deny(tempid); }

    public boolean isTemplatePlaceable(int x,int y, Region.Template t) {
        for (int i = 0 ; i < t.deltas.length; ++i) {
            int dx = x + t.deltas[i].x;
            int dy = y + t.deltas[i].y;
             int tid = Region.templateAtIndex(t.idx,i);
            if (!onBoard(dx,dy)) {
                return false;
            }
            if (isBlock(dx,dy)) {
                 return false;
            }
            if (cells.getCell(dx,dy) != null) {
                return false;
            }
            if (!isPossible(dx,dy,tid)) {
                 return false;
            }
        }
        return true;
    }


    public void placeTemplate(int x, int y, Region.Template t) {
        if (!isTemplatePlaceable(x,y,t)) throw new RuntimeException("Can't Place template here!");

        Point p = new Point(x,y);
        for (int i = 0 ; i < t.deltas.length; ++i) {
            int dx = x + t.deltas[i].x;
            int dy = y + t.deltas[i].y;
            int tid = Region.templateAtIndex(t.idx,i);
            Region.Template it = Region.templates[tid];
            Point tp = new Point(dx,dy);
            cells.setCell(dx,dy,new TemplatePointer(nextregionid,it,tp));
            makePossible(dx,dy,tid);
        }

        unknowns -= 3;
        ++nextregionid;
    }

    public TemplatePointer getCell(int x,int y) { return cells.getCell(x,y); }

    @Override public boolean isComplete() { return unknowns == 0; }

    private static class MyMove {
        int x;
        int y;
        int tempid;
        boolean doSet;

        public MyMove(int x,int y,int tempid,boolean doSet) {
            this.x = x;
            this.y = y;
            this.tempid = tempid;
            this.doSet = doSet;
        }

        public boolean applyMove(Board b) {
            Region.Template t = Region.templates[tempid];

            boolean empty = true;
            for (int i = 0 ; i < 3 ; ++i ) {
                int dx = x + t.deltas[i].x;
                int dy = y + t.deltas[i].y;
                int iidx = Region.templateAtIndex(tempid,i);
                Region.Template it = Region.templates[iidx];
                TemplatePointer itp = b.getCell(dx,dy);

                if (doSet) {
                    if (itp == null) {
                        continue;
                    } else {
                        if (itp.getTemplate().idx == iidx) {
                            empty = false;
                        } else {
                            return false;
                        }
                    }
                } else {
                    if (itp != null && itp.getTemplate().idx == iidx) {
                        return false;
                    }
                }
            }

            if (empty == false) return true;

            for (int i = 0 ; i < 3 ; ++i ) {
                int dx = x + t.deltas[i].x;
                int dy = y + t.deltas[i].y;
                Point dp = new Point(dx, dy);
                int iidx = Region.templateAtIndex(tempid, i);
                Region.Template it = Region.templates[iidx];

                if (doSet) {
                    b.possibles.getCell(dx, dy).set(iidx);
                    b.cells.setCell(dx, dy, new TemplatePointer(b.nextregionid, it, dp));
                } else {
                    b.possibles.getCell(dx, dy).deny(iidx);
                }
            }

            if (doSet) {
                ++b.nextregionid;
            }

            return true;
        }
    }




    @Override public boolean applyMove(Object o) {
        return ((MyMove)o).applyMove(this);
    }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (isBlock(x,y)) return null;
        if (getCell(x,y) != null) return null;
        Set<Integer> possibleset = possibles.getCell(x,y).possibles();
        if (possibleset.size() < 2) return null;

        FlattenSolvableTuple<Board> result = new FlattenSolvableTuple<Board>();
        for (int possible : possibleset) {
            MyMove pro = new MyMove(x,y,possible,true);
            MyMove anti = new MyMove(x,y,possible,false);
            Board b = new Board(this);
            pro.applyMove(b);
            result.addTuple(b,anti);
        }

        return result;
    }
}
