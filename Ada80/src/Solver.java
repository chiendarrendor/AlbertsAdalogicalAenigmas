import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        for(int postid : b.getPostIds()) {
            addLogicStep(new PostLogicStep(b.getPost(postid)));
        }
        addLogicStep(new NoEmptyPenLogicStep());
        addLogicStep(new IsolationLogicStep());
        addLogicStep(new NoLoopsLogicStep());
    }
}
