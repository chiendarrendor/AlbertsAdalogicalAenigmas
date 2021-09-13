import grid.graph.GridGraph;
import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DistrictConnectivityLogicStep implements grid.logic.LogicStep<Board> {

    // every cell must have recursive PATH to a numbered cell
    // every numbered cell must have recursive PATH to every other cell with the same number
    // no numbered cell should have a recursive PATH to any other cell with a different number

    private static class MyPCDReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board b;
        int key;
        public MyPCDReference(Board b, int key) { this.b = b; this.key = key; }

        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight();  }

        @Override public boolean isConnectedCell(int x, int y) {
            return b.getDistrict(x,y).isNumbered && b.getDistrict(x,y).number == key;
        }

        @Override public boolean isPossibleCell(int x, int y) {
            return !b.getDistrict(x,y).isNumbered;
        }

        @Override public boolean edgeExitsEast(int x, int y) { return b.getEdge(x,y, Direction.EAST) != EdgeState.WALL; }
        @Override public boolean edgeExitsSouth(int x, int y) { return b.getEdge(x,y,Direction.SOUTH) != EdgeState.WALL; }
    }

    private static class MyGridReference implements GridGraph.GridReference {
        Board b;
        public MyGridReference(Board b) { this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return true; }
        @Override public boolean edgeExitsEast(int x, int y) { return b.getEdge(x,y,Direction.EAST) != EdgeState.WALL; }
        @Override public boolean edgeExitsSouth(int x, int y) { return b.getEdge(x,y,Direction.SOUTH) != EdgeState.WALL; }
    }



    @Override public LogicStatus apply(Board thing) {
        Map<Integer, List<District>> districtsByNumber = new HashMap<>();
        List<District> unknowns = new ArrayList<>();
        for(District d : thing.districts.districtsById.values()) {
            if (d.isBroken) return LogicStatus.CONTRADICTION;
            if (!d.isNumbered) {
                unknowns.add(d);
            } else {
                int number = d.number;
                if (!districtsByNumber.containsKey(number)) districtsByNumber.put(number, new ArrayList<>());
                districtsByNumber.get(number).add(d);
            }
        }

        for (District d : unknowns) {
            if (d.externalUnknownEdges.size() == 0) return LogicStatus.CONTRADICTION;
            if (d.externalUnknownEdges.size() == 1) {
                EdgeContainer.EdgeCoord ec = d.externalUnknownEdges.iterator().next();
                thing.setEdge(ec.x,ec.y,ec.isV,EdgeState.PATH);
                return LogicStatus.LOGICED;
            }
        }

        for(int num : districtsByNumber.keySet()) {
            List<District> districts = districtsByNumber.get(num);
            for (District d : districts) {
                if(districts.size() > 1) {
                    if (d.externalUnknownEdges.size() == 0) return LogicStatus.CONTRADICTION;
                    if (d.externalUnknownEdges.size() == 1) {
                        EdgeContainer.EdgeCoord ec = d.externalUnknownEdges.iterator().next();
                        thing.setEdge(ec.x,ec.y,ec.isV,EdgeState.PATH);
                        return LogicStatus.LOGICED;
                    }

                    //PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyPCDReference(thing,num));
                    //if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;
                    //List<Point> articulations = pcd.getArticulatingPossibles();
                    //if (articulations.size() > 0) {
                    //    articulations.stream().forEach(p->thing.getDistrict(p.x,p.y).setNumber(num));
                    //    return LogicStatus.LOGICED;
                   //}
                }
            }
        }

        GridGraph gg = new GridGraph(new MyGridReference(thing));
        List<Set<Point>> conSets = gg.connectedSets();
        // to be valid:
        //  a) all instances of a given number must be present in a single conset
        //  b) every conset must have a number
        Set<Integer> seenNumbers = new HashSet<>();
        for(Set<Point> conset : conSets) {
            Set<Integer> localSeenNumbers = new HashSet<>();
            for (Point p : conset) {
                District d = thing.getDistrict(p.x,p.y);
                if (d.isBroken) return LogicStatus.CONTRADICTION;
                if (!d.isNumbered) continue;
                if (seenNumbers.contains(d.number)) return LogicStatus.CONTRADICTION; // if we have a number seeen in a prior conset...
                localSeenNumbers.add(d.number);
            }
            if (localSeenNumbers.size() == 0) return LogicStatus.CONTRADICTION;  // we saw no numbers in this conset.
            seenNumbers.addAll(localSeenNumbers); // add the numbers we saw for the next round.
        }

        return LogicStatus.STYMIED;
    }
}
