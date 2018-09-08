package OnTheFlyAStar;

import java.util.List;

public interface AStarNode<T> {
    // a number greater than 0 that determines what the winning grade is of
    // an object successored from this objects parent.
    int winGrade();
    // a number indicating how far towards the goal we have come.
    // a number less than 0 or greater than winGrade represents an invalid state and
    // will not be further processed.
    int getGrade();
    // because we don't build out the graph ahead of time, we may run across the same
    // position from multiple beginnings.  We only want to process each state once,
    // so the canonical key of two states should be identical IFF they would be
    // considered the same node on the hypothetical built-out graph
    String getCanonicalKey();

    List<T> successors();
}
