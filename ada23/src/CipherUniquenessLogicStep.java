import grid.logic.LogicStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CipherUniquenessLogicStep implements grid.logic.LogicStep<Board> {
    @Override
    public LogicStatus apply(Board thing) {
        Map<Integer,ArrayList<Character>> uniques = new HashMap<>();

        for (char c : thing.getCharMap().getCiphers()) {
            Set<Integer> sizes = thing.getCharMap().getEntrySet(c);
            if (sizes.size() > 1) continue;
            if (sizes.size() == 0) return LogicStatus.CONTRADICTION;
            int usize = sizes.iterator().next();
            if (!uniques.containsKey(usize)) uniques.put(usize,new ArrayList<Character>());
            uniques.get(usize).add(c);
        }

        // if any of them have more than one item, it's broken.
        if (uniques.values().stream().anyMatch((x)->x.size() > 1)) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;
        // each nuumber must be removed from all lists except the one it came from
        for (int ulen : uniques.keySet()) {
            char originator = uniques.get(ulen).iterator().next();
            for (char inp : thing.getCharMap().getCiphers()) {
                if (inp == originator) continue;
                Set<Integer> iset = thing.getCharMap().getEntrySet(inp);
                if (!iset.contains(ulen)) continue;
                iset.remove(ulen);
                result = LogicStatus.LOGICED;
            }
        }

        return result;
    }
}
