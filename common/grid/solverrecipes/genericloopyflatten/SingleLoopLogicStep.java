package grid.solverrecipes.genericloopyflatten;

import grid.logic.LogicStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SingleLoopLogicStep implements grid.logic.LogicStep<LoopyBoard> {
    private boolean debug = false;
    public void debug() { debug = true; }

    private static Set<String> activeVertexEdges(LoopyBoard thing, String vname) {
        return thing.getVertexEdges(vname).stream().filter(e->thing.getEdge(e)==LineState.PATH).collect(Collectors.toSet());
    }

    // to be called on a set of size 2 where one item in the set is the given edge...return the other one.
    private static String oedge(Set<String> eset, String incoming) {
        return eset.stream().filter(e->!e.equals(incoming)).findFirst().get();
    }


    @Override public LogicStatus apply(LoopyBoard thing) {
        Map<String,Set<String>> singlevs = new HashMap<>();
        Map<String,Set<String>> doublevs = new HashMap<>();
        boolean hasString = false; // true if there's at least one group of connected vertices with singles on both ends

        for (String vname : thing.getVertexNames()) {
            Set<String> vedges = activeVertexEdges(thing,vname);
            if (vedges.size() > 2) {
                if (debug) System.out.println("CONTRADICTION: vertex " + vname + " has more than 2 edges " + vedges);
                return LogicStatus.CONTRADICTION;
            }
            if (vedges.size() == 2) doublevs.put(vname,vedges);
            if (vedges.size() == 1) { singlevs.put(vname,vedges); hasString = true; }
        }

        while(singlevs.size() > 0) {
            String curVertex = singlevs.keySet().iterator().next();
            String theEdge = singlevs.get(curVertex).iterator().next();
            StringBuffer sb = debug ? new StringBuffer() : null;
            if (debug) sb.append("CHAIN: " + curVertex);
            singlevs.remove(curVertex);

            while(true) {
                String overtex = oedge(thing.getEdgeEnds(theEdge),curVertex);
                if (singlevs.containsKey(overtex)) {
                    singlevs.remove(overtex);
                    if (debug) { sb.append("-(" + overtex + ")"); System.out.println(sb.toString()); }
                    break;
                }
                if (doublevs.containsKey(overtex)) {
                    curVertex = overtex;
                    theEdge = oedge(doublevs.get(overtex),theEdge);
                    doublevs.remove(overtex);
                    if (debug) { sb.append("-" + overtex); }
                    continue;
                }
                throw new RuntimeException("We should always be able to find the next vertex in chain!");
            }
        }
        // if we get here, we have processed away all singles, and all doubles that are in chains.
        // all doubles that are left must be in one or more loops.
        if (doublevs.size() == 0) {
            if (debug) System.out.println("STYMIED: no remaining doublevs");
            return LogicStatus.STYMIED; // no loops, no bad!
        }
        if (hasString) {
            if (debug) System.out.println("CONTRADICTION: strings and doublevs");
            return LogicStatus.CONTRADICTION; // a loop and any strings, bad!
        }
        // if we get here, the whole board must be one or more loops.  we have to find out how many.

        boolean seenLoop = false;
        while(doublevs.size() > 0) {
            if (seenLoop) {
                if (debug) System.out.println("CONTRADICTION: multiple loops");
                return LogicStatus.CONTRADICTION;
            }
            seenLoop = true;

            String startVertex = doublevs.keySet().iterator().next();
            Set<String> edges = doublevs.get(startVertex);
            String theedge = edges.iterator().next();
            doublevs.remove(startVertex);

            String curVertex = startVertex;
            StringBuffer sb = debug ? new StringBuffer() : null;
            if (debug) sb.append("LOOP: " + curVertex);
            while(true) {
                String overtex = oedge(thing.getEdgeEnds(theedge),curVertex);
                if (overtex.equals(startVertex)) {
                    if (debug) { sb.append("-(" + overtex + ")");  System.out.println(sb.toString()); }
                    break;
                }

                curVertex = overtex;
                if (debug) { sb.append("-" + overtex); }
                theedge = oedge(doublevs.get(overtex),theedge);
                doublevs.remove(curVertex);
            }
        }
        if (debug) System.out.println("No bad stuff detected");
        return LogicStatus.STYMIED;
    }
}
