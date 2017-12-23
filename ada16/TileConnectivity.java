
import java.util.*;
import java.awt.Point;

// Determines the following things about a board:
// 1) are all tiles currently connected via tiles and empty spaces?  (if not, CONTRADICTION)
// 2) are there any empty spaces where placing a tree would disconnect the tiles? 
//     (if yes, add tiles to those spaces on the board and return LOGICED)
// 3) otherwise, return STYMIED








public class TileConnectivity
{

	private static class GridGraphReference implements GridGraph.GridReference
	{
		private Board theBoard = null;
		private Set<Point> inclusions = null;
		public GridGraphReference(Board b) { theBoard = b; }
		public GridGraphReference(Board b,Set<Point> incl) { theBoard = b ; inclusions = incl; }
		public int getWidth() { return theBoard.width; }
		public int getHeight() { return theBoard.height; }
		public boolean isIncludedCell(int x, int y) 
		{
			if (theBoard.isTree(x,y)) return false;
			if (inclusions == null) return true;
			return inclusions.contains(new Point(x,y));
		}
		public boolean edgeExitsEast(int x, int y) { return true; }
		public boolean edgeExitsSouth(int x,int y) { return true; }
	}

	private static class ATCResult
	{
		public boolean allTilesTogether = false;
		public boolean boardConnected = false;
		public Set<Point> tileSet = null;
	}
	
	
	
	private static ATCResult allTilesConnected(Board b,GridGraph gg)
	{
		ATCResult result = new ATCResult();
		if (gg.isConnected())
		{
			result.boardConnected = true;
			result.allTilesTogether = true;
			return result;
		}
		
		// if we get here, boardConnected = false
		
		// if we have more than one connected component, as long as all of the tiles are in one component, it should still be okay.
		List<Set<Point>> concoms = gg.connectedSets();
		
		for (Set<Point> concom : concoms)
		{
			boolean seenTile = false;
			for (Point p : concom)
			{
				if (b.isTile(p.x,p.y)) { seenTile = true ; break; }
			}
			
			if (seenTile && result.tileSet != null)
			{
				result.allTilesTogether = false;
				result.tileSet = null;
				return result;
			}
			
			if (seenTile)
			{
				result.allTilesTogether = true;
				result.tileSet = concom;
			}
		}
		return result;
	}
	
	// articulation points are not strong enough
	// it is possible than an articulation point 
	// cuts off only empty space, and therefore
	// should not be a known required tile add.
	private static boolean PointBreaksBoard(Board b,Point p)
	{
		Board newB = new Board(b);
		newB.addTree(p.x,p.y);
		GridGraph mgg = new GridGraph(new GridGraphReference(newB));
		ATCResult atcr = allTilesConnected(newB,mgg);
		return !atcr.allTilesTogether;
	}
	
	
	
	
	
	

	public static LogicStatus UpdateTileConnectivity(Board b)
	{
		LogicStatus result = LogicStatus.STYMIED;
		GridGraph gg = new GridGraph(new GridGraphReference(b));
		
		ATCResult atcr = allTilesConnected(b,gg);
		if (!atcr.allTilesTogether) return LogicStatus.CONTRADICTION;
		
		if (!atcr.boardConnected) 
		{
			gg = new GridGraph(new GridGraphReference(b,atcr.tileSet));
		}
		
		Set<Point> aps = gg.getArticulationPoints();
		
		for (Point p : aps)
		{
			if (!PointBreaksBoard(b,p)) continue;
		
			if (b.isEmpty(p.x,p.y))
			{
				b.addTile(p.x,p.y);
				result = LogicStatus.LOGICED;
			}
		}
		return result;
	}
}
		