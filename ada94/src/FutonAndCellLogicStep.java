import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.HashSet;
import java.util.Set;

public class FutonAndCellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public FutonAndCellLogicStep(int x, int y) { this.x = x; this.y = y; }

    // not reentrant...but that's okay, was never designed to be...
    Set<FutonPair> doomed = new HashSet<>();
    private void clearDoomed() { doomed.clear(); }

    private LogicStatus clearPillows(PossibleFutons possibles, FutonCell futoncell) {
        LogicStatus result = LogicStatus.STYMIED;
        for(FutonPair fpair : futoncell.getPillows()) {
            if (possibles.isSet(fpair)) return LogicStatus.CONTRADICTION;
            doomed.add(fpair);
            result = LogicStatus.LOGICED;
        }
        return result;
    }

    private LogicStatus clearFutons(PossibleFutons possibles, FutonCell futoncell) {
        LogicStatus result = LogicStatus.STYMIED;
        for (FutonPair fpair : futoncell.getFutons()) {
            if (possibles.isSet(fpair)) return LogicStatus.CONTRADICTION;
            doomed.add(fpair);
            result = LogicStatus.LOGICED;
        }
        return result;
    }



    @Override public LogicStatus apply(Board thing) {
        Cell cell = thing.getCell(x,y);
        FutonCell futoncell = thing.getFutonCell(x,y);
        PossibleFutons possibles = thing.getPossibles();
        LogicStatus result = LogicStatus.STYMIED;
        clearDoomed();

        if (!cell.isValid()) return LogicStatus.CONTRADICTION;
        if (cell.isDone()) {
            if (cell.getDoneType() == CellType.PILLAR || cell.getDoneType() == CellType.AISLE || cell.getDoneType() == CellType.FUTON) {
                LogicStatus fresult = clearPillows(possibles,futoncell);
                if (fresult == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (fresult == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
            if (cell.getDoneType() == CellType.PILLAR || cell.getDoneType() == CellType.AISLE || cell.getDoneType() == CellType.PILLOW) {
                LogicStatus fresult = clearFutons(possibles,futoncell);
                if (fresult == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (fresult == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
            doomed.stream().forEach(fp->possibles.clear(fp));
            // so if we get here, all impossible FutonPairs have been removed from this cell.
            // if type was pillar or aisle, we're done (and literally forever, there's nothing more to be done for this cell!)
            if (cell.getDoneType() == CellType.PILLAR || cell.getDoneType() == CellType.AISLE) return result;
            // if we get here, our done type is either Pillow or Futon
            if (cell.getDoneType() == CellType.PILLOW) {
                if (futoncell.getPillows().size() == 0) return LogicStatus.CONTRADICTION;
                if (futoncell.getPillows().size() > 1) return result;
                FutonPair theFuton = futoncell.getPillows().iterator().next();
                possibles.set(theFuton);
                // so the set will take care of both ends of the futon in futon space.
                // if we're here, this cell is taken care of too, we just have to set the futon end.
                Cell futonEnd =  thing.getCell(theFuton.getFuton().x,theFuton.getFuton().y);
                if (!futonEnd.isValid()) return LogicStatus.CONTRADICTION;
                if (!futonEnd.has(CellType.FUTON)) return LogicStatus.CONTRADICTION;
                if (!futonEnd.isDone()) result = LogicStatus.LOGICED;
                futonEnd.set(CellType.FUTON);
                return result;
            }
            if (cell.getDoneType() == CellType.FUTON) {
                if (futoncell.getFutons().size() == 0) return LogicStatus.CONTRADICTION;
                if (futoncell.getFutons().size() > 1) return result;
                FutonPair theFuton = futoncell.getFutons().iterator().next();
                possibles.set(theFuton);
                Cell pillowEnd = thing.getCell(theFuton.getPillow().x,theFuton.getPillow().y);
                if (!pillowEnd.isValid()) return LogicStatus.CONTRADICTION;
                if (!pillowEnd.has(CellType.PILLOW)) return LogicStatus.CONTRADICTION;
                if (!pillowEnd.isDone()) result = LogicStatus.LOGICED;
                pillowEnd.set(CellType.PILLOW);
                return result;
            }
            throw new RuntimeException("We should never get here!");
        }
        // we should only get here if our cell is not done. (which, by the way, means that PILLAR is not one of the possibles.
        if (!cell.has(CellType.PILLOW)) {
            LogicStatus fresult = clearPillows(possibles,futoncell);
            if (fresult == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (fresult == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }
        if (!cell.has(CellType.FUTON)) {
            LogicStatus fresult = clearFutons(possibles,futoncell);
            if (fresult == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (fresult == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }
        doomed.stream().forEach(fp->possibles.clear(fp));

        // at this point, if cell can't be either pillow or futon, then we've cleared all those futons from futon space
        // let's look in the other directions:
        if (futoncell.getFutons().size() == 0 && cell.has(CellType.FUTON)) {
            cell.clear(CellType.FUTON);
            result = LogicStatus.LOGICED;
        }

        if (futoncell.getPillows().size() == 0 && cell.has(CellType.PILLOW)) {
            cell.clear(CellType.PILLOW);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
