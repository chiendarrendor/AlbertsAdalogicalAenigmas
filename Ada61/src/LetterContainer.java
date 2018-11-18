import grid.puzzlebits.Direction;

public class LetterContainer implements Comparable<LetterContainer> {
    int x;
    int y;
    char letter;
    Direction d;
    public LetterContainer(int x, int y, char letter, Direction d) {this.x = x; this.y = y; this.letter = letter; this.d = d; }

    // returns 0,1,or 2 for the direction-height of this direction (smaller is higher)
    private int dirHeight(Direction d) {
        if (d == null) return 1;
        switch(d) {
            case NORTH:
            case NORTHEAST:
            case NORTHWEST:
                return 0;
            case EAST:
            case WEST:
                return 1;
            case SOUTHEAST:
            case SOUTH:
            case SOUTHWEST:
                return 2;
            default: throw new RuntimeException("Why not?");
        }

    }


    @Override public int compareTo(LetterContainer o) {
        if (y != o.y) return Integer.compare(y,o.y);
        if (dirHeight(d) != dirHeight(o.d)) return Integer.compare(dirHeight(d),dirHeight(o.d));
        return Integer.compare(x,o.x);
    }
}
