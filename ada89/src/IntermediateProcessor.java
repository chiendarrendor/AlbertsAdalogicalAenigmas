import grid.logic.flatten.FlattenLogicer;

public class IntermediateProcessor implements FlattenLogicer.IntermediateCallback<Board> {
    int bestGrade = Integer.MAX_VALUE;
    Board bestBoard = null;


    @Override public void foundOne(Board state) {
        int grade = state.getUnknownCount();
        if (grade < bestGrade) {
            bestGrade = grade;
            bestBoard = state;
        }
    }
}
