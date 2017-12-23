import java.awt.Point;
import java.awt.image.*;
import java.util.*;
import java.awt.*;

public class DeltaClue extends ClueSet.Clue
{
	int delta; 
	char dir;
	
	public DeltaClue(Point curloc, int delta,char dir,Board b,ClueSet cs)
	{
		Point d = cs.getDeltaOfDir(dir);
		if (d == null) throw new RuntimeException("Invalid delta clue direction " + dir);
		this.dir = dir;
		Point other = new Point(curloc.x+d.x,curloc.y+d.y);
		if (!b.onBoard(other)) throw new RuntimeException("dir is off board!");
		
		cs.applyPointToClue(this,curloc);
		cs.applyPointToClue(this,other);
		
		this.delta = delta;
	}
	
	static Map<Integer,BufferedImage> images = new HashMap<Integer,BufferedImage>();
	
	public BufferedImage getImage()
	{
		if (images.containsKey(delta)) return images.get(delta);
		BufferedImage bi = new BufferedImage(cluewidth,clueheight,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		Color trans = new Color(0,0,0,0);
		g.setColor(trans);
		g.fillRect(0,0,cluewidth,clueheight);
		g.setColor(Color.white);
		g.fillOval(0,0,cluewidth,clueheight);
		g.setColor(Color.black);
		g.drawOval(0,0,cluewidth,clueheight);
		GridPanel.DrawStringInCell(g,Color.black,0,0,cluewidth,clueheight,""+delta);
	
		images.put(delta,bi);
		return bi;
	}
	
	public void AlterCellImage(Point p,BufferedImage bi)
	{
		int cx;
		int cy;
		boolean isMe = p.equals(operantPoints.elementAt(0));
		if      (isMe && dir == 'N') { cx = bi.getWidth()/2 ; cy = 0; }
		else if (isMe && dir == 'S') { cx = bi.getWidth()/2 ; cy = bi.getHeight(); }
		else if (isMe && dir == 'W') { cx = 0 ; cy = bi.getHeight()/2; }
		else if (isMe && dir == 'E') { cx = bi.getWidth(); cy = bi.getHeight()/2; }
		else if (!isMe && dir == 'S') { cx = bi.getWidth()/2 ; cy = 0; }
		else if (!isMe && dir == 'N') { cx = bi.getWidth()/2 ; cy = bi.getHeight(); }
		else if (!isMe && dir == 'E') { cx = 0 ; cy = bi.getHeight()/2; }
		else if (!isMe && dir == 'W') { cx = bi.getWidth(); cy = bi.getHeight()/2; }		
		else throw new RuntimeException("Bwah?");
	
		DrawImageOnCenter(new Point(cx,cy),(Graphics2D)bi.getGraphics(),getImage());
	}
	
	private LogicStatus ApplyOneSide(Numbers nums, Point actp, Point passp)
	{
		Numbers.IntSet newActive = new Numbers.IntSet();
		Numbers.IntSet passive = nums.numbers[passp.x][passp.y];
		Numbers.IntSet active = nums.numbers[actp.x][actp.y];
		
		for (int i : active)
		{
			if (passive.contains(i+delta) || passive.contains(i-delta)) newActive.add(i);
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
		
		LogicStatus stat = ApplyOneSide(nums, operantPoints.elementAt(0),
										operantPoints.elementAt(1));
		if (stat == LogicStatus.CONTRADICTION) return stat;
		if (stat == LogicStatus.LOGICED) result = stat;
		
		stat = ApplyOneSide(nums, operantPoints.elementAt(1),
							operantPoints.elementAt(0));
		if (stat == LogicStatus.CONTRADICTION) return stat;
		if (stat == LogicStatus.LOGICED) result = stat;
	
		return result;
	}	
	
	
	
}

		
		