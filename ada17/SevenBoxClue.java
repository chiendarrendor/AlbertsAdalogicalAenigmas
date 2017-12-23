import java.awt.Point;
import java.awt.image.*;
import java.util.*;

public class SevenBoxClue extends ClueSet.Clue
{
	protected SevenBoxClue(ClueSet cs,int ulx,int uly, int dx,int dy)
	{
		for (int i = 0 ; i < 7 ; ++i) cs.applyPointToClue(this,new Point(ulx+i*dx,uly+i*dy));
	}
	
	public void AlterCellImage(Point p,BufferedImage bi)
	{
	}
	
	public LogicStatus Apply(Numbers nums)
	{
		LogicStatus result = LogicStatus.STYMIED;
		
		Map<Integer,Vector<Point>> hasNum = new HashMap<Integer,Vector<Point>>();
		for (int i = 1 ; i <= 7 ; ++i ) hasNum.put(i,new Vector<Point>());
		for (Point p : operantPoints)
		{
			if (nums.numbers[p.x][p.y].size() == 0) return LogicStatus.CONTRADICTION;
		
			for(int i : nums.numbers[p.x][p.y])
			{
				hasNum.get(i).add(p);
			}
		}
		
		for (int i = 1 ; i <= 7 ; ++i)
		{
			Vector<Point> vp = hasNum.get(i);
			if (vp.size() == 0) return LogicStatus.CONTRADICTION;
			if (vp.size() > 1) continue;
			
			Point unique = vp.elementAt(0);
			if (nums.numbers[unique.x][unique.y].size() == 1) continue;
			nums.numbers[unique.x][unique.y] = new Numbers.IntSet();
			nums.numbers[unique.x][unique.y].add(i);
			result = LogicStatus.LOGICED;
		}
			
		for (Point p : operantPoints)
		{
			if (nums.numbers[p.x][p.y].size() == 0) return LogicStatus.CONTRADICTION;
			if (nums.numbers[p.x][p.y].size() > 1) continue;
			
			int unique = nums.numbers[p.x][p.y].iterator().next();
			
			for (Point pprime : operantPoints)
			{
				if (pprime == p) continue;
				if (nums.numbers[pprime.x][pprime.y].contains(unique))
				{
					nums.numbers[pprime.x][pprime.y].remove(unique);
					result = LogicStatus.LOGICED;
				}
			}
		}
				
		// detection of simple 'cliques'
		HashMap<String,HashSet<Point>> vps = new HashMap<String,HashSet<Point>>();
		for (Point p : operantPoints)
		{
			String ps = nums.numbers[p.x][p.y].toString();
			if (!vps.containsKey(ps)) vps.put(ps,new HashSet<Point>());
			vps.get(ps).add(p);
		}
		for (String s : vps.keySet())
		{
			HashSet<Point> points = vps.get(s);
			Point exemplar = points.iterator().next();
			Numbers.IntSet is = nums.numbers[exemplar.x][exemplar.y];
			if (points.size() > s.length()) return LogicStatus.CONTRADICTION;
			if (points.size() < s.length()) continue;
			if (points.size() == 1) continue;
			
			for (Point pprime : operantPoints)
			{
				if (points.contains(pprime)) continue;
				Numbers.IntSet isprime = nums.numbers[pprime.x][pprime.y];
				for (int i : is)
				{
					if (isprime.contains(i))
					{
						result = LogicStatus.LOGICED;
						isprime.remove(i);
					}
				}
			}
		}
	
	
	
	
	
	
	
		return result;
	}
	
	
	public static void MakeSevenBox(ClueSet cs, int ulx,int uly)
	{
		for (int i = 0 ; i < 7 ; ++i)
		{
			new SevenBoxClue(cs,ulx,uly+i,1,0);
			new SevenBoxClue(cs,ulx+i,uly,0,1);
		}
	}
			

}