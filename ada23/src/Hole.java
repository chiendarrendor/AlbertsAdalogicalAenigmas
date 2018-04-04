import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.awt.Point;
import java.util.SortedMap;
import java.util.TreeMap;

// a hole contains a set of Shot objects.
// a shot object contains children.
// specifically, it should all those shot objects that terminate in a hole that aren't
// invalidated by edges of the board, other known shot paths, and sand traps.
//
public class Hole
{
    Point initialPoint;
    Board b;
    SortedMap<Integer,ShotHolder> shotsByLength = new TreeMap<>();
    boolean isFixedLength;
    int length = -1;
    char lengthid = 0;
    boolean isSet = false;


    public Hole(Point intialPoint, String slength, Board b)
    {
        this.initialPoint = intialPoint;
        this.b = b;

        isFixedLength = slength.chars().allMatch(Character::isDigit);
        if (isFixedLength) {
            length = Integer.parseInt(slength);
        } else {
            lengthid = slength.charAt(0);
        }

        if (isFixedLength) {
            ShotHolder top = new ShotHolder();
            boolean isValid = top.takeInitialShot(length,intialPoint,b,null);
            if (!isValid) throw new RuntimeException("Initial ball at " + intialPoint + " has no shots!");
            shotsByLength.put(length,top);
        } else {
            for (int curlen = 1; ; ++curlen) {
                ShotHolder top = new ShotHolder();
                boolean isValid = top.takeInitialShot(curlen,intialPoint,b,null);
                if (top.isAllBlocked()) break;
                if (!isValid) continue;
                shotsByLength.put(curlen,top);
                b.getCharMap().addEntry(lengthid,curlen);
            }
        }


    }

    public Hole(Hole right,Board b)
    {
        this.initialPoint = right.initialPoint;
        this.b = b;
        this.isSet = right.isSet;
        isFixedLength = right.isFixedLength;
        length = right.length;
        lengthid = right.lengthid;
        right.shotsByLength.keySet().stream().forEach((len)->shotsByLength.put(len,new ShotHolder(right.shotsByLength.get(len))));
        // this is necessary because you can't hand the ref to yourself 'this' to a child so they can set their parent to you
        // while you are constructing yourself.  So parentage has to be processed after construction.
        shotsByLength.values().stream().forEach((shot)->shot.setParent(null));
    }

    public Point getInitialPoint() { return initialPoint; }
    public boolean isFixedLength() { return isFixedLength; }
    public int maxLen() {
        if (shotsByLength.size() > 0) return shotsByLength.lastKey();
        return -1;
    }

    public void show() {
        System.out.println("Hole " + initialPoint.x + "," + initialPoint.y);
        System.out.print("  ");
        getShotNames().stream().forEach((x)->System.out.print(x.toString()));
        System.out.println("");
        for (int i = 1; i <= maxLen() ; ++i) {
            ShotHolder sh = shotsByLength.get(i);
            if (sh == null) continue;
            System.out.format("  Len %d (%d)%n",i,sh.count() );
            sh.stream().forEach((x)->x.show("    "));

        }
    }

    public boolean isSet() { return isSet; }

    public List<ShotName> getShotNames() {
        List<ShotName> result = new ArrayList<>();

        shotsByLength.keySet().stream().forEach((len)->{
            ShotName name = new ShotName(initialPoint,len);
            ShotHolder sh = shotsByLength.get(len);
            result.addAll(sh.getShotNames(name));
        });


        return result;
    }

    public void removeShot(ShotName name) {
        ShotHolder sh = shotsByLength.get(name.length);
        sh.removeShot(name,0);
        if (sh.count() == 0) shotsByLength.remove(name.length);
    }

    public void removeAllButShot(ShotName name) {
        for (int i = 1 ; i <= maxLen() ; ++i) {
            if (i == name.length) continue;
            shotsByLength.remove(i);
        }
        shotsByLength.get(name.length).removeAllButShot(name,0);
    }


    public void set(ShotName name) {
        removeAllButShot(name);
        // recursion will only operate on non-initial points.
        b.getCellStates().setCell(initialPoint.x,initialPoint.y,CellState.LIE);
        shotsByLength.get(name.length).glueDown(b);

        isSet = true;
    }

    public void validate() {
        for (int i = 1 ; i <= maxLen() ; ++i) {
            if (!shotsByLength.containsKey(i)) continue;
            ShotHolder sh = shotsByLength.get(i);
            if (!sh.revalidate(b)) shotsByLength.remove(i);
        }
    }
}
