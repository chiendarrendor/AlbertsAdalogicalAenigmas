import grid.lambda.LambdaInteger;
import grid.logic.LogicStatus;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.Path;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;

public class MisalignmentLogicStep implements grid.logic.LogicStep<Board> {
    private enum CellType {
        UNCONNECTABLE,
        UNKNOWN,
        LOVER,
        MATE
    }

    private static class CellInfo {
        int num;
        CellType type;
        public CellInfo(CellType type,int num) { this.type = type; this.num = num; }
    }

    private static boolean isComplete(Board b,Path p) {
        if (b.isLover(p.endOne().x,p.endOne().y) && b.isMate(p.endTwo().x,p.endTwo().y)) return true;
        if (b.isLover(p.endTwo().x,p.endTwo().y) && b.isMate(p.endOne().x,p.endOne().y)) return true;
        return false;
    }

    private static boolean isLover(Board b,Path p) {
        if (b.isLover(p.endOne().x,p.endOne().y)) return true;
        if (b.isLover(p.endTwo().x,p.endTwo().y)) { p.reverse(); return true; }
        return false;
    }

    private static boolean isMate(Board b,Path p) {
        if (b.isMate(p.endOne().x,p.endOne().y)) return true;
        if (b.isMate(p.endTwo().x,p.endTwo().y)) { p.reverse(); return true; }
        return false;
    }

    // given a path, set EndOne with a CellInfo of type e1, number num, EndTwo with e2,num, and all internal cells
    // with UNCONNECTABLE

    private static void setPathInfo(CellContainer<CellInfo> cc,Path pth, CellType e1,CellType e2, int num) {
        for (Point p : pth) cc.setCell(p.x,p.y,new CellInfo(CellType.UNCONNECTABLE,num));
        cc.getCell(pth.endOne().x,pth.endOne().y).type = e1;
        cc.getCell(pth.endTwo().x,pth.endTwo().y).type = e2;
    }

    private static void setUnconnectable(CellContainer<CellInfo> cc,Path pth) {
        setPathInfo(cc,pth,CellType.UNCONNECTABLE,CellType.UNCONNECTABLE,0);
    }


    private static void setLoverPath(Board b,CellContainer<CellInfo> cc, Path pth) {
        setPathInfo(cc,pth,CellType.UNCONNECTABLE,CellType.LOVER,
                b.getLoverNumber(pth.endOne().x,pth.endOne().y) - pth.size()
        );
    }

    private static void setMatePath(CellContainer<CellInfo> cc,Path pth) {
        setPathInfo(cc,pth,CellType.UNCONNECTABLE,CellType.MATE,pth.size());
    }

    private static void setOpenPath(CellContainer<CellInfo> cc,Path pth) {
        setPathInfo(cc, pth, CellType.UNKNOWN, CellType.UNKNOWN, pth.size());
    }


