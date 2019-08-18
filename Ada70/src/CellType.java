public class CellType {
    private PathType pathtype;
    private PresenceType presencetype;

    public PathType getPathType() { return pathtype; }
    public PresenceType getPresenceType() { return presencetype; }

    public CellType(PathType pathtype) { this.pathtype = pathtype; this.presencetype = PresenceType.UNKNOWN; }
    public CellType(PathType pathtype, PresenceType presencetype) { this.pathtype = pathtype; this.presencetype = presencetype; }
    public CellType(CellType parent,PathType pathtype) { this.pathtype = pathtype; this.presencetype = parent.getPresenceType(); }
    public CellType(CellType parent,PresenceType presencetype) { this.presencetype = presencetype; this.pathtype = parent.getPathType(); }

    public final static CellType INITIAL = new CellType(PathType.INITIAL);
    public final static CellType INITIALFORBIDDEN  = new CellType(PathType.INITIAL,PresenceType.FORBIDDEN);
    public final static CellType EMPTY = new CellType(PathType.EMPTY);

    // is cell complete? (illegal states are incomplete)
    // PATH         PRESUnk     PRESReq     PRESForb    got
    // EMPTY        y           n           y
    // INITIAL      n           n           n           xxx
    // INITEMPT     y           Xn          y            x
    // TERMINAL     y           y           Xn            x
    // VERTICAL     y           Xn          y            x
    // HORIZ        y           Xn          y            x
    public boolean isComplete() {
        if (isIllegal()) return false;
        if (pathtype == PathType.INITIAL) return false;
        if (pathtype == PathType.EMPTY  && presencetype == PresenceType.REQUIRED) return false;
        return true;
    }

    public boolean isIllegal() {
        if (pathtype == PathType.TERMINAL && presencetype == PresenceType.FORBIDDEN) return true;
        if ((pathtype == PathType.INITIALEMPTY ||
                pathtype == PathType.VERTICAL || pathtype == PathType.HORIZONTAL) &&
                presencetype == PresenceType.REQUIRED) return true;
        return false;
    }

    // assumes that the cell you are asking after is the base cell of the non-moving non-processed jump
    // so pathtype is INITIAL
    public boolean canRemain() { return presencetype != PresenceType.FORBIDDEN; }

    // can a jump land on this space? (non-moving case handled in canRemain)
    // PATH         PRESUnk     PRESReq     PRESForb    got
    // EMPTY        y           y           n
    // INITIAL      n           n           n
    // INITEMPT     n           n           n
    // TERMINAL     n           n           n
    // VERTICAL     n           n           n
    // HORIZ        n           n           n
    public boolean canLand() { return pathtype == PathType.EMPTY && presencetype != PresenceType.FORBIDDEN; }

    // can a jump pass over this space?
    // PATH         PRESUnk     PRESReq     PRESForb    got
    // EMPTY        y           n           y
    // INITIAL      n           n           n
    // INITEMPT     n           n           n
    // TERMINAL     n           n           n
    // VERTICAL     n           n           n
    // HORIZ        n           n           n


    public boolean canPass() { return pathtype == PathType.EMPTY && presencetype != PresenceType.REQUIRED; }
}
