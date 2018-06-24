import grid.puzzlebits.Direction;

public class ArrowInfo {
    Direction d;
    int size;

    public ArrowInfo(String s) {
        size = Integer.parseInt(s.substring(0,1));
        d = Direction.fromShort(s.substring(1,2));
    }
}
