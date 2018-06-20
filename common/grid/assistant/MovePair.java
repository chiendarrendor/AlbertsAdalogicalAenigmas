package grid.assistant;

public class MovePair<T> {
    AssistantMove<T> move;
    AssistantMove<T> antimove;
    public MovePair(AssistantMove<T> move, AssistantMove<T> antimove) { this.move = move; this.antimove = antimove; }
    public MovePair() { move = null ; antimove = null; }
    public boolean isNoOp() { return move == null && antimove == null; }
}
