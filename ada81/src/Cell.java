public class Cell {
    private boolean canBePositive = true;
    private boolean canBeNegative = true;
    private boolean canBeBlank = true;
    private int count = 3;

    private void setNotPositive() {
        if (canBePositive) {
            canBePositive = false;
            --count;
        }
    }
    private void setNotNegative() {
        if (canBeNegative) {
            canBeNegative = false;
            --count;
        }
    }
    private void setNotBlank() {
        if (canBeBlank) {
            canBeBlank = false;
            --count;
        }
    }

    public Cell() {}

    public Cell(Cell right) {
        canBePositive = right.canBePositive;
        canBeNegative = right.canBeNegative;
        canBeBlank = right.canBeBlank;
        count = right.count;
    }

    public boolean isBroken() { return count == 0; }
    public boolean isDone() { return count == 1; }
    public void clearPositive() { setNotPositive(); }
    public void clearNegative() { setNotNegative(); }
    public void clearBlank() { setNotBlank(); }
    public boolean canBePositive() { return canBePositive; }
    public boolean canBeNegative() { return canBeNegative; }
    public boolean canBeBlank() { return canBeBlank; }
    public boolean canBeMagnetic() { return canBeNegative || canBePositive; }
    public boolean isBlank() { return canBeBlank && !canBePositive && !canBeNegative; }
    public boolean isPositive() { return !canBeBlank && canBePositive && !canBeNegative; }
    public boolean isNegative() { return !canBeBlank && !canBePositive && canBeNegative; }
    public boolean isMagnetic() { return !canBeBlank && (canBePositive || canBeNegative); }


    public void setMagnetic() {
        if (!canBePositive() && !canBeNegative()) throw new RuntimeException("Cell Can't be magnetic!");
        setNotBlank();
    }

    public void setPositive() {
        if (!canBePositive())  throw new RuntimeException("Cell Can't be positive!");
        setNotNegative();
        setNotBlank();
    }
    public void setNegative() {
        if (!canBeNegative()) throw new RuntimeException("Cell Can't be negative");
        setNotPositive();
        setNotBlank();
    }
    public void setBlank() {
        if (!canBeBlank()) throw new RuntimeException("Cell can't be blank");
        setNotPositive();
        setNotNegative();
    }


    // LineLogicStep is agnostic of positive/negative, except when it is.
    // these will only operate on MoveTye POSITIVE and NEGATIVE
    public boolean is(MoveType t) { return t==MoveType.POSITIVE ? isPositive() : isNegative(); }
    public boolean canBe(MoveType t) { return t == MoveType.POSITIVE ? canBePositive() : canBeNegative(); }
    public void clear(MoveType t) { if (t == MoveType.POSITIVE)  clearPositive(); else clearNegative(); }
    public void set(MoveType t) { if (t == MoveType.POSITIVE) setPositive(); else setNegative(); }


}
