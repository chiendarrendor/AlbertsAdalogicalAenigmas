import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import org.omg.CORBA.UNKNOWN;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ArrowLogicStep implements LogicStep<Board> {
    int size;
    List<Point> targets;

    public ArrowLogicStep(int size, List<Point> arrowtargets) {
        this.size = size;
        this.targets = arrowtargets;
    }

    List<Point> unknowns = new ArrayList<>();
    public LogicStatus apply(Board thing) {
        int numwalls = 0;
        int numpaths = 0;
        unknowns.clear();

        for (Point p : targets) {
            switch(thing.getCell(p.x,p.y)) {
                case UNKNOWN: unknowns.add(p); break;
                case PATH: ++numpaths; break;
                case WALL: ++numwalls; break;
                case ARROW: break;
            }
        }

        if (numwalls > size) return LogicStatus.CONTRADICTION;
        if (numwalls + unknowns.size() < size) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (numwalls == size) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.PATH));
            return LogicStatus.LOGICED;
        }

        if (numwalls + unknowns.size() == size) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.WALL));
            return LogicStatus.LOGICED;
        }

        LogicStatus result = LogicStatus.STYMIED;
        // before we do unknown-block analysis, let's make sure that all wall-adjacents are
        // paths and no two walls are adjacent.
        for (int i = 0 ; i < targets.size() ; ++i) {
            Point curp = targets.get(i);
            if (thing.getCell(curp.x,curp.y) != CellState.WALL) continue;
            if (i-1 >= 0) {
                Point bp = targets.get(i-1);
                if (thing.getCell(bp.x,bp.y) == CellState.WALL) return LogicStatus.CONTRADICTION;
                if (thing.getCell(bp.x,bp.y) == CellState.UNKNOWN) {
                    thing.setCell(bp.x,bp.y,CellState.PATH);
                    result = LogicStatus.LOGICED;
                }
            }
            if (i+1 <= targets.size() - 1) {
                Point np = targets.get(i+1);
                if (thing.getCell(np.x,np.y) == CellState.WALL) return LogicStatus.CONTRADICTION;
                if (thing.getCell(np.x,np.y) == CellState.UNKNOWN) {
                    thing.setCell(np.x,np.y,CellState.PATH);
                    result = LogicStatus.LOGICED;
                }
            }
        }

        int numwall = 0;
        List<Point> curblock = null;
        List<List<Point>> blocks = new ArrayList<>();

        for (Point p : targets) {
            CellState cs = thing.getCell(p.x,p.y);
            if (cs == CellState.WALL) ++numwall;

            if (curblock == null && cs == CellState.UNKNOWN) {
                curblock = new ArrayList<>();
                blocks.add(curblock);
            }
            if (curblock != null && cs == CellState.UNKNOWN) {
                curblock.add(p);
            }
            if (curblock != null && cs != CellState.UNKNOWN) {
                curblock = null;
            }
        }

        int maxwallcount = numwall;
        for (List<Point> block : blocks) {
            maxwallcount += (block.size()+1) / 2;
        }

        if (maxwallcount < size) return LogicStatus.CONTRADICTION;
        if (maxwallcount > size) return result;

        for (List<Point> block : blocks) {
            if (block.size() % 2 == 0) continue;
            for (int i = 0 ; i < block.size() ; i += 2) {
                Point cp = block.get(i);
                thing.setCell(cp.x,cp.y,CellState.WALL);
                result = LogicStatus.LOGICED;
            }
        }

        return result;
    }
}
