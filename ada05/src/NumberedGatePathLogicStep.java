import grid.logic.LogicStatus;
import grid.puzzlebits.Path.Path;
import org.omg.CORBA.UNKNOWN;

import java.awt.Point;
import java.util.Iterator;

public class NumberedGatePathLogicStep implements grid.logic.LogicStep<Board> {

    private static enum OpDir { UNKNOWN, INCREMENTING, DECREMENTING };

    private static class OpDirBlock {
        public OpDir opdir;
    }

    private static boolean isStart(Board thing,Point p) {
        return thing.getStartCell().equals(p);
    }

    private static boolean isSpecial(Board thing, Point p) {
        if (thing.getStartCell().equals(p)) return true;
        if (thing.isGate(p.x,p.y) && thing.getGate(p.x,p.y).isNumbered()) return true;
        return false;
    }

    private static boolean isInterGate(Board thing, Point p) {
        return thing.isGate(p.x,p.y) && ! thing.getGate(p.x,p.y).isNumbered();
    }


    // given two specials, a count of intervening non-numbered gates, and possibly a increment/decrement status
    // determine if the two specials are a legal number of gates apart
    // we can assume that at most one of (start,end) is actually the starting block
    private static LogicStatus checkSpecials(Board thing, Point startspecial, Point endspecial, int intercount, OpDirBlock odb) {
        boolean isincrement = false;
        boolean isdecrement = false;
        if (isStart(thing,startspecial)) {
            int onum = thing.getGate(endspecial.x, endspecial.y).getNumber();
            isincrement = (onum - intercount == 1);
            isdecrement = (thing.getGateCount() - (onum + intercount) == 0);
        } else if (isStart(thing,endspecial)) {
            int onum = thing.getGate(startspecial.x, startspecial.y).getNumber();
            isincrement = (thing.getGateCount() - (onum + intercount) == 0);
            isdecrement = (onum - intercount == 1);
        } else {
            int num1 = thing.getGate(startspecial.x, startspecial.y).getNumber();
            int num2 = thing.getGate(endspecial.x, endspecial.y).getNumber();
            isincrement = (num2 - num1 - intercount == 1);
            isdecrement = (num1 - num2 - intercount == 1);
        }

        if (isincrement && isdecrement) throw new RuntimeException("Can't get increment and decrement1");
        if (!isincrement && !isdecrement) return LogicStatus.CONTRADICTION;
        if (odb.opdir == OpDir.INCREMENTING) return isincrement ? LogicStatus.STYMIED : LogicStatus.CONTRADICTION;
        if (odb.opdir == OpDir.DECREMENTING) return isdecrement ? LogicStatus.STYMIED : LogicStatus.CONTRADICTION;

        odb.opdir = isincrement ? OpDir.INCREMENTING : OpDir.DECREMENTING;
        return LogicStatus.STYMIED;
    }




    @Override public LogicStatus apply(Board thing) {
        OpDirBlock odb = new OpDirBlock();

        for (Path path : thing.getPaths()) {
            odb.opdir = OpDir.UNKNOWN;
            Point endone = path.endOne();
            Path.Cursor pcursor = path.getCursor(endone.x,endone.y);

            Point firstspecial = null;
            Point startspecial = null;
            Point endspecial = null;
            int intercount = 0;

            // states:
            // startspecial and endspecial are both null ..haven't seen a special yet.
            // startspecial is not null, but end is... we are looking for an end special.
            // startspecial and endspecial are null .. we just found an end for our start.

            while(true) {
                Point curpoint = pcursor.get();
                if (isSpecial(thing,curpoint)) {
                    if (firstspecial == null) firstspecial = curpoint;

                    if (startspecial == null) {
                        startspecial = curpoint;
                        intercount = 0;
                    } else if (endspecial == null) {
                        endspecial = curpoint;
                    }
                }
                if (isInterGate(thing,curpoint)) ++intercount;

                if (startspecial != null && endspecial != null) {
                    if (checkSpecials(thing,startspecial,endspecial,intercount,odb) == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                    startspecial = endspecial;
                    if (endspecial == firstspecial) break;
                    endspecial = null;
                    intercount = 0;
                }

                if (!pcursor.hasNext()) break;
                pcursor.next();
            }


        }

        return LogicStatus.STYMIED;
    }
}
