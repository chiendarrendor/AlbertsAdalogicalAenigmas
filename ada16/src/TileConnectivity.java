
import grid.graph.GridGraph;
import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.*;
import java.awt.Point;

// Determines the following things about a board:
// 1) are all tiles currently connected via tiles and empty spaces?  (if not, CONTRADICTION)
// 2) are there any empty spaces where placing a tree would disconnect the tiles? 
//     (if yes, add tiles to those spaces on the board and return LOGICED)
// 3) otherwise, return STYMIED








public class TileConnectivity implements LogicStep<Board> {

	private static class MyConnectivityReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
		private Board b;
		public MyConnectivityReference(Board b) { this.b = b; }
		@Override public int getWidth() { return b.width; }
		@Override public int getHeight() { return b.height; }
		@Override public boolean isConnectedCell(int x, int y) { return b.isTile(x,y); }
		@Override public boolean isPossibleCell(int x, int y) { return b.isEmpty(x,y); }
		@Override public boolean edgeExitsEast(int x, int y) { return true; }
		@Override public boolean edgeExitsSouth(int x, int y) { return true; }
	}

	public static LogicStatus UpdateTileConnectivity(Board b) {
		PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyConnectivityReference(b));

		if (pcd.isEmpty()) return LogicStatus.STYMIED;
		if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

		LogicStatus result = LogicStatus.STYMIED;

		for (Point p : pcd.getNonConnectingPossibles()) {
			if (!b.isEmpty(p.x,p.y)) throw new RuntimeException("non connecting possible is not empty!");
			b.addTree(p.x,p.y);
			result = LogicStatus.LOGICED;
		}

		for (Point p : pcd.getArticulatingPossibles()) {
			if (!b.isEmpty(p.x,p.y)) throw new RuntimeException("articulating possible is not empty!");
			b.addTile(p.x,p.y);
			result = LogicStatus.LOGICED;
		}
		return result;
	}

	@Override public LogicStatus apply(Board thing) { return UpdateTileConnectivity(thing); }
}
		