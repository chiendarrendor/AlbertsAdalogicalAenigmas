import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.solverrecipes.genericloopyflatten.LineState;
import grid.spring.ExpandedGridEdgeListener;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VertexClueLogicStep extends CellEdgeTranslator implements LogicStep<Board> {
    Point clueVertex;
    VertexClueColor color;
    public VertexClueLogicStep(Point v, VertexClueColor color) { this.clueVertex = v; this.color = color; }

    private static String getOtherEndVertex(Board thing,String vname,String ename) {
        return thing.loopy.getEdgeEnds(ename).stream().filter(v->!v.equals(vname)).findFirst().get();
    }


    private static Set<Point> knownAdjacents(Board thing,Point p) {
        String vname = vertexName(p);
        Set<String> conedgenames = thing.loopy.getVertexEdges(vname);

        return thing.loopy.getVertexEdges(vertexName(p)).stream()
                .filter(ename->thing.loopy.getEdge(ename) == LineState.PATH)
                .map(ename->getOtherEndVertex(thing,vname,ename))
                .map(ovname->nameToVertex(ovname))
                .collect(Collectors.toSet());
    }

    private static class EdgeInfo {
        String vname;
        Point vertex;
        Direction d;
        String ename;
        EdgeContainer.EdgeCoord ecoord;
        LineState curstate;
        Point overtex;
        String overtexname;
        public EdgeInfo(Board b,Direction d,Point v) {
            vertex = v;
            vname = vertexName(v);
            this.d = d;
            switch(d) {
                case NORTH: ecoord = new EdgeContainer.EdgeCoord(v.x,v.y-1,true); break;
                case SOUTH: ecoord = new EdgeContainer.EdgeCoord(v.x,v.y,true); break;
                case EAST: ecoord = new EdgeContainer.EdgeCoord(v.x,v.y,false); break;
                case WEST: ecoord = new EdgeContainer.EdgeCoord(v.x-1,v.y,false); break;
            }
            ename = edgeName(ecoord);
            if (!b.loopy.hasEdge(ename)) {
                ename = null;
                return;
            }
            curstate = b.loopy.getEdge(ename);
            overtexname = getOtherEndVertex(b,vname,ename);
            overtex = nameToVertex(overtexname);

        }

    }


    private static class SurroundingState {
        String vname;
        Point v;
        Map<Direction,EdgeInfo> edges = new HashMap<>();

        public SurroundingState(Board b,Point v) {
            this.v = v;
            this.vname = vertexName(v);
            for (Direction d : Direction.orthogonals()) {
                EdgeInfo ei = new EdgeInfo(b,d,v);
                if (ei.ename != null && ei.curstate != LineState.NOTPATH) edges.put(d,ei);
            }
        }

        public Set<Direction> dirs() { return edges.keySet(); }

        public int onPathCount() {
            return (int)edges.values().stream().filter(ei->ei.curstate == LineState.PATH).count();
        }

        // given onPathCount == 2, is true if the path is a non-bend one.
        public boolean straightThrough() {
            List<EdgeInfo> lei = edges.values().stream().filter(ei->ei.curstate == LineState.PATH).collect(Collectors.toList());
            return lei.get(0).d.getOpp() == lei.get(1).d;
        }

        public EdgeInfo getSingleOn() {
            return edges.values().stream().filter(ei->ei.curstate == LineState.PATH).findFirst().get();
        }

        public EdgeInfo inDir(Direction d) { return edges.get(d); }
    }




    private static LogicStatus doBend(Board thing,Point v) {
        LogicStatus result = LogicStatus.STYMIED;
        SurroundingState ss = new SurroundingState(thing,v);
        int onsize = ss.onPathCount();
        if (onsize > 2) return LogicStatus.CONTRADICTION;
        if (onsize == 2) return ss.straightThrough() ? LogicStatus.CONTRADICTION : LogicStatus.STYMIED;
        if (onsize == 1) {
            EdgeInfo ei = ss.getSingleOn();
            EdgeInfo opp = ss.inDir(ei.d.getOpp());
            if (opp != null && opp.curstate == LineState.UNKNOWN) {
                thing.loopy.setEdge(opp.ename,LineState.NOTPATH);
                result = LogicStatus.LOGICED;
            }

            List<EdgeInfo> odirs = new ArrayList<>();
            for (Direction d : ss.dirs()) {
                if (d == ei.d) continue;
                if (opp != null && d == opp.d) continue;
                odirs.add(ss.inDir(d));
            }

            if (odirs.size() == 0) return LogicStatus.CONTRADICTION;
            if (odirs.size() == 2) return result;
            thing.loopy.setEdge(odirs.get(0).ename,LineState.PATH);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }

    private static LogicStatus doStraight(Board thing, Point v) {
        LogicStatus result = LogicStatus.STYMIED;
        SurroundingState ss = new SurroundingState(thing,v);
        int onsize = ss.onPathCount();
        if (onsize > 2) return LogicStatus.CONTRADICTION;
        if (onsize == 2) return ss.straightThrough() ? LogicStatus.STYMIED : LogicStatus.CONTRADICTION;
        if (onsize == 1) {
            EdgeInfo ei = ss.getSingleOn();
            EdgeInfo opp = ss.inDir(ei.d.getOpp());
            if (opp == null) return LogicStatus.CONTRADICTION;
            if (opp.curstate == LineState.UNKNOWN) {
                thing.loopy.setEdge(opp.ename,LineState.PATH);
                result = LogicStatus.LOGICED;
            }

            for (Direction d : ss.dirs()) {
                if (d == ei.d) continue;
                if (d == opp.d) continue;
                EdgeInfo oth = ss.inDir(d);
                thing.loopy.setEdge(oth.ename,LineState.NOTPATH);
                result = LogicStatus.LOGICED;
            }
            return result;
        }

        return LogicStatus.STYMIED;
    }

    // we are here if we know that the clue vertex was straight.
    // furthermore, we know that the given set of vertices are the ones that are actually connected to
    // so there will be no more than two, and they will have at least one edge.
    private static LogicStatus doAtLeastOneBend(Board thing, Set<Point> vertices) {
        if (vertices.size() < 2) return LogicStatus.STYMIED;
        Set<SurroundingState> sss = vertices.stream().map(v->new SurroundingState(thing,v)).collect(Collectors.toSet());
        if (sss.stream().filter(ss->ss.onPathCount() > 2).count() > 0) return LogicStatus.CONTRADICTION;
        if (sss.stream().filter(ss->ss.onPathCount() == 2).filter(ss->ss.straightThrough()).count() > 1) return LogicStatus.CONTRADICTION;
        if (sss.stream().filter(ss->ss.onPathCount() == 2).filter(ss->!ss.straightThrough()).count() > 0) return LogicStatus.STYMIED;
        if (sss.stream().filter(ss->ss.onPathCount() == 2).filter(ss->ss.straightThrough()).count() == 0) return LogicStatus.STYMIED;
        // if we get here, we know that we have exactly 1 straightThrough, and no bends.
        // the one that is not the straightthrough has to bend.
        SurroundingState only = sss.stream().filter(ss->ss.onPathCount() < 2).findFirst().get();
        return doBend(thing,only.v);
    }



    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        if (color == VertexClueColor.BLACK) {
            LogicStatus cenls = doBend(thing,clueVertex);
            if (cenls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (cenls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

            for (Point p : knownAdjacents(thing,clueVertex)) {
                LogicStatus ols = doStraight(thing,p);
                if (ols == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (ols == LogicStatus.LOGICED) return LogicStatus.LOGICED;
            }
        } else {
            LogicStatus cenls = doStraight(thing,clueVertex);
            if (cenls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (cenls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            LogicStatus ols = doAtLeastOneBend(thing,knownAdjacents(thing,clueVertex));
            if (ols == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (ols == LogicStatus.LOGICED) return LogicStatus.LOGICED;
        }

        return result;
    }
}
