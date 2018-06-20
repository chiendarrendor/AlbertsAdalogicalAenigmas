package grid.assistant;

import grid.puzzlebits.Direction;

public class EdgeKey {
    private int x;
    private int y;
    private Direction d;

    public EdgeKey(int x,int y,Direction d) { this.x = x; this.y = y; this.d = d; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        EdgeKey oek = (EdgeKey)o;

        if (x != oek.x) return false;
        if (y != oek.y) return false;
        if (d != oek.d) return false;
        return true;
    }

    private final int PRIME = 31;
    public int hashCode() {
        int result = 1;
        result = PRIME * result + x;
        result = PRIME * result + y;
        result = PRIME * result + d.hashCode();
        return result;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Direction getD() { return d; }
}
