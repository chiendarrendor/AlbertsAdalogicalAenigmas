import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.ArrayList;
import java.util.List;

public class PostLogicStep implements LogicStep<Board> {
    int postid;
    
    public PostLogicStep(Post post) { postid = post.id; }

    @Override public LogicStatus apply(Board thing) {
        Board.PostCountState pcs = thing.getPostCountState(postid);
        
        // # paths ->
        // # walls v
        //    0 1 2 3 4
        // 0  . . . w Z
        // 1  . . v B x
        // 2  y y Y x x
        // 3  A A x x x
        // 4  A x x x x
        // x = not possible
        // A contradiction -- too many talls
        // B contradiction -- only one wall, and no paths
        // Y OK ... 2 paths 2 walls
        // Z OK .. no paths is ok
        // y 2 walls, make unknowns paths
        // v 2 paths, 1 wall, make unknown wall
        // w 3 paths, make unknown path
        
        if (pcs.getWallCount() > 2) return LogicStatus.CONTRADICTION; // A
        if (pcs.getWallCount() == 1 && pcs.pathcount == 3) return LogicStatus.CONTRADICTION; // B
        if (pcs.getUnknownSize() == 0) return LogicStatus.STYMIED; // Y,Z
        
        // y
        if (pcs.getWallCount() == 2) {
            pcs.unknowns.stream().forEach(fid->thing.setFenceState(fid,EdgeType.PATH));
            return LogicStatus.LOGICED;
        }
        
        // v
        if (pcs.getWallCount() == 1 && pcs.pathcount == 2) {
            pcs.unknowns.stream().forEach(fid->thing.setFenceState(fid,EdgeType.WALL));
            return LogicStatus.LOGICED;
        }
        
        if (pcs.pathcount == 3) {
            pcs.unknowns.stream().forEach(fid->thing.setFenceState(fid,EdgeType.PATH));
            return LogicStatus.LOGICED;
        }
        
        return LogicStatus.STYMIED;
    }
}
