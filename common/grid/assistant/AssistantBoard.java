package grid.assistant;

import grid.logic.flatten.FlattenSolvable;

public interface AssistantBoard<T> extends FlattenSolvable<T> {
    T clone();
}
