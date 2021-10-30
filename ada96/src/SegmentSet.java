import java.util.ArrayList;
import java.util.List;

public class SegmentSet {
    List<Segment> segments = new ArrayList<>();

    public SegmentSet() {};
    public SegmentSet(SegmentSet right) {
        segments.addAll(right.segments);
    }

    public boolean addable(Segment s) {
        for (int i = 0 ; i < segments.size() ; ++i) {
            Segment cur = segments.get(i);

            if (s.begin.equals(cur.begin)) return false;
            if (s.end.equals(cur.end)) return false;
            if (s.end.equals(cur.begin)) return false;

            if (i == segments.size()-1) {
                if (!s.begin.equals(cur.end)) throw new RuntimeException("single segment add to segment set must be extend");
            } else {
                if (s.begin.equals(cur.end)) return false;
                // there's no way for an otherwise legal new terminal to intersect the previous terminal
                if (s.intersects(cur)) return false;
            }
        }
        return true;
    }




    // we add a single segment to segments if it's a chain.
    // as such its endpoints should never touch any other
    // segment's endpoints, except for the last segment, where
    // new segement's begin = tail segement's end.
    public void addSegment(Segment s) {
        if (!addable(s)) throw new RuntimeException("Can't add that segment, why didn't you check first?");
        segments.add(s);
    }

    // this might be expensive.  might have to implement Bentley-Ottman
    // https://en.wikipedia.org/wiki/Bentley%E2%80%93Ottmann_algorithm
    // or at the very least keep a data structure of which segments are "near" points
    // (extending convex hull of segment two spaces in each direction appears to be sufficient)
    public boolean addable(SegmentSet other) {
        for (Segment mys : segments) {
            for (Segment others : other.segments) {
                if (mys.begin.equals(others.begin)) return false;
                if (mys.end.equals(others.end)) return false;
                if (mys.end.equals(others.begin)) return false;
                if (mys.begin.equals(others.end)) return false;
                if (mys.intersects(others)) return false;
            }
        }
        return true;
    }

    public void addSegmentSet(SegmentSet other) {
        if (!addable(other)) throw new RuntimeException("Can't add that Segment Set, why didn't you check first?");
        segments.addAll(other.segments);
    }
}
