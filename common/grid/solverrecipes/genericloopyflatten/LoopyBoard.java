package grid.solverrecipes.genericloopyflatten;

import grid.logic.LogicStep;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoopyBoard implements FlattenSolvable<LoopyBoard> {
    private Map<String,Set<String>> vertices = new HashMap<>();
    private Map<String,LineState> edges = new HashMap<>();
    private Map<String,Set<String>> edgeends = new HashMap<>();
    private List<CluePair> clues = new ArrayList<>();
    private int unknowns = 0;
    private Set<String> demandedVertices = new HashSet<>();

    private class CluePair {
        int size;
        Set<String> edges;
        public CluePair(int size,Set<String>edges) { this.size = size; this.edges = edges; }
    }




    private void addVertex(String vname,String ename) {
        if (!vertices.containsKey(vname)) {
            vertices.put(vname,new HashSet<>());
        }
        vertices.get(vname).add(ename);
    }

    public LoopyBoard() {}
    public LoopyBoard(LoopyBoard right) {
        vertices = right.vertices;
        unknowns = right.unknowns;
        edges = new HashMap<String,LineState>(right.edges);
        edgeends = right.edgeends;
        clues = right.clues;
        demandedVertices = right.demandedVertices;
    }

    public void addEdge(String v1name,String v2name,String ename) {
        if (edges.containsKey(ename)) throw new RuntimeException("Duplicate Edge inserted: " + ename);
        ++unknowns;
        Set<String> myedgeends = new HashSet<>();
        myedgeends.add(v1name);
        myedgeends.add(v2name);
        edgeends.put(ename,myedgeends);
        edges.put(ename,LineState.UNKNOWN);
        addVertex(v1name,ename);
        addVertex(v2name,ename);
    }

    public void addClue(int size,Set<String> edges) {
        clues.add(new CluePair(size,edges));
    }
    public void demandVertex(String vname) {
        if (!vertices.containsKey(vname)) throw new RuntimeException("Can't demand unknown vertex");
        demandedVertices.add(vname);
    }


    public LineState getEdge(String ename) {
        if (!edges.containsKey(ename)) throw new RuntimeException("Unknown Edge requested: " + ename);
        return edges.get(ename);
    }
    public void setEdge(String ename,LineState ls) {
        if (!edges.containsKey(ename)) throw new RuntimeException("Edge Set does not contain edge named " + ename);
        edges.put(ename,ls);
        --unknowns;
    }
    public Set<String> getVertexEdges(String vname) { return vertices.get(vname); }
    public Set<String> getVertexNames() { return vertices.keySet(); }
    public Set<String> getEdgeEnds(String ename) { return edgeends.get(ename); }
    public Set<String> getEdgeNames() { return edges.keySet(); }
    public boolean hasEdge(String ename) { return edges.containsKey(ename); }
    public boolean hasVertex(String vname) { return vertices.containsKey(vname); }


    private static class MyMove {
        String ename;
        LineState ls;
        public MyMove(String ename,LineState ls) { this.ename = ename; this.ls = ls; }
        public boolean applyMove(LoopyBoard b) {
            if (b.getEdge(ename) != LineState.UNKNOWN) return b.getEdge(ename) == ls;
            b.setEdge(ename,ls);
            return true;
        }
        @Override public String toString() { return "LoopyBoard MyMove: " + ename + ": " + ls; }
    }

    @Override public boolean isComplete() { return unknowns == 0; }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    private List<FlattenSolvableTuple<LoopyBoard>> getSuccessorTuples(boolean limitone) {
        List<FlattenSolvableTuple<LoopyBoard>> result = new ArrayList<>();
        for(String ename : edges.keySet()) {
            if (getEdge(ename) != LineState.UNKNOWN) continue;
            LoopyBoard b1 = new LoopyBoard(this);
            LoopyBoard b2 = new LoopyBoard(this);
            MyMove mm1 = new MyMove(ename,LineState.NOTPATH);
            MyMove mm2 = new MyMove(ename,LineState.PATH);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<LoopyBoard>(b1,mm1,b2,mm2));
            if (limitone) return result;
        }
        return result;
    }

    @Override public List<FlattenSolvableTuple<LoopyBoard>> getSuccessorTuples() { return getSuccessorTuples(false); }
    @Override public List<LoopyBoard> guessAlternatives() {
        return getSuccessorTuples(true).get(0).choices;
    }

    public List<LogicStep<LoopyBoard>> getLogic() {
        List<LogicStep<LoopyBoard>> result = new ArrayList<>();
        for (String vname : vertices.keySet()) result.add(new VertexPathLogicStep(vname,vertices.get(vname),demandedVertices.contains(vname)));
        for (CluePair clue : clues) result.add(new RegionBoundaryLogicStep(clue.size,clue.edges));
        result.add(new SingleLoopLogicStep());
        return result;
    }

    public void showStructure() {
        for(String vname : getVertexNames()) {
            System.out.println("V: " + vname + ": " + getVertexEdges(vname));
        }
        for(String edge : getEdgeNames()) {
            System.out.println("E: " + edge + ": " + getEdgeEnds(edge));
        }
    }

    public void showState() {
        for (String edge : getEdgeNames()) {
            System.out.println("S: " + edge + ": " + getEdge(edge));
        }
    }

}
