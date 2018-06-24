package grid.logic.flatten;

import grid.logic.LogicStatus;

public class FlattenDeep {

    public static <T extends FlattenSolvable<T>> FlattenLogicer.RecursionStatus applyLogic(FlattenLogicer<T> logicer, T thing) {

        FlattenLogicer.RecursionStatus r1 = logicer.recursiveApplyLogic(thing);
        if (r1 != FlattenLogicer.RecursionStatus.GO) return r1;

        while(true) {
            LogicStatus lstat = logicer.applyTupleSuccessors(thing);
            if (lstat == LogicStatus.CONTRADICTION) return FlattenLogicer.RecursionStatus.DEAD;
            if (lstat == LogicStatus.STYMIED) return FlattenLogicer.RecursionStatus.GO;

            FlattenLogicer.RecursionStatus r2 = logicer.recursiveApplyLogic(thing);
            if (r2 != FlattenLogicer.RecursionStatus.GO) return r2;
        }
    }
}
