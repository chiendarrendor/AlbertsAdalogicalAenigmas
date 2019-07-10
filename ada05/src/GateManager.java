import grid.copycon.CopyCon;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GateManager {
    public class GatePointer {
        public Gate g;
        Map<Direction,Gate> terminals = new HashMap<>();
    }

    @Shallow CellContainer<GatePointer> cellinfo;
    @Shallow Map<Integer,Gate> gates = new HashMap<>();

    public GatePointer getGatePointer(int x,int y) {
        return cellinfo.getCell(x,y);
    }

    public int getGateCount() { return gates.size(); }
    public Collection<Gate> getGates() { return gates.values(); }
    public Gate getGate(int id) { return gates.get(id); }
    public boolean isGate(int x,int y) {
        GatePointer gp = getGatePointer(x,y);
        return (gp != null && gp.g != null);
    }

    public Gate getGate(int x,int y) {
        return getGatePointer(x,y).g;
    }

    private void addCellPointer(Gate g,Point p) {
        if (cellinfo.getCell(p.x,p.y) != null) {
            throw new RuntimeException("Shouldn't have more than one gate per cell");
        }
        GatePointer newgp = new GatePointer();
        newgp.g = g;
        cellinfo.setCell(p.x,p.y,newgp);
    }

    private void addCellTerminal(Gate g,Point p,Direction d) {
        if (cellinfo.getCell(p.x,p.y) == null) {
            cellinfo.setCell(p.x,p.y,new GatePointer());
        }
        GatePointer gp = cellinfo.getCell(p.x,p.y);
        if (gp.terminals.containsKey(d)) {
            throw new RuntimeException("Shouldn't write more than one terminal per cell/direction");
        }
        if (gp.g != null) {
            throw new RuntimeException("Can't have a gate and a terminal in the same place!");
        }

        gp.terminals.put(d,g);
        g.addTerminal(new EdgeContainer.CellCoord(p.x,p.y,d));
    }



    private void extendDirection(Gate g,Point cp, GridFileReader gfr, Direction d,Set<Point> ungates) {
        int delta = 1;
        while(true) {
            Point np = d.delta(cp, delta);
            if (!ungates.contains(np)) {
                if (gfr.inBounds(np)) {
                    if (gfr.getBlock("BLOCKS")[np.x][np.y].charAt(0) != '*') {
                        throw new RuntimeException("Gate at " + np + " terminates at non-block!");
                    }

                    addCellTerminal(g,np,d.getOpp());
                }
                break;
            }
            ungates.remove(np);
            g.addCell(np);
            addCellPointer(g,np);
            ++delta;
        }
    }

    public GateManager(GridFileReader gfr) {
        cellinfo = new CellContainer<GatePointer>(gfr.getWidth(),gfr.getHeight(),(x,y)->null);


        Set<Point> unprocessedgates = new HashSet<>();
        Map<Integer,Set<Point>> unprocessednumbers = new HashMap<>();
        Map<Point,String> gateclues = new HashMap<>();

        for (int y = 0 ; y < gfr.getHeight(); ++y) {
            for (int x = 0 ; x < gfr.getWidth() ; ++x) {
                String s = gfr.getBlock("GATES")[x][y];

                if (s.equals(".") || s.equals("*")) {
                    // do nothing
                } else if (s.equals("-") || s.equals("|")) {
                    unprocessedgates.add(new Point(x,y));
                } else if (s.matches("\\d+")) {
                    int num = Integer.parseInt(s);
                    if (!unprocessednumbers.containsKey(num)) {
                        unprocessednumbers.put(num,new HashSet<>());
                    }
                    unprocessednumbers.get(num).add(new Point(x,y));
                } else {
                    throw new RuntimeException("Illegal value in GATES at " + x + "," + y + ": " + s);
                }

                if (gfr.hasBlock("GATECLUES")) {
                    String cluestring = gfr.getBlock("GATECLUES")[x][y];
                    if (cluestring.equals(".")) continue;
                    gateclues.put(new Point(x,y),cluestring);
                }


            }
        }

        while(unprocessedgates.size() > 0) {
            Point p = unprocessedgates.iterator().next();
            unprocessedgates.remove(p);

            Gate g = new Gate(gfr.getBlock("GATES")[p.x][p.y].charAt(0));
            gates.put(g.getId(),g);

            g.addCell(p);
            addCellPointer(g,p);
            if (g.getOrientation() == '-') {
                extendDirection(g,p,gfr,Direction.WEST,unprocessedgates);
                extendDirection(g,p,gfr,Direction.EAST,unprocessedgates);
            } else {
                extendDirection(g,p,gfr,Direction.NORTH,unprocessedgates);
                extendDirection(g,p,gfr,Direction.SOUTH,unprocessedgates);
            }
        }

        for (Map.Entry<Integer,Set<Point>> ent : unprocessednumbers.entrySet()) {
            int num = ent.getKey();
            Set<Point> ends = ent.getValue();

            boolean found = false;
            for (Gate g : getGates()) {
                if (ends.size() != g.getTerminals().size()) continue;
                boolean broken = false;
                for (EdgeContainer.CellCoord ec : g.getTerminals()) {
                    Point eccp = new Point(ec.x,ec.y);
                    if (!ends.contains(eccp)) {
                        broken = true;
                        break;
                    }
                }
                if (broken) continue;
                found = true;
                g.setNumber(num);
                break;
            }

            if (!found) {
                throw new RuntimeException("Couldn't find gate for number " + num);
            }


        }


        for (Point p : gateclues.keySet()) {
            String clue = gateclues.get(p);
            Gate g = getGate(p.x,p.y);

            if (g.getOrientation() == '|') {
                g.addLetter(Direction.WEST,clue.charAt(0));
                g.addLetter(Direction.EAST,clue.charAt(1));
            } else {
                g.addLetter(Direction.NORTH,clue.charAt(0));
                g.addLetter(Direction.SOUTH,clue.charAt(1));
            }
        }



    }

    public GateManager(GateManager right) {
        CopyCon.copy(this,right);
    }
}
