import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class JumpSet implements Iterable<Jump> {
    private Set<Jump> jumps = new HashSet<>();
    private Point basep;

    public JumpSet(Board board, int x, int y,int distance) {
        basep = new Point(x,y);

        if (distance == 0) {
            Jump nj = new Jump(basep,null,null,null);
            jumps.add(nj);
            board.getBackReferences(basep.x,basep.y).addJump(nj);
        }


        for (Direction d : Direction.orthogonals()) {
            Set<Point> intermediates = new HashSet<>();
            Point curterminal = null;
            for (int dist = 1 ; true ; intermediates.add(curterminal),++dist) {
                curterminal = d.delta(basep,dist);
                if (!board.onBoard(curterminal.x,curterminal.y)) break;
                if (!board.getCell(curterminal.x,curterminal.y).canPass()) break;
                if (!board.getCell(curterminal.x,curterminal.y).canLand()) continue;
                if (distance > 0 && dist < distance) continue;
                if (distance > 0 && dist > distance) break;

                Jump nj = new Jump(basep,new HashSet<Point>(intermediates),curterminal,d);
                jumps.add(nj);
                board.getBackReferences(curterminal.x,curterminal.y).addJump(nj);

            }
        }
    }

    public JumpSet(JumpSet r) {
        jumps.addAll(r.jumps);
    }

    public void show() {
        System.out.println("Jumps for Point " + basep);
        jumps.stream().forEach(x->System.out.println("  " + x.toString()));
    }

    public void remove(Jump j) {
        jumps.remove(j);
    }

    public boolean set(Jump j) {
        if (!jumps.contains(j)) return false;
        jumps.clear();
        jumps.add(j);
        return true;
    }


    @Override public Iterator<Jump> iterator() {
        return jumps.iterator();
    }

    public int size() { return jumps.size(); }
    public Jump solo() { return jumps.iterator().next(); }

    public void cleanBad(Board b) {
        Set<Jump> newjumps = new HashSet<>();
        for (Jump j : jumps) {
            if (j.isLegal(b)) newjumps.add(j);
        }
        jumps = newjumps;
    }

    public boolean contains(Jump j) { return jumps.contains(j); }
}
