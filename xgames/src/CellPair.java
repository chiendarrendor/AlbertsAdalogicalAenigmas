public class CellPair {
    StripPtr whiteStrip = null;
    StripPtr greyStrip = null;

    public CellPair() {}
    public CellPair(CellPair right) { this.whiteStrip = right.whiteStrip; this.greyStrip = right.greyStrip; }
}

