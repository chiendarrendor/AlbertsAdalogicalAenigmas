import grid.logic.LogicStep;

import java.util.Arrays;

public class CageLogicStep extends PermutationExhaustionLogicStep {
    Cage cage;
    int sumval;
    public CageLogicStep(Cage cage) {
        this.cage = cage;
        init(cage.cells);
        sumval = cage.size;
    }

    @Override boolean isPermutationValid(int[] permutation) {
        int total = Arrays.stream(permutation).sum();
        return total == sumval;
    }
}
