

import java.util.*;


public class GalaxyBoardAreaOverlays
{

	Map<Integer,GalaxyBoard> overlays = new HashMap<Integer,GalaxyBoard>();

	public class OverlayState
	{
		boolean isCore;  // true if this cell is the initial cells of the area (and therefore singular)
		boolean isSource; // true if this cell is one of the claimed cells of the source board (and therefore singular)
		Set<Integer> areaIds = new HashSet<Integer>();
		public OverlayState(boolean iscore,boolean issource) { isCore = iscore; isSource = issource; }
	}
	
	OverlayState[][] cellstates;
	
	public GalaxyBoardAreaOverlays(GalaxyBoard source)
	{
		cellstates = new OverlayState[source.getWidth()][source.getHeight()];
		ForEachCell.op(source.getWidth(),source.getHeight(),new ForEachCell.CellOp() {
			public boolean op(int x,int y)
			{
				GalaxyBoard.Area.Pair cpair = source.getBoardCell(x,y);
				if (cpair == null) return true;
				cellstates[x][y] = new OverlayState(cpair.isInitial,true);
				cellstates[x][y].areaIds.add(cpair.getAreaId());
				return true;
			}
		});
		
		for (GalaxyBoard.Area area : source.getAreas())
		{
			GalaxyBoard forArea = new GalaxyBoard(source);
			Expand(forArea,area.getId());
			overlays.put(area.getId(),forArea);
					
			ForEachCell.op(source.getWidth(),source.getHeight(),new ForEachCell.CellOp() {
				public boolean op(int x,int y)
				{
					GalaxyBoard.Area.Pair cpair = forArea.getBoardCell(x,y);
					if (cpair == null) return true;
					if (cpair.getAreaId() != area.getId()) return true;
					if (cellstates[x][y] == null)
					{
						cellstates[x][y] = new OverlayState(false,false);
					}
					cellstates[x][y].areaIds.add(area.getId());
					return true;
				}
			});
		}
	}
	
	private void ExpandOne(GalaxyBoard b,GalaxyBoard.Area a,GalaxyBoard.Area.Pair p,int dx,int dy,Vector<GalaxyBoard.Area.Pair> queue)
	{
		int nx = p.p1.x + dx;
		int ny = p.p1.y + dy;
		if (!a.addCell(nx,ny)) return;
		queue.add(b.getBoardCell(nx,ny));
	}
	
	private void Expand(GalaxyBoard newBoard,int areaid)
	{
		// invariant:  the queue contains Legal Pairs of AreaId that we have not
		// yet expanded.
		Vector<GalaxyBoard.Area.Pair> queue = new Vector<GalaxyBoard.Area.Pair>();
		GalaxyBoard.Area theArea = newBoard.getAreaById(areaid);
		for (GalaxyBoard.Area.Pair pair : theArea.pairs) { queue.add(pair); }
		
		while(queue.size() > 0)
		{
			GalaxyBoard.Area.Pair thePair = queue.remove(0);
			ExpandOne(newBoard,theArea,thePair,1,0,queue);
			ExpandOne(newBoard,theArea,thePair,-1,0,queue);
			ExpandOne(newBoard,theArea,thePair,0,1,queue);
			ExpandOne(newBoard,theArea,thePair,0,-1,queue);
		}
	}
	
}