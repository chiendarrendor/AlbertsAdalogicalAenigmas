// contains the set of all paths that can terminate here

import java.util.HashSet;
import java.util.Set;

public class PossiblePaths
{
    Set<Path> paths = new HashSet<Path>();

    public PossiblePaths() {}
    public PossiblePaths(PossiblePaths right) { paths.addAll(right.paths); }
}
