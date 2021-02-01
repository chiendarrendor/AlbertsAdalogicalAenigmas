public class RegionId {
    final private char tag;
    final private int x;
    final private int y;
    public RegionId(char tag,int x,int y) { this.tag = tag; this.x = x; this.y = y; }
    public char getTag() { return tag; }
    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        RegionId orid = (RegionId)o;
        if (tag != orid.tag) return false;
        if (x != orid.x) return false;
        if (y != orid.y) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + tag;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }


    @Override
    public String toString() {
        return "RegionId: " + tag + " - " + x + "," + y;
    }
}
