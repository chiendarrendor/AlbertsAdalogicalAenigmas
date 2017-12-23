import java.awt.Point;
import java.awt.image.*;
import java.util.*;
import java.awt.*;

// So, this clue will get two characters, an inequality, < > ^ V
// and a direction N E W S
// < > only goes with E W
// ^ V only goes with N S
// <E -- I am smaller than east
// >E -- I am larger than east
// <W -- I am larger than west
// >W -- I am smaller than west
// ^N -- I am larger than north
// VN -- I am smaller than north
// ^S -- I am smaller than south
// VS -- I am larger than south

// we can assume that self is operantPoints.elementAt(0)
// and other is operantPoints.elementAt(1)
public class InequalityClue extends ClueSet.Clue
{
	boolean iAmSmaller; // if we're here, we're either larger or smaller
	char dir;
	
	public InequalityClue(Point curloc, char ineq,char dir,Board b,ClueSet cs)
	{
		Point d = cs.getDeltaOfDir(dir);
		if (d == null) throw new RuntimeException("Invalid inequality clue direction " + dir);
		this.dir = dir;
		Point other = new Point(curloc.x+d.x,curloc.y+d.y);
		if (!b.onBoard(other)) throw new RuntimeException("dir is off board!");
		cs.applyPointToClue(this,curloc);
		cs.applyPointToClue(this,other);
		
		if      (dir == 'E' && ineq == '<') iAmSmaller = true;
		else if (dir == 'E' && ineq == '>') iAmSmaller = false;
		else if (dir == 'W' && ineq == '<') iAmSmaller = false;
		else if (dir == 'W' && ineq == '>') iAmSmaller = true;
		else if (dir == 'N' && ineq == '^') iAmSmaller = false;
		else if (dir == 'N' && ineq == 'V') iAmSmaller = true;
		else if (dir == 'S' && ineq == '^') iAmSmaller = true;
		else if (dir == 'S' && ineq == 'V') iAmSmaller = false;
		else throw new RuntimeException("invalid inequality " + ineq + " for direction " + dir);
	}
	
	// 0 = '<' 1 = '^'  2 = '>'  3 = 'V'
	static BufferedImage[] images = null;
	
	private static BufferedImage rotateCw(BufferedImage img)
	{
		BufferedImage newImage = new BufferedImage(img.getHeight(),img.getWidth(),img.getType());
		for (int x = 0 ; x < img.getWidth(); ++x)
		{
			for (int y = 0 ; y < img.getHeight() ; ++y)
			{
				newImage.setRGB( img.getHeight() - 1 - y , x , img.getRGB(x,y));
			}
		}
		return newImage;
	}
	
	
	
	public static BufferedImage getImage(int dir)
	{
		if (images != null) return images[dir];
		images = new BufferedImage[4];

		BufferedImage bi = new BufferedImage(cluewidth,clueheight,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		Color trans = new Color(0,0,0,0);
		g.setColor(trans);
		g.fillRect(0,0,cluewidth,clueheight);
		g.setColor(Color.white);
		g.fillOval(0,0,cluewidth,clueheight);
		g.setColor(Color.black);
		g.drawOval(0,0,cluewidth,clueheight);
		GridPanel.DrawStringInCell(g,Color.black,0,0,cluewidth,clueheight,"<");
	
		images[0] = bi;
		images[1] = rotateCw(images[0]);
		images[2] = rotateCw(images[1]);
		images[3] = rotateCw(images[2]);
		return images[dir];
	}	
	
	
	public void AlterCellImage(Point p,BufferedImage bi)
	{
		int cx;
		int cy;
		int rdir;
		
		boolean isMe = p.equals(operantPoints.elementAt(0));
		
		// 0 = '<' 1 = '^'  2 = '>'  3 = 'V'
		if      (isMe && dir == 'N') { cx = bi.getWidth()/2 ; cy = 0; rdir = iAmSmaller ? 3 : 1;}
		else if (isMe && dir == 'S') { cx = bi.getWidth()/2 ; cy = bi.getHeight(); rdir = iAmSmaller ? 1 : 3;}
		else if (isMe && dir == 'W') { cx = 0 ; cy = bi.getHeight()/2; rdir = iAmSmaller ? 2 : 0;}
		else if (isMe && dir == 'E') { cx = bi.getWidth(); cy = bi.getHeight()/2; rdir = iAmSmaller ? 0 : 2;}
		else if (!isMe && dir == 'S') { cx = bi.getWidth()/2 ; cy = 0; rdir = iAmSmaller ?  1 : 3; }
		else if (!isMe && dir == 'N') { cx = bi.getWidth()/2 ; cy = bi.getHeight(); rdir = iAmSmaller ? 3 : 1;}
		else if (!isMe && dir == 'E') { cx = 0 ; cy = bi.getHeight()/2;  rdir = iAmSmaller ? 0 : 2;}
		else if (!isMe && dir == 'W') { cx = bi.getWidth(); cy = bi.getHeight()/2; rdir = iAmSmaller ? 2 : 0;}		
		else throw new RuntimeException("Bwah?");	
		
		DrawImageOnCenter(new Point(cx,cy),(Graphics2D)bi.getGraphics(),getImage(rdir));
	}
	
	private LogicStatus ApplyOneSide(Numbers nums, boolean activeIsSmaller, Point actp, Point passp)
	{
		Numbers.IntSet newActive = new Numbers.IntSet();
		Numbers.IntSet passive = nums.numbers[passp.x][passp.y];
		Numbers.IntSet active = nums.numbers[actp.x][actp.y];
		
		for (int i : active)
		{
			if (activeIsSmaller ? passive.hasLarger(i) : passive.hasSmaller(i)) newActive.add(i);
		}
		
		if (newActive.size() == 0) return LogicStatus.CONTRADICTION;
		
		if (newActive.size() != active.size())
		{
			nums.numbers[actp.x][actp.y] = newActive;
			return LogicStatus.LOGICED;
		}
		return LogicStatus.STYMIED;		
	}
	
	
	
	public LogicStatus Apply(Numbers nums)
	{
		LogicStatus result = LogicStatus.STYMIED;
		
		LogicStatus stat = ApplyOneSide(nums, iAmSmaller, operantPoints.elementAt(0),
										operantPoints.elementAt(1));
		if (stat == LogicStatus.CONTRADICTION) return stat;
		if (stat == LogicStatus.LOGICED) result = stat;
		
		stat = ApplyOneSide(nums, !iAmSmaller, operantPoints.elementAt(1),
							operantPoints.elementAt(0));
		if (stat == LogicStatus.CONTRADICTION) return stat;
		if (stat == LogicStatus.LOGICED) result = stat;
	
		return result;
	}
	
}

		
		