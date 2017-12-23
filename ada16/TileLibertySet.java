

// this class is an adjunct to the Board class that organizes the data differently.
// every tile belongs to exactly one set.   all empty adjacent spaces of a set are 
// considered 'liberties.'   A tree blocks an adjacent space from being a liberty,
// as does the edge of the board.

import java.awt.Point;
import java.util.*;

public class TileLibertySet
{
	private int width;
	private int height;
	
	private class TileSet
	{
		public Set<Point> cells = new HashSet<Point>();
		public Set<Point> liberties = new HashSet<Point>();
		public TileSet(Point mytile) { cells.add(mytile); }
		public TileSet() {}
	}
	
	private class Liberty
	{
		public Set<TileSet> tilesets = new HashSet<TileSet>();
	}
	
	private Set<TileSet> tilesetset = new HashSet<TileSet>();
	private Map<Point,TileSet> tilesets = new HashMap<Point,TileSet>();
	private Map<Point,Liberty> liberties = new HashMap<Point,Liberty>();
	private Set<Point> trees = new HashSet<Point>();
	
	public TileLibertySet(int width,int height) { this.width = width; this.height = height; }	
	public TileLibertySet(TileLibertySet right)
	{
		width = right.width;
		height = right.height;
		for (Point treep : right.trees) { trees.add(new Point(treep.x,treep.y)); }
		for (TileSet ts : right.tilesetset)
		{
			TileSet newts = new TileSet();
			tilesetset.add(newts);
			for (Point p : ts.cells) 
			{
				Point newp = new Point(p.x,p.y);
				newts.cells.add(newp);
				tilesets.put(newp,newts);
			}
			for (Point p : ts.liberties)
			{
				Point newp = new Point(p.x,p.y);
				newts.liberties.add(newp);
				GetOrCreateLiberty(newp).tilesets.add(newts);
			}
		}
	}
	
	
	
	
	private static Point[] deltas = new Point[] { new Point(0,1) , new Point(0,-1) , new Point (1,0) , new Point(-1,0) };
	private boolean onBoard(Point p) { return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height; }
	private Liberty GetOrCreateLiberty(Point p)
	{
		if (liberties.containsKey(p)) return liberties.get(p);
		Liberty lib = new Liberty();
		liberties.put(p,lib);
		return lib;
	}
	
	private void MergeTileSets(Set<TileSet> sets)
	{
		TileSet largestSet = null;
		int largestsize = 0;
		for (TileSet ts: sets)
		{
			if (ts.cells.size() > largestsize) 
			{
				largestSet = ts;
				largestsize = ts.cells.size();
			}
		}
		sets.remove(largestSet);
		for (TileSet ts : sets)
		{
			largestSet.cells.addAll(ts.cells);
			largestSet.liberties.addAll(ts.liberties);
			
			for(Point cellp : ts.cells)
			{
				tilesets.put(cellp,largestSet);
			}
			for(Point libp : ts.liberties)
			{
				Liberty lib = liberties.get(libp);
				lib.tilesets.remove(ts);
				lib.tilesets.add(largestSet);
			}
			tilesetset.remove(ts);
		}
	}
			
	
	private void DestroyLiberty(Point tp)
	{
		if (!liberties.containsKey(tp)) return;
		Liberty lib = liberties.get(tp);
		for (TileSet ts : lib.tilesets)
		{
			ts.liberties.remove(tp);
		}
		liberties.remove(tp);
	}	
	
	
	// when a tree is added, any Tile Set containing that Liberty is removed.
	// We also must keep track of the tree points so that they can never become liberties.	
	public void AddTree(Point tp)
	{
		trees.add(tp);
		DestroyLiberty(tp);
	}
	
	// adding a tile:
	// create a new set and add it to the board
	// if we put the tile on a liberty, destroy that liberty
	// if any on-board adjacent tiles are not tiles and not trees, they must become liberties.
	// if this new tile is placed next to existing tilesets, merge the tilesets
	
	public void AddTile(Point tp)
	{
		TileSet myts = new TileSet(tp);
		tilesetset.add(myts);
		tilesets.put(tp,myts);
		DestroyLiberty(tp);
		
		for (Point delta : deltas)
		{
			Point dp = new Point(tp.x+delta.x,tp.y+delta.y);
			if (!onBoard(dp)) continue;
			if (trees.contains(dp)) continue;
			if (tilesets.containsKey(dp)) continue;
			GetOrCreateLiberty(dp).tilesets.add(myts);
			myts.liberties.add(dp);
		}
		
		Set<TileSet> toMerge = new HashSet<TileSet>();
		toMerge.add(myts);
		
		for (Point delta : deltas)
		{
			Point dp = new Point(tp.x+delta.x,tp.y+delta.y);
			if (!tilesets.containsKey(dp)) continue;
			toMerge.add(tilesets.get(dp));
		}
		
		MergeTileSets(toMerge);
	}
	
	public void print(String title)
	{
		System.out.println(title);
		System.out.println("TileSets:");
		for (TileSet ts : tilesetset)
		{
			System.out.println("  TileSet: " + ts);
			System.out.print("    Cells:");
			for (Point p : ts.cells) { System.out.print(" (" + p.x + "," + p.y + ")"); }
			System.out.println("");
			System.out.print("    Liberties:");
			for (Point p : ts.liberties) { System.out.print(" (" + p.x + "," + p.y + ")");  }
			System.out.println("");
		}
		System.out.println("Liberties:");
		for (Map.Entry<Point,Liberty> ent : liberties.entrySet())
		{
			System.out.print("  Liberty (" + ent.getKey().x + "," + ent.getKey().y + "):");
			for (TileSet ts : ent.getValue().tilesets) { System.out.print(" " + ts); }
			System.out.println("");
		}
		System.out.println("");
		System.out.print("Trees:");
		for (Point p : trees) { System.out.print(" (" + p.x + "," + p.y + ")"); }
		System.out.println("");
	}
	
	// want to find the set with smallest liberties, and then return the liberty with the largest # of sets.
	// this is called on a Board that is not solved, and has been determined legal by TileConnectivity, so
	// every Set should have at least 2 liberties.
	public Point BestGuess()
	{
		int minlibs = width * height;
		TileSet mints = null;
		for (TileSet ts : tilesetset)
		{
			if (ts.liberties.size() < minlibs)
			{
				minlibs = ts.liberties.size();
				mints = ts;
			}
		}
		
		int maxtilesets = 0;
		Point libp = null;
		for (Point p : mints.liberties)
		{
			Liberty lib = liberties.get(p);
			Set<TileSet> lts = lib.tilesets;
			
		
			if (lts.size() > maxtilesets)
			{
				maxtilesets = lts.size();
				libp = p;
			}
		}
		return libp;
	}
	
	
	
	
}
	
	
	