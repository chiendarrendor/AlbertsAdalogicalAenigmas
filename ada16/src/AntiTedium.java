

// AntiTedium operates on a board thusly:
// for each row and each column:
// four tiles in a row -> CONTRADICTION
// three tiles in a row -> a tree must be on both ends -> LOGIC
// 2 . 2 , 2 . 1, 1 . 2 -> a tree must be in the middle -> LOGIC
// otherwise stymied

import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class AntiTedium implements LogicStep<Board> {

	private static LogicStatus ApplyOne(Board b,int sx,int sy,Direction d) {
		int tilecount = 0;
		Set<Point> unknowns = new HashSet<>();
		for (int i = 0 ; i < 4 ; ++i ) {
			Point p = d.delta(sx,sy,i);
			if (b.isTree(p.x,p.y)) {continue;}
			else if (b.isTile(p.x,p.y)) {++tilecount; continue;}
			else unknowns.add(p);
		}
		if (tilecount == 4) return LogicStatus.CONTRADICTION;
		if (unknowns.size() == 0) return LogicStatus.STYMIED;
		if (tilecount < 3) return LogicStatus.STYMIED;
		unknowns.stream().forEach(p->b.addTree(p.x,p.y));
		return LogicStatus.LOGICED;
	}

	public static LogicStatus ApplyAntiTedium(Board b) {
		LogicStatus result = LogicStatus.STYMIED;
		for (int y = 0; y < b.height; ++y) {
			for (int x = 0; x < b.width; ++x) {
				if (b.width - x >= 4) {
					LogicStatus ls = ApplyOne(b, x, y, Direction.EAST);
					if (ls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
					if (ls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
				}
				if (b.height - y >= 4) {
					LogicStatus ls = ApplyOne(b, x, y, Direction.SOUTH);
					if (ls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
					if (ls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
				}
			}
		}
		return result;
	}

	@Override public LogicStatus apply(Board thing) { return ApplyAntiTedium(thing); }
}