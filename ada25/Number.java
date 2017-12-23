
import java.awt.Point;
import java.util.regex.*;
import java.util.*;

public class Number
{
	public enum DirType 
	{ 
		ACROSS(new Point(1,0),"A"), 
		DOWN(new Point(0,1),"D");
		
		private final String sigil;
		private final Point delta;
		
		public String getSigil() { return sigil; }
		public Point getDelta() { return delta; }
		DirType(Point p,String s) { sigil = s; delta = p; }
	} ;
	Vector<Point> points = new Vector<Point>();
	String sigil;
	int minimum;
	int maximum;
	

	static boolean CanMake(Board b,int x,int y,DirType dir)
	{
		Point delta = dir.getDelta();
		if (!b.isNum(x,y)) return false;
		
		// we shouldn't exist if there is empty space behind us;
		int tx = x - delta.x;
		int ty = y - delta.y;
		if (b.onBoard(tx,ty) && !b.isBlock(tx,ty)) return false;
			
		int cctr = 1; // the one we're standing on.
		while(true)
		{
			int nx = x + cctr * delta.x;
			int ny = y + cctr * delta.y;
			if (!b.onBoard(nx,ny)) break;
			if (b.isBlock(nx,ny)) break;
			++cctr;
		}
		return cctr > 1;
	}
	
	public Number(Board b, int x, int y, DirType dir)
	{
		if (!CanMake(b,x,y,dir)) throw new RuntimeException("You should have checked!");
		int numpart = b.getNum(x,y);
		sigil = "" + numpart + dir.getSigil();
		
		minimum = 1;
		maximum = 9;
		points.add(new Point(x,y));
		int cctr = 1;
		Point delta = dir.getDelta();
		while(true)
		{
			int nx = x + cctr * delta.x;
			int ny = y + cctr * delta.y;
			if (!b.onBoard(nx,ny)) break;
			if (b.isBlock(nx,ny)) break;

			minimum *= 10;
			maximum = 10 * maximum + 9;
			points.add(new Point(nx,ny));
			++cctr;
		}
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(sigil + "(" + points.size() + "):");
		for (Point p : points) { sb.append(" (" + p.x + "," + p.y + ")"); }
		return sb.toString();
	}
	
}
