import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrowLogicStep extends PermutationExhaustionLogicStep {
    public ArrowLogicStep(Arrow arrow) {
        List<Point> points = new ArrayList<>();
        points.add(arrow.head);
        points.addAll(arrow.body);
        init(points);
    }

    @Override boolean isPermutationValid(int[] permutation) {
        int subtotal = Arrays.stream(permutation).skip(1).sum();
        return subtotal == permutation[0];
    }
}
