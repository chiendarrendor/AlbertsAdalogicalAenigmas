import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

// this object contains up to 4 legal shots in each direction and is responsible for
// invalidating them if necessary
public class ShotHolder
{
    private Map<Direction,Shot> shots = new HashMap<>();
    private boolean allBlocked = true;
    public ShotHolder() {};

    public ShotHolder(ShotHolder right) {
        right.shots.keySet().stream().forEach((d)->shots.put(d,new Shot(right.shots.get(d))));
    }


    public boolean takeInitialShot(int distance, Point start, Board b, Shot parent) {
        boolean result = false;
        for (Direction d : Direction.orthogonals()) {
            Shot s = new Shot(parent,d,distance,start,b);
            if (!s.isBlocked()) allBlocked = false;
            if (s.isValid()) {
                shots.put(d,s);
                result = true;
            }
        }
        return result;
    }

    public boolean isAllBlocked() { return allBlocked; }


    public void setParent(Shot parent) {
        shots.values().stream().forEach((shot)->shot.setParent(parent));
    }

    public Stream<Shot> stream() { return shots.values().stream(); }

    public int count() { return stream().mapToInt((x)-> x.count()).sum(); }

    public List<ShotName> getShotNames(ShotName name) {
        List<ShotName> result = new ArrayList<>();
        stream().forEach((shot)->result.addAll(shot.getShotNames(name)));
        return result;
    }

    public void removeShot(ShotName name, int opindex) {
        Direction opdir = name.shotdirs.get(opindex);
        Shot theShot = shots.get(opdir);


        if (opindex+1 == name.shotdirs.size()) {
            shots.remove(opdir);
        } else {
            theShot.removeShot(name,opindex+1);
            if (theShot.count() == 0) shots.remove(opdir);
        }
    }

    public void removeAllButShot(ShotName name, int opindex) {
        Direction opdir = name.shotdirs.get(opindex);
        Shot theShot = shots.get(opdir);

        for (Direction d : Direction.orthogonals()) {
            if (d == opdir || !shots.containsKey(opdir)) continue;
            shots.remove(d);
        }
        ++opindex;
        if (opindex == name.shotdirs.size()) return;
        theShot.removeAllButShot(name,opindex);
    }

    public void glueDown(Board b) {
        shots.values().stream().forEach((s)->s.glueDown(b));
    }

    public boolean revalidate(Board b) {
        boolean ok = false;
        for (Direction d : Direction.orthogonals()) {
            if (!shots.containsKey(d)) continue;
            Shot s = shots.get(d);

            if (s.revalidate(b)) {
                ok = true;
            } else {
                shots.remove(d);
            }
        }
        return ok;
    }

    public void fillShotInfo(CellContainer<Shot> shinfo) {
        shots.values().stream().forEach((shot)->shot.fillShotInfo(shinfo));
    }
}
