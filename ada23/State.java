
public class State
{
	int width;
	int height;
	
	// 1-5 = ball
	// @ = hole
	// * = filled hole
	// - = ball flight
	// . = empty space
	char[][] golfgame;

	public boolean isBall(int x,int y) { return golfgame[x][y] >= '0' && golfgame[x][y] <= '9'; }
	public int ballValue(int x,int y) 
	{
		if (!isBall(x,y)) throw new RuntimeException("can't get value of not a ball!");
		return golfgame[x][y] - '0';
	}
	
	public boolean isHole(int x,int y) { return golfgame[x][y] == '@'; }
	public boolean isFilledHole(int x,int y) { return golfgame[x][y] == '*'; }
	public boolean isFlight(int x,int y) { return golfgame[x][y] == '-'; }
	public boolean blocksFlight(int x,int y) { return isBall(x,y) || isHole(x,y) || isFilledHole(x,y) || isFlight(x,y); }
	public boolean blocksFlightLast(int x,int y) { return isBall(x,y) || isFilledHole(x,y) || isFlight(x,y); }
	public boolean onBoard(int x, int y) { return x >= 0 && x < width && y >= 0 && y < height; }
	
	public void setFlight(int x,int y) { golfgame[x][y] = '-'; }
	public void setBall(int x,int y, int value)
	{
		if (isHole(x,y)) golfgame[x][y] = '*';
		else golfgame[x][y] = (char)(value + '0');
	}
	

	
	public boolean isDone()
	{
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{				
				if (isBall(x,y) || isHole(x,y)) return false;
			}
		}
		return true;
	}
	
	public State(GridFileReader gfr)
	{
		width = gfr.getWidth();
		height = gfr.getHeight();
		golfgame = new char[width][height];

		int numBalls = 0;
		int numHoles = 0;

		
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				golfgame[x][y] = gfr.getBlock("BALLS")[x][y].charAt(0);
				
				if (golfgame[x][y] >= '0' && golfgame[x][y] <= '9') ++numBalls;
				else if (golfgame[x][y] == '@') ++numHoles;
				else if (golfgame[x][y] == '.') {}
				else throw new RuntimeException("Character '" + golfgame[x][y] + "' should not be in board!");
			}
		}
		
		if (numBalls != numHoles) throw new RuntimeException("Ball count does not match hole count!");
	}
	
	public State(State right)
	{
		this.width = right.width;
		this.height = right.height;
		this.golfgame = new char[width][height];
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				this.golfgame[x][y] = right.golfgame[x][y];
			}
		}
	}
}