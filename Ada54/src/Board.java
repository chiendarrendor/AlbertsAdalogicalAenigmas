import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.GridPathContainer;
import grid.puzzlebits.Path.Path;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Board implements MultiFlattenSolvable<Board> {
    private GridFileReader gfr;
    private CellContainer<Direction> arrows;
    private CellContainer<Set<Direction>> winds;
    private CellContainer<Boolean> complete;
    private EdgeContainer<EdgeInfo> edges;
    private GridPathContainer paths;
    private Point start;
    private int unknowns;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        int[] inc = new int[1];
        inc[0] = 0;
        complete = new CellContainer<Boolean>(getWidth(),getHeight(),
                (x,y)-> {
                    if (hasBlock(x,y)) return true;
                    ++inc[0];
                    return false;
                },
                (x,y,r) -> new Boolean(r)
        );
        unknowns = inc[0];

        paths = new GridPathContainer(getWidth(),getHeight(),(x,y,cell)-> {
            if (cell.getInternalPaths().size() > 0) throw new BadMergeException("merging with middle of other path!");
            if (cell.getTerminalPaths().size() > 2) throw new BadMergeException("merging more than two!");
            if (cell.getTerminalPaths().size() == 1) return;

            Path p1 = cell.getTerminalPaths().get(0);
            Path p2 = cell.getTerminalPaths().get(1);

            if (p1 == p2) cell.closeLoop(p1);
            else cell.merge(p1,p2);
        });


        arrows = new CellContainer<Direction>(getWidth(),getHeight(),
                (x,y) -> {
                    String dir = gfr.getBlock("ARROWS")[x][y];
                    if (dir.charAt(0) == '.') return null;
                    return Direction.fromShort(dir);
                },
                (x,y,r) -> r
        );

        winds = new CellContainer<Set<Direction>>(getWidth(),getHeight(),
                (x,y) -> new HashSet<Direction>(),
                (x,y,r) -> r
        );

        edges = new EdgeContainer<EdgeInfo>(getWidth(),getHeight(),
                new EdgeInfo(), // this constructor form sets a blank (wall) EdgeInfo for all outer edges.
                (x,y,isV) -> new EdgeInfo(isV ? Direction.EAST : Direction.NORTH),
                (x,y,isV,old) -> new EdgeInfo(old)
        );

        start = new Point(Integer.parseInt(gfr.getVar("STARTX")),Integer.parseInt(gfr.getVar("STARTY")));

        // processing of blocks, both with and without arrows
        forEachCell((x,y) -> {
           if (!hasBlock(x,y)) return;
           // clear all edges of the block.
           Arrays.stream(Direction.orthogonals()).forEach((dir) -> getEdge(x,y,dir).clear());

           Direction ard = getArrow(x,y);
           if (ard == null) return;

           blow(x,y,ard);

        });


    }

    public Board(Board right) {
        gfr = right.gfr;
        arrows = right.arrows;
        start = right.start;
        winds = right.winds;
        edges = new EdgeContainer<EdgeInfo>(right.edges);
        complete = new CellContainer<Boolean>(right.complete);
        unknowns = right.unknowns;
        paths = new GridPathContainer(right.paths);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public GridFileReader getReader() { return gfr; }
    public char getLetter(int cx, int cy) { return gfr.getBlock("LETTERS")[cx][cy].charAt(0); }
    public boolean hasLetter(int cx, int cy) { return getLetter(cx,cy) != '.'; }
    public boolean hasBlock(int x,int y) { return gfr.getBlock("BLOCKS")[x][y].charAt(0) == '#'; }
    public boolean hasBlock(Point p) { return hasBlock(p.x,p.y); }
    public Direction getArrow(int x,int y) { return arrows.getCell(x,y); }
    public Point getStartPoint() { return start; }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public EdgeInfo getEdge(int x,int y,Direction d) { return edges.getEdge(x,y,d); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl);}
    public Stream<Point> stream() { return CellLambda.stream(getWidth(),getHeight()); }
    public Set<Direction> getWinds(int x,int y) { return winds.getCell(x,y); }
    public void setCellComplete(int x,int y) {
        --unknowns;
        complete.setCell(x,y,true);
    }

    public boolean isCellComplete(int x,int y) { return complete.getCell(x,y); }

    private void addPath(int x,int y,Direction d) {
        Point p1 = new Point(x,y);
        Point p2 = d.delta(p1,1);
        paths.link(p1,p2);
    }

    public void useEdge(int x,int y,Direction d) {
        EdgeInfo ei = getEdge(x,y,d);
        ei.douse();
        addPath(x,y,d);
    }


    public GridPathContainer getPaths() { return paths; }


    // going in the direction of d until we hit an edge or a block,
    // all edges going the opposite direction must be made impossible.
    private void blow(int x,int y,Direction d) {
        Point sp = new Point(x,y);
        for (int i = 1 ; ; ++i) {
            Point np = d.delta(sp,i);
            if (!onBoard(np)) break;
            if (hasBlock(np)) break;
            winds.getCell(np.x,np.y).add(d);
            getEdge(np.x,np.y,d.getOpp()).removeDirection(d.getOpp());
        }
    }




    // --------------------- FlattenSolvable requires --------------------------
    public boolean isComplete() { return unknowns == 0; }

    private static class MyMove {
        private Point cell;
        private Direction opdir;
        private EdgeSynopsis antiSynopsis;

        public MyMove(Point cell,Direction opdir,EdgeSynopsis antiSynopsis) {
            this.cell = cell;
            this.opdir = opdir;
            this.antiSynopsis = antiSynopsis;
        }


        // an edge will be guessed with three values:
        // WALL, USED_IN, and USED_OUT
        // the MyMove object will actuate the corresponding antimove for the given move.
        public boolean applyMove(Board b) {
            EdgeInfo ei = b.getEdge(cell.x,cell.y,opdir);
            switch(antiSynopsis) {
                case WALL:
                    // if we guessed wall and were wrong, then the edge must transit a path
                    if (ei.isWall()) return false;
                    b.useEdge(cell.x,cell.y,opdir);
                    break;
                case USED_IN:
                    if (ei.getSynopsis(opdir) == EdgeSynopsis.USED_IN) return false;
                    ei.removeDirection(opdir.getOpp());
                    break;
                case USED_OUT:
                    if (ei.getSynopsis(opdir) == EdgeSynopsis.USED_OUT) return false;
                    ei.removeDirection(opdir);
                    break;
                default: throw new RuntimeException("Why did you try to guess that?");
            }
            return true;
        }
    }

    public boolean applyMove(Object o) {
        MyMove mm = (MyMove)o;
        return mm.applyMove(this);
    }

    @Override
    public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x,int y) {
        return getTuplesForCellWithEdges(x,y,Direction.EAST,Direction.SOUTH);
    }


    public List<FlattenSolvableTuple<Board>> getTuplesForCellWithEdges(int x, int y,Direction ... dirs) {
        List<FlattenSolvableTuple<Board>> tuples = new ArrayList<>();
        for (Direction d : dirs ) {
            EdgeInfo ei = getEdge(x,y,d);
            EdgeSynopsis syn = ei.getSynopsis(d);

            if (syn == EdgeSynopsis.WALL || syn == EdgeSynopsis.USED_IN || syn == EdgeSynopsis.USED_OUT) continue;

            FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
            tuples.add(fst);

            if (syn != EdgeSynopsis.USED_UNKNOWN) {
                Board b = new Board(this);
                MyMove mm = new MyMove(new Point(x,y),d,EdgeSynopsis.WALL);
                EdgeInfo nei = b.getEdge(x,y,d);
                nei.clear();
                fst.addTuple(b,mm);
            }

            if (syn != EdgeSynopsis.POSSIBLE_OUT) {
                Board b = new Board(this);
                MyMove mm = new MyMove(new Point(x, y), d, EdgeSynopsis.USED_IN);
                EdgeInfo nei = b.getEdge(x, y, d);
                nei.removeOutbound(d);
                b.useEdge(x,y,d);
                fst.addTuple(b, mm);
            }

            if (syn != EdgeSynopsis.POSSIBLE_IN) {
                Board b = new Board(this);
                MyMove mm = new MyMove(new Point(x, y), d, EdgeSynopsis.USED_OUT);
                EdgeInfo nei = b.getEdge(x, y, d);
                nei.removeInbound(d);
                b.useEdge(x,y,d);
                fst.addTuple(b, mm);
            }
        }
        return tuples;
    }


    int guessGrade(Point p) {
        return Arrays.stream(Direction.orthogonals()).mapToInt(d -> {
            int dirscore = getEdge(p.x,p.y,d).isUsed() ? 10 : 0;
            Point np = d.delta(p,1);
            if (onBoard(np) && isCellComplete(np.x,np.y)) ++dirscore;
            return dirscore;
        }).sum();
    }

    @Override
    public List<Board> guessAlternatives() {
        Point best = stream().filter(p -> !isCellComplete(p.x,p.y)).max(Comparator.comparing(p -> guessGrade(p))).get();
        List<FlattenSolvableTuple<Board>> lfst = getTuplesForCellWithEdges(best.x,best.y,Direction.orthogonals());

        return lfst.get(0).choices;
    }



}
