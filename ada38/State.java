
import java.util.*;
import java.awt.Point;

public class State
{
	public class Traveler
	{
		int curx;
		int cury;
		int movedist;
		boolean moved;
		
		public Traveler(int x,int y,int dist) { curx = x; cury = y; movedist = dist; moved = false; }
	}
	
	// this set of fields/methods is to allow the logic to specify a space as having been moved through
	private Traveler theShadow = new Traveler(-1,-1,-1);
	public boolean isShadow(int x,int y) { return travelerboard[x][y] == theShadow; }
	public void setShadow(int x,int y) { travelerboard[x][y] = theShadow; }
	
	public Traveler getTraveler(int x,int y) { return travelerboard[x][y]; }
	
	private Board theBoard;
	public Board getBoard() { return theBoard; }
	public int getWidth() { return theBoard.getWidth(); }
	public int getHeight() { return theBoard.getHeight(); }
	public  Traveler[][] travelerboard;
	public  Vector<Traveler> travelers = new Vector<Traveler>();
	
	public State(Board b,String[][] numbers)
	{
		theBoard = b;
		travelerboard = new Traveler[getWidth()][getHeight()];
		
		for (int x = 0 ; x < getWidth() ; ++x)
		{
			for (int y = 0 ; y < getHeight() ; ++y)
			{
				char tc = numbers[x][y].charAt(0);
				if (tc == '.') continue;
				
				// if we are here, we have a new traveler
				travelerboard[x][y] = new Traveler(x,y,Character.getNumericValue(tc));
				travelers.add(travelerboard[x][y]);				
			}
		}
	}
	
	public State(State right)
	{
		theBoard = right.theBoard;
		travelerboard = new Traveler[getWidth()][getHeight()];

		for (int x = 0 ; x < getWidth() ; ++x)
		{
			for (int y = 0 ; y < getHeight() ; ++y)
			{
				if (right.isShadow(x,y))
				{
					setShadow(x,y);
					continue;
				}
				if (right.travelerboard[x][y] == null) continue;
				// ok, if we get here, travelerboard[x][y] is not null, but it's not a shadow either.  must be a traveler
				Traveler ot = right.travelerboard[x][y];
				Traveler t = new Traveler(ot.curx,ot.cury,ot.movedist);
				t.moved = ot.moved;
				travelerboard[x][y] = t;
				travelers.add(t);
			}
		}
	}
	
	
}
		
	

