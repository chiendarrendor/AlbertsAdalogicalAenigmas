
import java.util.*;
import java.awt.Point;

public class State
{
	public static char EMPTY = '.';
	
	public class ActorInfo
	{
		char house;
		int curx;
		int cury;
		boolean isHome = false;
		Vector<Point> path = new Vector<Point>();
		public ActorInfo(char h,int x,int y) { house = h ; curx = x; cury = y; path.add(new Point(x,y)); }
	}
	
	Vector<ActorInfo> actors = new Vector<ActorInfo>();
	Map<Character,Vector<ActorInfo>> actorsByHouse = new HashMap<Character,Vector<ActorInfo>>();
	Map<Board.Region,Set<Character>> regionPossibles = new HashMap<Board.Region,Set<Character>>();
	
	
	// So, we need to differentiate here between a 'move', which blocks everyone, including mover
	// and a 'Was' which blocks everyone except mover.  Currently moves block everyone except mover.
	
	public class SpaceInfo
	{
		ActorInfo actor = null;
		boolean isMove = false;
	}
	
	SpaceInfo[][] spaces;

	Board theBoard;
	int getWidth() { return theBoard.width; }
	int getHeight() { return theBoard.height; }
	
	public boolean blocksActor(ActorInfo ai,int x,int y)
	{
		if (spaces[x][y].actor == null) return false;
		if (spaces[x][y].actor != ai) return true;
		// if we get here, we have the same actor.
		return spaces[x][y].isMove;
	}
	
	public boolean isMove(int x,int y)
	{
		if (spaces[x][y].actor == null) return false;
		return spaces[x][y].isMove;
	}
	
	public boolean isWas(int x,int y)
	{
		if (spaces[x][y].actor == null) return false;
		return !spaces[x][y].isMove;
	}
	
	public ActorInfo actorCurrentlyAt(int x,int y)
	{
		if (spaces[x][y].actor == null) return null;
		if (spaces[x][y].actor.curx == x && spaces[x][y].actor.cury == y) return spaces[x][y].actor;
		return null;
	}
	
	public ActorInfo actorAt(int x,int y)
	{
		return spaces[x][y].actor;
	}
		
	public void Move(ActorInfo ai,int newx,int newy)
	{
		SpaceInfo newSpace = spaces[newx][newy];
		SpaceInfo oldSpace = spaces[ai.curx][ai.cury];
		if (newSpace.actor != null)
		{ 
			if (newSpace.actor != oldSpace.actor) throw new RuntimeException("Move to non-empty space!");
			if (newSpace.isMove) throw new RuntimeException("Actor has already moved there!");
		}
		
		newSpace.actor = ai;
		newSpace.isMove = true;
		
		ai.curx = newx;
		ai.cury = newy;
		ai.path.add(new Point(newx,newy));
	}

	public void SetWas(ActorInfo ai,int wasx,int wasy)
	{
		SpaceInfo si = spaces[wasx][wasy];
		if (si.actor != null) throw new RuntimeException("Was to non-empty space!");
	
		si.actor = ai;
		si.isMove = false;
	}
	
	public boolean MustMove(int x,int y)
	{
		ActorInfo ai = actorCurrentlyAt(x,y);
		if (ai == null) return false;
		Board.Region region = theBoard.regions[x][y];
		Set<Character> possibles = regionPossibles.get(region);
		return !possibles.contains(ai.house);
	}
	
	


	
	// if the ActorInfo is actually in the given space,
	// and the space is in a region with a unique house
	// and the unique house matches the ActorInfo's house.
	public boolean isHome(int x,int y)
	{
		ActorInfo ai = spaces[x][y].actor;
		if (ai == null) return false;
		if (ai.curx != x || ai.cury != y) return false;
		Board.Region region = theBoard.regions[x][y];
		Set<Character> rps = regionPossibles.get(region);
		if (rps.size() != 1) return false;
		char regionHouse = rps.iterator().next();
		return regionHouse == ai.house;
	}
		
	
	
	

	
	public State(Board b,String[][] letters)
	{
		Set<Character> houses = new HashSet<Character>();
		theBoard = b;
		
		spaces = new SpaceInfo[getWidth()][getHeight()];
		for (int x = 0 ; x < getWidth() ; ++x)
		{
			for (int y = 0 ; y < getHeight() ; ++y)
			{
				spaces[x][y] = new SpaceInfo();
			
				char tc = letters[x][y].charAt(0);
				if (tc == EMPTY) continue;
				
				// Every non EMPTY space in letters, is a piece that can move independently.
				// furthermore, the set of all unique letters of all actors, is exactly the set of 
				// names of houses (regions) on the board, so they should have the same cardinality.
				ActorInfo ai = new ActorInfo(tc,x,y);
				actors.add(ai);
				houses.add(tc);
				spaces[x][y].actor = ai;
				spaces[x][y].isMove = true;
				if (!actorsByHouse.containsKey(tc))
				{
					actorsByHouse.put(tc,new Vector<ActorInfo>());
				}
				actorsByHouse.get(tc).add(ai);
				
				
			}
		}
		
		if (houses.size() != theBoard.rmap.size()) throw new RuntimeException("different number of unique actors as houses!");
		for (Board.Region r : theBoard.rmap.values())
		{
			regionPossibles.put(r,new HashSet<Character>());
			for (char c: houses) { regionPossibles.get(r).add(c); }
		}
	}
	
	public void ShowState()
	{
		for (Map.Entry<Board.Region,Set<Character>> ent : regionPossibles.entrySet())
		{
			Board.Region region = ent.getKey();
			System.out.print("For Region: " + region);
			System.out.print("   ");
			for (char c : regionPossibles.get(region)) { System.out.print(c); }
			System.out.println("");
		}
		for (char house : actorsByHouse.keySet())
		{
			System.out.print("House " + house + "(");
			System.out.print(actorsByHouse.get(house).size() + "):");

			for (Board.Region region : regionPossibles.keySet())
			{
				if (!regionPossibles.get(region).contains(house)) continue;
				System.out.print(" " + region);
			}
			System.out.println("");
		}	
		
		for (ActorInfo ai : actors)
		{
			System.out.print("Actor " + ai.house + ":");
			for (Point p : ai.path) { System.out.print(" (" + p.x + "," + p.y + ")"); }
			System.out.println("");
		}
		
	}
}
		
	

