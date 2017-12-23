
import java.util.*;
import java.awt.Point;
import java.awt.image.*;
import java.awt.*;


public class ClueSet
{
	static public Map<Character,Point> deltaByDir;
	static
	{
		deltaByDir = new HashMap<Character,Point>();
		deltaByDir.put('N',new Point(0,-1));
		deltaByDir.put('S',new Point(0,1));
		deltaByDir.put('E',new Point(1,0));
		deltaByDir.put('W',new Point(-1,0));
	}

	public static Point getDeltaOfDir(char dir)
	{
		if (deltaByDir.containsKey(dir)) return deltaByDir.get(dir);
		return null;
	}
	

	public static abstract class Clue
	{
		static int cluewidth = 25;
		static int clueheight = 25;
		Vector<Point> operantPoints = new Vector<Point>();
		public abstract void AlterCellImage(Point p,BufferedImage bi);
		public abstract LogicStatus Apply(Numbers num);
		
		protected static void DrawImageOnCenter(Point center,Graphics2D canvas, BufferedImage image)
		{
			int ulx = center.x - cluewidth/2;
			int uly = center.y - clueheight/2;
			canvas.drawImage(image,ulx,uly,null);
		}
	}
	
	Set<Clue> clues = new HashSet<Clue>();
	Map<Point,Set<Clue>> cluemap = new HashMap<Point,Set<Clue>>();
	
	public void applyPointToClue(Clue c,Point p)
	{
		c.operantPoints.add(p);
		if (!cluemap.containsKey(p)) cluemap.put(p,new HashSet<Clue>());
		cluemap.get(p).add(c);
		clues.add(c);
	}
	
	public void showClues()
	{
		for (Clue c : clues)
		{
			System.out.print("Clue:");
			for (Point p : c.operantPoints) { System.out.print(" (" + p.x + "," + p.y + ")"); }
			System.out.println("");
		}
	}
	
	
}
