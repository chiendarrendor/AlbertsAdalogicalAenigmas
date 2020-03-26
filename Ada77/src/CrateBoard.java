import com.sun.org.apache.xpath.internal.operations.Bool;
import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import org.omg.CORBA.UNKNOWN;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrateBoard implements FlattenSolvable<CrateBoard> {
    @Shallow GridFileReader gfr;
    @Shallow Set<CrateShift> shifts;
    @Deep CellContainer<CrateShiftCellHolder> cells;
    @Ignore Set<CrateShift> lockedshifts = new HashSet<>();
    @Shallow List<Point> startingboxes = new ArrayList<>();
    @Deep CellContainer<TerminalState> terminalstatus;

    private List<CrateShift> getCrateShifts(int x,int y) {
        List<CrateShift> result = new ArrayList<>();
        Point self = cells.getCell(x,y).getSelf();
        result.add(new CrateShift(self));

        for (Direction d : Direction.orthogonals()) {
            for (int i = 1 ; ; ++i) {
                Point np = d.delta(self,i);
                if (!onBoard(np)) break;
                if (hasStartingBox(np.x,np.y)) break;
                result.add(new CrateShift(self,np));
            }
        }

        return result;
    }



    public CrateBoard(String fname) {
        gfr = new GridFileReader(fname);
        shifts = new HashSet<>();

        cells = new CellContainer<CrateShiftCellHolder>(getWidth(),getHeight(),
                (x,y)->new CrateShiftCellHolder(x,y),
                (x,y,r)->new CrateShiftCellHolder(r));

        forEachCell((x,y)->{
            if (!hasStartingBox(x,y)) return;
            List<CrateShift> cellshifts = getCrateShifts(x,y);
            for(CrateShift shift : cellshifts) {
                place(shift);
            }
            shifts.addAll(cellshifts);
            startingboxes.add(cells.getCell(x,y).getSelf());
        });

        terminalstatus = new CellContainer<TerminalState>(getWidth(),getHeight(),(x,y)-> TerminalState.UNKNOWN);

    }

    public CrateBoard(CrateBoard right) {
        CopyCon.copy(this,right);
        lockedshifts.addAll(right.lockedshifts);
    }



    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasStartingBox(int x,int y) { return gfr.getBlock("BOXES")[x][y].charAt(0) == '@'; }
    public boolean hasClue(int x,int y) { return ! gfr.getBlock("CLUES")[x][y].equals(".");}
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean booleanForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl);}

    public void store(String fname) {
        try {
            FileWriter myWriter = new FileWriter(fname);
            for(CrateShift shift : shifts) {
                myWriter.write(
                        shift.getInitial().x + " " + shift.getInitial().y + " " +
                                shift.getTerminal().x + " " + shift.getTerminal().y + " " +
                                isActive(shift) + " " + isLocked(shift) + "\n"
                );
            }
            for (int y = 0 ; y < getHeight() ; ++y) {
                for (int x = 0 ; x < getWidth() ; ++x) {
                    if (terminalstatus.getCell(x,y) == TerminalState.MUSTBEEMPTY) {
                        myWriter.write(x + " " + y + " EMPTY" + "\n");
                    }
                    if (terminalstatus.getCell(x,y) == TerminalState.MUSTHAVEBOX) {
                        myWriter.write(x + " " + y + " BOX" + "\n");
                    }
                }
            }

            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void load(String fname) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fname));
            String line;
            while((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 6) {
                    int ix = Integer.parseInt(parts[0]);
                    int iy = Integer.parseInt(parts[1]);
                    int dx = Integer.parseInt(parts[2]);
                    int dy = Integer.parseInt(parts[3]);
                    boolean isActive = Boolean.parseBoolean(parts[4]);
                    boolean isLocked = Boolean.parseBoolean(parts[5]);
                    CrateShift shift = find(ix,iy,dx,dy);
                    if (!isActive) remove(shift);
                    if (isLocked) {
                        if (!checkingSet(shift)) throw new RuntimeException("Can't load this board, set lands on terminalstates");
                    }
                } else if (parts.length == 3) {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    String type = parts[2];
                    terminalstatus.setCell(x,y,type.equals("BOX") ? TerminalState.MUSTHAVEBOX : TerminalState.MUSTBEEMPTY);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    private static Pattern CLUEMATCHER = Pattern.compile("^(\\d+)([NSEW])$");

    public int getClueSize(int x,int y) {
        String clue = gfr.getBlock("CLUES")[x][y];
        Matcher m = CLUEMATCHER.matcher(clue);
        if (!m.matches()) throw new RuntimeException("Can't parse clue " + clue);
        return Integer.parseInt(m.group(1));
    }

    public Direction getClueDirection(int x,int y) {
        String clue = gfr.getBlock("CLUES")[x][y];
        Matcher m = CLUEMATCHER.matcher(clue);
        if (!m.matches()) throw new RuntimeException("Can't parse clue " + clue);
        return Direction.fromShort(m.group(2));
    }




    public void place(CrateShift shift) {
        Point start = shift.getInitial();
        Point end = shift.getTerminal();

        cells.getCell(start.x,start.y).addInitial(shift);
        cells.getCell(end.x,end.y).addTerminal(shift);

        for (Point p : shift.getIntermediates()) {
            cells.getCell(p.x,p.y).addIntermediate(shift);
        }
    }

    public void remove(CrateShift shift) {
        Point start = shift.getInitial();
        Point end = shift.getTerminal();

        cells.getCell(start.x,start.y).removeInitial(shift);
        cells.getCell(end.x,end.y).removeTerminal(shift);

        for (Point p : shift.getIntermediates()) {
            cells.getCell(p.x,p.y).removeIntermediate(shift);
        }
    }

    public CrateShift find(int ix,int iy,int dx,int dy) {
        for(CrateShift shift : shifts) {
            if (shift.getInitial().x != ix || shift.getInitial().y != iy) continue;
            if (shift.getTerminal().x != dx || shift.getTerminal().y != dy) continue;
            return shift;
        }
        throw new RuntimeException("Can't find a shift");
    }


    public void play(int ix,int iy, int dx,int dy) {
        CrateShift shift = find(ix,iy,dx,dy);
        if (!isActive(shift)) throw new RuntimeException("can't play an inactive!");
        if (!checkingSet(shift)) throw new RuntimeException("Can't play this set on terminalstates");
    }


    public boolean isActive(CrateShift shift) {
        Point initial = shift.getInitial();
        return cells.getCell(initial.x,initial.y).isOnBoard(shift);
    }

    public boolean isLocked(CrateShift shift) {
        return lockedshifts.contains(shift);
    }

    private Set<CrateShift> getCrossingsOf(Point p) {
        return cells.getCell(p.x,p.y).getAll();
    }

    private Set<CrateShift> getCrossingsOf(Collection<Point> points) {
        Set<CrateShift> result = new HashSet<>();
        points.stream().forEach(p->result.addAll(getCrossingsOf(p)));
        return result;
    }



    public Set<CrateShift> getCrossingShifts(CrateShift shift) {
        if (!isActive(shift)) throw new RuntimeException("Can't get Crossing for an inactive shift");

        Set<CrateShift> result = new HashSet<>();
        result.addAll(getCrossingsOf(shift.getInitial()));
        result.addAll(getCrossingsOf(shift.getIntermediates()));
        result.addAll(getCrossingsOf(shift.getTerminal()));
        result.remove(shift);
        return result;
    }

    public TerminalState termstat(int x,int y) { return terminalstatus.getCell(x,y); }
    public TerminalState termstat(Point p){ return termstat(p.x,p.y); }
    public void settermstat(int x,int y,TerminalState ts) { terminalstatus.setCell(x,y,ts);}
    public void settermstat(Point p,TerminalState ts) { settermstat(p.x,p.y,ts); }

    public boolean checkingSet(CrateShift shift) {
        if (!shift.getInitial().equals(shift.getTerminal())) {
            if (termstat(shift.getInitial()) == TerminalState.MUSTHAVEBOX) return false;
        }
        if (termstat(shift.getTerminal()) == TerminalState.MUSTBEEMPTY) return false;
        for (Point p : shift.getIntermediates()) {
            if (termstat(p) == TerminalState.MUSTHAVEBOX) return false;
        }

        getCrossingShifts(shift).stream().forEach(s->remove(s));
        lockedshifts.add(shift);

        if (!shift.getInitial().equals(shift.getTerminal())) {
            settermstat(shift.getInitial(), TerminalState.MUSTBEEMPTY);
        }
        settermstat(shift.getTerminal(),TerminalState.MUSTHAVEBOX);
        for (Point p : shift.getIntermediates()) settermstat(p,TerminalState.MUSTBEEMPTY);

        return true;
    }



    @Override public boolean isComplete() {
        for (Point p : startingboxes) {
            if (cells.getCell(p.x,p.y).initialSize() != 1) return false;
            if (!lockedshifts.contains(cells.getCell(p.x,p.y).getUniqueInitial())) return false;
        }

        return booleanForEachCell((x,y)->  terminalstatus.getCell(x,y) != TerminalState.UNKNOWN );
    }

    public static class MyMove {
        CrateShift shift;
        boolean doSet;

        public MyMove(CrateShift shift, boolean doSet) { this.shift = shift; this.doSet = doSet; }
        public boolean applyMove(CrateBoard b) {
            if (doSet) {
                if (!b.isActive(shift)) return false;
                return(b.checkingSet(shift));
            } else {
                b.remove(shift);
            }
            return true;
        }
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }


    @Override public List<FlattenSolvableTuple<CrateBoard>> getSuccessorTuples() {
        List<FlattenSolvableTuple<CrateBoard>> result = new ArrayList<FlattenSolvableTuple<CrateBoard>>();

        for (CrateShift shift : shifts) {
            if (!isActive(shift)) continue;;
            if (lockedshifts.contains(shift)) continue;

            CrateBoard b1 = new CrateBoard(this);
            CrateBoard b2 = new CrateBoard(this);
            MyMove mm1 = new MyMove(shift,true);
            MyMove mm2 = new MyMove(shift,false);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<CrateBoard>(b1,mm1,b2,mm2));
        }

        return result;
    }

    private class ShiftHolder {
        private CrateShift shift;
        private int crosssize;
        public int getCrossSize() { return crosssize; }
        public CrateShift getShift() { return shift; }
        public ShiftHolder(CrateShift shift) { this.shift = shift; crosssize = getCrossingShifts(shift).size(); }
    }



    @Override public List<CrateBoard> guessAlternatives() {
        List<ShiftHolder> holders = new ArrayList<>();
        for (CrateShift shift : shifts) {
            if (!isActive(shift)) continue;;
            if (lockedshifts.contains(shift)) continue;
            holders.add(new ShiftHolder(shift));
        }
        Collections.sort(holders,(sh1,sh2)->Integer.compare(sh2.getCrossSize(),sh1.getCrossSize()));
        CrateShift guess = holders.get(0).getShift();

        CrateBoard b1 = new CrateBoard(this);
        CrateBoard b2 = new CrateBoard(this);
        MyMove mm1 = new MyMove(guess,true);
        MyMove mm2 = new MyMove(guess,false);
        mm1.applyMove(b1);
        mm2.applyMove(b2);

        List<CrateBoard> result = new ArrayList<>();
        result.add(b1);
        result.add(b2);

        return result;
    }
}
