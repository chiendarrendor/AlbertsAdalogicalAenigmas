

import java.awt.Point;
import java.util.*;


public class Numbers
{

	public static class NumberVector extends Vector<Number> {};


	NumberVector[][] numbergrid;
	Board theBoard;
	Map<String,Number> numberMap = new HashMap<String,Number>();
	
	
	
	private void AddNumber(int x,int y,Number.DirType dir)
	{
		if (!Number.CanMake(theBoard,x,y,dir)) return;
		
		Number newn = new Number(theBoard,x,y,dir);
		numberMap.put(newn.sigil,newn);
		for (Point p : newn.points)
		{
			if (numbergrid[p.x][p.y] == null) numbergrid[p.x][p.y] = new NumberVector();
			numbergrid[p.x][p.y].add(newn);
		}
	}
	
	
	
	public Numbers(Board b)
	{
		theBoard = b;
		numbergrid = new NumberVector[b.width][b.height];
		
		for (int x = 0 ; x  < b.width ; ++x)
		{
			for (int y = 0 ; y < b.height ; ++y)
			{
				AddNumber(x,y,Number.DirType.ACROSS);
				AddNumber(x,y,Number.DirType.DOWN);
			}
		}		
	}
	
	public void show()
	{
		for (int x = 0 ; x  < theBoard.width ; ++x)
		{
			for (int y = 0 ; y < theBoard.height ; ++y)
			{
				Vector<Number> nn = numbergrid[x][y];
				System.out.print("(" + x + "," + y + "): ");
				if (nn == null)
				{
					System.out.println("--NONE--");
					continue;
				}
				
				for (Number num : nn)
				{
					System.out.print(" " + num.sigil);
				}
				System.out.println("");
			}
		}
	}
	
	
	
}
