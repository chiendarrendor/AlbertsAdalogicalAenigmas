import grid.lambda.LambdaInteger;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import javax.swing.plaf.synth.Region;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Districts {

    Map<Integer,District> districtsById = new HashMap<>();
    CellContainer<District> districtsByCell;

    public Districts(int width, int height, Board board) {
        districtsByCell = new CellContainer<District>(width,height,
                (x,y)-> {
                    District dist = board.hasNumber(x,y) ? new District(board.getNumber(x,y)) : new District();
                    districtsById.put(dist.getId(),dist);
                    dist.contents.add(new Point(x,y));

                    for(Direction d : Direction.orthogonals()) {
                        if (board.getEdge(x,y,d) == EdgeState.WALL) continue;
                        if (board.getEdge(x,y,d) == EdgeState.PATH) throw new RuntimeException("Shouldn't have any paths at this point!");
                        dist.addEdge(x,y,d);
                    }

                    return dist;
                },
                (x,y,r)->null
        );
    }

    public Districts(Districts right) {
        districtsByCell = new CellContainer<District>(right.districtsByCell);

        for(int did : right.districtsById.keySet()) {
            District newDistrict = new District(right.districtsById.get(did));
            districtsById.put(did,newDistrict);

            for (Point p : newDistrict.contents) {
                districtsByCell.setCell(p.x,p.y,newDistrict);
            }
        }
    }

    public void addWall(EdgeContainer.EdgeCoord ec) {
        // removeEdge no-ops if edge is already gone so
        for (Point p : ec.getAdjacentCells()) {
            districtsByCell.getCell(p.x,p.y).removeEdge(ec);
        }
    }

    public void addPath(EdgeContainer.EdgeCoord ec) {
        List<Point> regionReferences = ec.getAdjacentCells();
        District d1 = districtsByCell.getCell(regionReferences.get(0).x,regionReferences.get(0).y);
        District d2 = districtsByCell.getCell(regionReferences.get(1).x,regionReferences.get(1).y);

        // if the Districts on both sides of the edge are the same, then we're just removing an internal edge.
        if (d1 == d2) {
            d1.removeEdge(ec);
            return;
        }

        // if we get here, we have to merge districts.  We are going to do this by destroying d2, moving all info and
        // references over to d1.
        districtsById.remove(d2.getId());
        for (Point p : d2.contents) districtsByCell.setCell(p.x,p.y,d1);
        d1.contents.addAll(d2.contents);

        // ec used to be an external edge of d1 and d2...because it's no longer unknown, it's not mentioned.
        d1.removeEdge(ec);
        d1.externalUnknownEdges.addAll(d2.externalUnknownEdges);
        d1.internalUnknownEdges.addAll(d1.internalUnknownEdges);

        if (d1.isNumbered && d2.isNumbered && d1.number != d2.number) d1.isBroken = true; // this is how we make isBroken
        d1.number = d1.isNumbered ? d1.number : d2.number; // if neither is numbered this still works.  if both are numbered we don't care.
        d1.isNumbered = d1.isNumbered || d2.isNumbered;
        d1.isBroken = d1.isBroken || d2.isBroken;

        // it is possible for some other external edges to now be internal edges.
        Set<EdgeContainer.EdgeCoord> stillExternal = new HashSet<>();
        Set<EdgeContainer.EdgeCoord> nowInternal = new HashSet<>();
        for(EdgeContainer.EdgeCoord tec : d1.externalUnknownEdges) {
            List<Point> trr = tec.getAdjacentCells();
            if (districtsByCell.getCell(trr.get(0).x,trr.get(0).y) == districtsByCell.getCell(trr.get(1).x,trr.get(1).y)) {
                nowInternal.add(tec);
            } else {
                stillExternal.add(tec);
            }
        }
        d1.externalUnknownEdges = stillExternal;
        d1.internalUnknownEdges.addAll(nowInternal);
    }

}
