import grid.puzzlebits.CellContainer;
import javafx.geometry.Pos;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PossibleFutons {
    private int width;
    private int height;
    private Map<Integer,FutonPair> allFutons;
    private CellContainer<FutonCell> cells;
    private Map<Integer,FutonPair> setFutons;

    public PossibleFutons(int width,int height) {
        this.height = height;
        this.width = width;
        allFutons = new HashMap<>();
        setFutons = new HashMap<>();

        for (int y = 0 ; y < height ; ++y) {
            for (int x = 0 ; x < width ; ++x) {
                Point here = new Point(x,y);
                if (x < width-1) {
                    Point other = new Point(x+1,y);
                    FutonPair f1 = new FutonPair(here,other,width,height);
                    allFutons.put(f1.getUuid(),f1);
                    FutonPair f2 = new FutonPair(other,here,width,height);
                    allFutons.put(f2.getUuid(),f2);
                }
                if (y < height-1) {
                    Point other = new Point(x,y+1);
                    // this makes the north-pillowed futon, which is illegal...
                    //FutonPair f1 = new FutonPair(here,other,width,height);
                    //allFutons.put(f1.getUuid(),f1);
                    FutonPair f2 = new FutonPair(other,here,width,height);
                    allFutons.put(f2.getUuid(),f2);
                }
            }
        }
        cells = new CellContainer<FutonCell>(width,height,
                (x,y)->new FutonCell(),
                (x,y,r)->new FutonCell(r));

        for(FutonPair fpair : allFutons.values()) {
            cells.getCell(fpair.getPillow().x,fpair.getPillow().y).addPillow(fpair);
            cells.getCell(fpair.getFuton().x,fpair.getFuton().y).addFuton(fpair);
        }
    }

    public PossibleFutons(PossibleFutons right) {
        height = right.height;
        width = right.width;
        cells = new CellContainer<FutonCell>(right.cells);
        allFutons = FutonUtilities.futonPairMapDeepCopy(right.allFutons);
        setFutons = FutonUtilities.futonPairMapDeepCopy(right.setFutons);
    }

    public FutonCell getCell(int x,int y) { return cells.getCell(x,y); }
    public Collection<FutonPair> getSetFutons() { return setFutons.values(); }
    public boolean isSet(FutonPair f) { return setFutons.containsKey(f.getUuid()); }

    public void clear(FutonPair f) {
        FutonCell pillowCell = getCell(f.getPillow().x,f.getPillow().y);
        FutonCell futonCell = getCell(f.getFuton().x,f.getFuton().y);
        pillowCell.clearPillow(f);
        futonCell.clearFuton(f);
        allFutons.remove(f.getUuid());
    }

    public void set(FutonPair f) {
        Set<FutonPair> doomed = new HashSet<>();
        FutonCell pillowCell = getCell(f.getPillow().x,f.getPillow().y);
        for(FutonPair otherpillow : pillowCell.getPillows()) {
            if(otherpillow == f) continue;
            doomed.add(otherpillow);
        }
        FutonCell futonCell = getCell(f.getFuton().x,f.getFuton().y);
        for (FutonPair otherfuton : futonCell.getFutons()) {
            if (otherfuton == f) continue;
            doomed.add(otherfuton);
        }
        doomed.stream().forEach(df->clear(df));
        setFutons.put(f.getUuid(),f);
    }

}
