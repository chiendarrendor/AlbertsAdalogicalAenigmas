
import java.util.*;

public class Numbers
{
	static final int minnum = 1;
	static final int maxnum = 7;

	public static class IntSet extends HashSet<Integer>
	{	
		public boolean hasLarger(int x)
		{
			for (int i = x+1 ; i <= maxnum; ++i) if (contains(i)) return true;
			return false;
		}
		public boolean hasSmaller(int x)
		{
			for (int i = x-1 ; i >= minnum ; --i) if (contains(i)) return true;
			return false;
		}
		
		public String toString()
		{
			StringBuffer sb = new StringBuffer();
			for (int i = minnum ; i <= maxnum ; ++i) if (contains(i)) sb.append(i);
			return sb.toString();
		}
		
		
		
	}
	


	IntSet[][] numbers = null;
	int width;
	int height;
	int guessdepth;
	
	
	public Numbers(Board b)
	{
		width = b.width;
		height = b.height;
		numbers = new IntSet[width][height];
		guessdepth = 0;
		
		
		for (int x = 0 ; x < width ; x++)
		{
			for (int y = 0 ; y < height ; y++)
			{
				if (!b.onBoard(x,y)) continue;
				numbers[x][y] = new IntSet();
				for (int i = minnum ; i <= maxnum ; ++i) { numbers[x][y].add(i); }
			}
		}
	}
	
	public Numbers(Numbers right)
	{
		width = right.width;
		height = right.height;
		numbers = new IntSet[width][height];
		guessdepth = 1 + right.guessdepth;
		
		for (int x = 0 ; x < width ; x++)
		{
			for (int y = 0 ; y < height ; y++)
			{
				if (right.numbers[x][y] == null) continue;
				numbers[x][y] = new IntSet();
				for (int i : right.numbers[x][y]) numbers[x][y].add(i);
			}
		}
	}
	
	public boolean isSolution()
	{
		for (int x = 0 ; x < width ; x++)
		{
			for (int y = 0 ; y < height ; y++)
			{
				if (numbers[x][y] == null) continue;
				if (numbers[x][y].size() != 1) return false;
			}
		}
		return true;
	}
	
	
	
}