import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.genericloopyflatten.LineState;
import org.omg.CORBA.UNKNOWN;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BrickSpecificClueLogicStep implements LogicStep<Board> {
    private enum SideType {TOP,BOTTOM,LEFT,RIGHT};
    private Map<SideType,Set<String>> sidenames = new HashMap<>();
    int size;

    public BrickSpecificClueLogicStep(int clue, CellLines cell) {
        size = clue;
        sidenames.put(SideType.TOP,new HashSet<>());
        sidenames.put(SideType.BOTTOM,new HashSet<>());
        sidenames.put(SideType.LEFT,new HashSet<>());
        sidenames.put(SideType.RIGHT,new HashSet<>());

        sidenames.get(SideType.TOP).add(cell.getCellLine(Direction.NORTHEAST));
        sidenames.get(SideType.TOP).add(cell.getCellLine(Direction.NORTHWEST));
        sidenames.get(SideType.LEFT).add(cell.getCellLine(Direction.WEST));
        sidenames.get(SideType.RIGHT).add(cell.getCellLine(Direction.EAST));
        sidenames.get(SideType.BOTTOM).add(cell.getCellLine(Direction.SOUTHEAST));
        sidenames.get(SideType.BOTTOM).add(cell.getCellLine(Direction.SOUTHWEST));
    }

    @Override public LogicStatus apply(Board thing) {
        int pathsidecount = 0;
        Set<SideType> unknownsides = new HashSet<>();
        for(SideType st : SideType.values()) {
            int pathcount = 0;
            int notpathcount = 0;
            int unknowncount = 0;

            for(String ename : sidenames.get(st)) {
                switch(thing.subboard.getEdge(ename)) {
                    case UNKNOWN: ++unknowncount ; break;
                    case PATH: ++pathcount; break;
                    case NOTPATH: ++notpathcount; break;
                }
            }

            if (pathcount > 0) { pathsidecount++; continue; }
            if (unknowncount > 0) unknownsides.add(st);
        }

        if (pathsidecount > size) return LogicStatus.CONTRADICTION;
        if (pathsidecount + unknownsides.size() < size) return LogicStatus.CONTRADICTION;
        if (unknownsides.size() == 0) return LogicStatus.STYMIED;

        if (pathsidecount == size) {
            for(SideType st: unknownsides) {
                for(String ename : sidenames.get(st))  {
                    if (thing.subboard.getEdge(ename) == LineState.UNKNOWN) {
                        thing.subboard.setEdge(ename,LineState.NOTPATH);
                    }
                }
            }
            return LogicStatus.LOGICED;
        }

        LogicStatus result = LogicStatus.STYMIED;
        if (pathsidecount + unknownsides.size() == size) {
            for (SideType st : unknownsides) {
                Set<String> unknownedges =
                        sidenames.get(st)
                                .stream()
                                .filter(e->thing.subboard.getEdge(e) == LineState.UNKNOWN)
                                .collect(Collectors.toSet());

                if (unknownedges.size() == 1) {
                    thing.subboard.setEdge(unknownedges.iterator().next(),LineState.PATH);
                    result = LogicStatus.LOGICED;
                }
            }
            return result;
        }





        return LogicStatus.STYMIED;
    }
}
