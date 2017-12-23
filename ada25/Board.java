
import java.awt.Point;
import java.util.*;
import java.util.regex.*;


public class Board
{
	int width;
	int height;
	boolean[][] isSpecial;
	String[][] rawboard;
	String verticalChars;
	String horizontalChars;
	
	public boolean onBoard(int x, int y) { return x >= 0 && x < width && y >= 0 && y < height; }
	static Pattern matchnum=Pattern.compile("^(\\d+)$");
	static Pattern matchblock=Pattern.compile("^#$");	
	
	// operators on rawboard
	public boolean isBlock(int x,int y)
	{
		Matcher m = matchblock.matcher(rawboard[x][y]);
		return m.find();
	}
	
	public boolean isNum(int x,int y)
	{
		Matcher m = matchnum.matcher(rawboard[x][y]);
		return m.find();
	}
	
	public int getNum(int x,int y)
	{
		if (!isNum(x,y)) throw new RuntimeException("Can't get Number when it isn't a number!");
		Matcher m = matchnum.matcher(rawboard[x][y]);
		m.find();
		return Integer.parseInt(m.group(1));
	}
		
	
	
	
	public Board(GridFileReader gfr)
	{
		width = gfr.getWidth();
		height = gfr.getHeight();
		isSpecial = new boolean[width][height];
		rawboard = gfr.getBlock("BLOCKNUMS");
		verticalChars = gfr.getVar("DOWNLETTERS");
		horizontalChars = gfr.getVar("ACROSSLETTERS");
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				isSpecial[x][y] = gfr.getBlock("SPECIALS")[x][y].equals("@");
			}
		}
	}
}