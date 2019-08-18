import java.util.ArrayList;
import java.util.List;

public class JumpList {
    private List<Jump> jumps = new ArrayList<>();

    public void addJump(Jump j) { jumps.add(j); }
    public List<Jump> stillValidJumps(Board b) {
        List<Jump> result = new ArrayList<>();
        for (Jump j : jumps) {
            JumpSet js = b.getJumpSet(j.base.x,j.base.y);
            if (js.contains(j)) result.add(j);
        }
        return result;
    }
}
