public enum CellType {
    TERMINAL(true),
    STRAIGHT(true),
    BEND(true),
    UNKNOWN(false),
    NOTBEND(false),
    NOTTERMINAL(false);

    private boolean isFinal;
    CellType(boolean isFinal) { this.isFinal = isFinal; }
    public boolean isFinal() { return isFinal; }

    public boolean isLike(CellType other) {
        if (this == other) return true;
        if (other.isFinal()) return false;
        if (other == UNKNOWN) return false;
        if (other == NOTBEND) return this == TERMINAL || this == STRAIGHT;
        if (other == NOTTERMINAL) return this == STRAIGHT || this == BEND;
        throw new RuntimeException("Should not get here");
    }
}
