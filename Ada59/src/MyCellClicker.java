import grid.assistant.AssistantMove;
import grid.assistant.CellClicker;
import grid.spring.ClickInfo;
import grid.assistant.MovePair;

import javax.swing.SwingUtilities;

public class MyCellClicker implements CellClicker<Board> {
    @Override public MovePair<Board> click(Board orig, Board cur, ClickInfo ci) {
        Cell origcell = orig.getCell(ci.cellx,ci.celly);
        Cell curcell = cur.getCell(ci.cellx,ci.celly);
        if (origcell == null) return null;
        int clickx = ci.dxloc / (ci.cellwidth / 3);
        int clicky = ci.dyloc / (ci.cellheight / 3);
        int clicksum = clickx * 10 + clicky;
        boolean isLeftClick = SwingUtilities.isLeftMouseButton(ci.me);

        int opid = -1;
        switch(clicksum) {
            case 0:
            case 10:
                return null;
            case 1:  opid = 1; break;
            case 2:  opid = 4; break;
            case 11: opid = 2; break;
            case 12: opid = 5; break;
            case 20: opid = Cell.WALLID; break;
            case 21: opid = 3; break;
            case 22: opid = 6; break;
            default:
                throw new RuntimeException("How did you possibly click there? " + clicksum);
        }

        AssistantMove<Board> setmove = new Board.MyMove(ci.cellx,ci.celly,true,opid);
        AssistantMove<Board> clearmove = new Board.MyMove(ci.cellx,ci.celly,false,opid);


        if (isLeftClick) {
            if (!origcell.contains(opid)) return null;
            return new MovePair<Board>(setmove,clearmove);
        } else {
            if (origcell.contains(opid) && origcell.isComplete()) return null;
            return new MovePair<Board>(clearmove,setmove);
        }
    }
}
