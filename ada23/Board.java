public class Board
{
	int width;
	int height;
	boolean[][] hasTrap;
	char[][] letters;
	
	public Board(GridFileReader gfr)
	{
		width = gfr.getWidth();
		height = gfr.getHeight();
		
		hasTrap = new boolean[width][height];
		letters = new char[width][height];
		
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				if (gfr.getBlock("TRAPS")[x][y].equals("#")) hasTrap[x][y] = true;
				else if (gfr.getBlock("TRAPS")[x][y].equals(".")) hasTrap[x][y] = false;
				else throw new RuntimeException("illegal TRAPS character " + gfr.getBlock("TRAPS")[x][y]);
				
				letters[x][y] = gfr.getBlock("LETTERS")[x][y].charAt(0);
			}
		}
	}
}