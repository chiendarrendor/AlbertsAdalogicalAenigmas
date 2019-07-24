public enum CellState {
    HORIZONTAL,
    VERTICAL,
    UNKNOWN;


    public CellState getOpp() {
        switch(this) {
            case HORIZONTAL: return VERTICAL;
            case VERTICAL: return HORIZONTAL;
            case UNKNOWN: throw new RuntimeException("UNKNOWN doesn't have an opposite!");
            default: throw new RuntimeException("This should never happen!");
        }
    }

}
