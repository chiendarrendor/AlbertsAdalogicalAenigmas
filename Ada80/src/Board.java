import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep EdgeContainer<EdgeType> edges;
    @Shallow FenceDomain fences;
    @Deep ActiveFenceSet livefences;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        edges = new EdgeContainer<EdgeType>(getWidth(),getHeight(),EdgeType.WALL,
                (x,y,isV)->EdgeType.UNKNOWN,
                (x,y,isV,old)->old);
        fences = new FenceDomain(this);
        livefences = new ActiveFenceSet(fences);
    }
    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public EdgeType getEdge(int x,int y,Direction d) { return edges.getEdge(x,y,d); }
    public EdgeType getEdge(int x,int y,boolean isV) { return edges.getEdge(x,y,isV);}
    private void setEdge(int x,int y,Direction d,EdgeType et) { edges.setEdge(x,y,d,et);}
    private void setEdge(int x,int y,boolean isV,EdgeType et) { edges.setEdge(x,y,isV,et); }
    public boolean hasAnimal(int x,int y) { return getAnimal(x,y) != '.'; }
    public char getAnimal(int x,int y) { return gfr.getBlock("ANIMALS")[x][y].charAt(0); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public boolean hasPost(int x,int y) { return gfr.getBlock("POSTS")[x][y].equals("@"); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public Collection<Integer> getPostIds() { return fences.posts.keySet(); }
    public Fence getFence(int id) { return fences.fences.get(id); }
    public Post getPost(int id) { return fences.posts.get(id); }
    public Collection<Integer> getFenceIds() { return livefences.getFenceIds(); }

    public EdgeType getFenceState(int fenceid) { return livefences.getFence(fenceid); }
    public void setFenceState(int fenceid,EdgeType et) {
        livefences.setFence(fenceid,et);
        setFenceEdges(fenceid,et);
    }


    private void setFenceEdges(int fenceid,EdgeType et) {
        Fence f = fences.fences.get(fenceid);
        for (EdgeContainer.EdgeCoord ec : f.edges) {
            setEdge(ec.x,ec.y,ec.isV,et);
        }
    }

    public class PostCountState {
        Post post;
        int pathcount;
        Set<Integer> walls = new HashSet<>();
        Set<Integer> unknowns = new HashSet<>();
        public int getUnknownSize() { return unknowns.size(); }
        public int getWallCount() { return walls.size(); }

        public PostCountState(int postid) {
            post = getPost(postid);
            for(Fence f : getPost(postid).fences) {
                switch(getFenceState(f.id)) {
                    case PATH: ++pathcount; break;
                    case WALL: walls.add(f.id); break;
                    case UNKNOWN: unknowns.add(f.id); break;
                }
            }
        }
    }

    public PostCountState getPostCountState(int postid) { return new PostCountState(postid); }




    public boolean postInCorner(int x,int y,Direction d)  {
        if (d == null || d == Direction.NORTH || d == Direction.SOUTH || d == Direction.EAST || d == Direction.WEST)
            throw new RuntimeException("Illegal direction " + d + " for postInCorner");
        int dx = 0;
        int dy = 0;
        switch(d) {
            case SOUTHEAST: break;
            case SOUTHWEST: dx = -1; break;
            case NORTHEAST: dy = -1; break;
            case NORTHWEST: dx = -1; dy = -1;
        }
        if (!onBoard(x+dx,y+dy)) return false;
        return hasPost(x+dx,y+dy);
    }

    private static class MyMove {
        int fenceid;
        EdgeType et;

        public MyMove(int fenceid,EdgeType et) { this.fenceid = fenceid ; this.et = et; }
        public boolean applyMove(Board b) {
            if (b.getFenceState(fenceid) != EdgeType.UNKNOWN) return b.getFenceState(fenceid) == et;
            b.setFenceState(fenceid,et);
            return true;
        }
    }

    private List<FlattenSolvableTuple<Board>> getSuccessors(boolean onlyone) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        for (int fenceid : livefences.getFenceIds()) {
            if (getFenceState(fenceid) != EdgeType.UNKNOWN) continue;
            Board b1 = new Board(this);
            Board b2 = new Board( this);
            MyMove mm1 = new MyMove(fenceid,EdgeType.PATH);
            MyMove mm2 = new MyMove(fenceid,EdgeType.WALL);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
            if (onlyone) break;
        }
        if (result.size() == 0) {
            throw new RuntimeException("getSuccessors callled on complete Board");
        }


        return result;
    }

    @Override public boolean isComplete() { return livefences.getUnknownCount() == 0; }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }
    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() { return getSuccessors(false); }
    @Override public List<Board> guessAlternatives() { return getSuccessors(true).get(0).choices; }
}
