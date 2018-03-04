public enum CellState {
    EMPTY,          // nothing is known of this cell
    INITIAL,        // a circle is here and has not moved yet
    TERMINAL,       // a circle's final resting place is here
    PATH            // a circle passed through here to a destination
}
