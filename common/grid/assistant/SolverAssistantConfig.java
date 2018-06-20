package grid.assistant;

import grid.logic.flatten.FlattenLogicer;
import grid.spring.GridPanel;

public interface SolverAssistantConfig<T extends AssistantBoard<T>> {
    GridPanel.GridListener getGridListener(BoardHolder<T> holder);
    GridPanel.EdgeListener getEdgeListener(BoardHolder<T> holder);
    CellClicker getCellClicker();
    EdgeClicker getEdgeClicker();
    FlattenLogicer<T> getLogicer();
    String serialize(AssistantMove<T> move);
    AssistantMove<T> deserialize(String s);
    T getInitialBoard();
    void displaySolution(T solution);
}