    @Override public LogicStatus apply(Board thing) {
        // this LogicStep should be run after the PathsLogicStep, but before any other logic step that might
        // modify the board.  this logic-step is going to determine whether or not any unknown edges can be
        // marked as walls because of either
        // a) a lover-ended path would be made longer than feasible or
        // b) a lover connects to a lover or a mate to a mate.
        //
        // while this particular behavior can eventually be caught with enough Flattening/Guessing,
        // it looks like we need mechanisms to detect these things a little sooner.
        //
        // step 1. every space on the board needs to be designated with one of the following:
        // X           -- designation for an unconnectable path space (lover/mate terminal or internal)
        // U <number>  -- designation for the end of a path that doesn't connect to either a lover or a mate
        //                where <number> is the number of cells in the path.  An empty, unpathed space is U 1
        // L <number>  -- designation for the end of a path where the other end is a lover, and <number> is
        //                the desired lover length less the path length
        // M <number>  -- designation for the end of a path where the other end is a mate, and <number> is
        //                the path length.
        CellContainer<CellInfo> info = new CellContainer<CellInfo>(thing.getWidth(),thing.getHeight(),(x,y)->null);
        for (Path pth : thing.getPaths()) {
            if (isComplete(thing,pth)) setUnconnectable(info,pth);
            else if (isLover(thing,pth)) setLoverPath(thing,info,pth);
            else if (isMate(thing,pth)) setMatePath(info,pth);
            else setOpenPath(info,pth);
        }

        thing.forEachCell((x,y)-> {
            if (info.getCell(x,y) != null) return;
            if (thing.isSpace(x,y)) { info.setCell(x,y,new CellInfo(CellType.UNKNOWN,1)); return; }
            if (thing.isMate(x,y)) { info.setCell(x,y,new CellInfo(CellType.MATE,1)); return; }
            if (thing.isLover(x,y)) { info.setCell(x,y,new CellInfo(CellType.LOVER,thing.getLoverNumber(x,y) - 1)); return; }
            throw new RuntimeException("Why not?");
        });



        LambdaInteger changecounter = new LambdaInteger(0);
        thing.forEachEdge( (x,y,isV,old)-> {
            if (old != EdgeState.UNKNOWN) return;

            EdgeContainer.CellCoord cc = EdgeContainer.getCellCoord(x,y,isV);
            Point ocell = new Point(isV ? cc.x + 1 : cc.x , isV ? cc.y : cc.y + 1);
            CellInfo ci1 = info.getCell(cc.x,cc.y);
            CellInfo ci2 = info.getCell(ocell.x,ocell.y);

            // XX XU XL XM UX LX MX  anything that could connect to an unconnectable shouldn't
            if (ci1.type == CellType.UNCONNECTABLE || ci2.type == CellType.UNCONNECTABLE) {
                changecounter.inc();
                thing.setEdge(x,y,isV,EdgeState.WALL);
                return;
            }
            // UU  unknown connected to unknown is indeterminable
            if (ci1.type == CellType.UNKNOWN && ci2.type == CellType.UNKNOWN) return;
            // LL  lover connected to lover shouldnt
            if (ci1.type == CellType.LOVER && ci2.type == CellType.LOVER) {
                changecounter.inc();
                thing.setEdge(x,y,isV,EdgeState.WALL);
                return;
            }
            // MM mate connected to mate shouldn't
            if (ci1.type == CellType.MATE && ci2.type == CellType.MATE) {
                changecounter.inc();
                thing.setEdge(x,y,isV,EdgeState.WALL);
                return;
            }
            // UM MU  mate connected to unknown is indeterminable
            if (ci1.type == CellType.MATE && ci2.type == CellType.UNKNOWN) return;
            if (ci1.type == CellType.UNKNOWN && ci2.type == CellType.MATE) return;

            // UL if we attach an unknown to a lover, then we shouldn't run out of cells
            if (ci1.type == CellType.UNKNOWN && ci2.type == CellType.LOVER && ci2.num - ci1.num <= 0) {
                changecounter.inc();
                thing.setEdge(x,y,isV,EdgeState.WALL);
                return;
            }

            // LU if we attach an unknown to a lover, then we shouldn't run out of cells
            if (ci1.type == CellType.LOVER && ci2.type == CellType.UNKNOWN && ci1.num - ci2.num <= 0) {
                changecounter.inc();
                thing.setEdge(x,y,isV,EdgeState.WALL);
                return;
            }

            // LM if we attach a mate to an unknown, they should exactly run out
            if (ci1.type == CellType.LOVER && ci2.type == CellType.MATE && ci1.num - ci2.num != 0) {
                changecounter.inc();
                thing.setEdge(x,y,isV,EdgeState.WALL);
                return;
            }

            // ML if we attach a mate to an unknown, they should exactly run out
            if (ci1.type == CellType.MATE && ci2.type == CellType.LOVER && ci2.num - ci1.num != 0) {
                changecounter.inc();
                thing.setEdge(x,y,isV,EdgeState.WALL);
                return;
            }

        });

        if (changecounter.get() > 0) return LogicStatus.LOGICED;
        return LogicStatus.STYMIED;
    }
}
