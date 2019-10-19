import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;

public class PathInfo {
    Path p;
    boolean isLoop;
    boolean end1Terminated;
    boolean end2Terminated;
    int end1Clue;
    int end2Clue;
    int bendcount;

    public PathInfo(Board b,Path p) {
        this.p = p;

        if (p.isClosed()) {
            isLoop = true;
            return;
        }

        if (b.hasClue(p.endOne().x,p.endOne().y)) {
            end1Terminated = true;
            end1Clue = b.getClue(p.endOne().x,p.endOne().y);
        }

        if (b.hasClue(p.endTwo().x,p.endTwo().y)) {
            end2Terminated = true;
            end2Clue = b.getClue(p.endTwo().x,p.endTwo().y);
        }

        bendcount = 0;

        Path.Cursor pc = p.getCursor(p.endOne().x,p.endOne().y);
        pc.next();

        while(pc.hasNext()) {
            if (Turns.isBend(Turns.makeTurn(pc.getPrev(),pc.get(),pc.getNext()))) ++bendcount;
            pc.next();
        }
    }
}
