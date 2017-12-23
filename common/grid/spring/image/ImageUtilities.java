package grid.spring.image;

import grid.graph.DotColor;
import grid.spring.Circle;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;
import java.awt.*;


public class ImageUtilities
{
	private static final int MINPIXELS = 20;
	private static final double MAXAVERAGE = 180;
	private static final int MINSEPARATION = 10;
	private static final int MINREGIONS = 12;
	private static final double EDGELETMIN = 0.2;
	private static final double WEIGHTFACTOR = 1.0;
	private static final double REGIONSFACTOR = 10.0;
	private static final double SIZEFACTOR = 1.0;

	public static BufferedImage LoadImage(String filename)
	{
		BufferedImage result = null;
		try
		{
			result = ImageIO.read(new File(filename));
		}
		catch (Exception ex)
		{
			System.out.println("Exception caught: " + ex);
		}
		return result;
	}
	
	public static void SaveImage(BufferedImage img, String filename)
	{
		try
		{
			ImageIO.write(img,"png",new File(filename));
		}
		catch (Exception ex)
		{
			System.out.println("Exception caught: " + ex);
		}
	}

	// returns a number between 0 and 255 for the blue channel of the given point
	// (this library assumes that all colors are grey and opaque, so that one color channel is enough)
	public static int Signal(BufferedImage img,int x, int y)
	{
		Color col = new Color(img.getRGB(x,y));
		return col.getBlue();
	}

	// returns the pure white rectangle inside of which is all the non-white of the image
	public static Rectangle GetBounding(BufferedImage img)
	{
		Rectangle result = new Rectangle(0,0,-1,-1);
		for (int x = 0 ; x < img.getWidth() ; ++x)
		{
			for (int y = 0 ; y < img.getHeight() ; ++ y)
			{
				if (Signal(img,x,y) == 255) continue;
				result.add(x,y);
			}
		}
		return result;
	}
	
	// for a given line (from start to end inclusive, in the direction of delta)
	// provide a score as a four-double array:
	// the score is calculated from first non-white cell to last non-white cell
	// 0) 255 - average signal strength 
	// 1) number of regions (white-separated areas)
	// 2) size of largest region
	// 3) the number of cells in the operant region (first non-white to last non-white)
	private static double[] invalid = { 0,0,0,0 };
	public static double[] getLineStructureScore(BufferedImage img,Point start,Point end,Point delta)
	{
		// find first non-white space
		Point first = new Point(start);
		while(!first.equals(end))
		{
			if (Signal(img,first.x,first.y) != 255) break;
			first.translate(delta.x,delta.y);
		}
		if (first.equals(end)) return invalid;
		Point last = new Point(end);
		while(!last.equals(first))
		{
			if (Signal(img,last.x,last.y) != 255) break;
			last.translate(-delta.x,-delta.y);
		}
		
		int sum = 0;
		int count = 0;
		int numregions = 1;
		int largestregionsize = 0;
		int curregionsize = 1;
		boolean isBlack = true;
		while(true)
		{
			++count;
			int s = Signal(img,first.x,first.y);
			sum += s;
			
			if (s < 255)
			{
				++curregionsize;
				if (!isBlack) { isBlack = true ; ++numregions; }
			}
			else
			{
				if (isBlack) 
				{ 
					isBlack = false; 
					if (curregionsize > largestregionsize) largestregionsize = curregionsize;
					curregionsize = 0;
				}
			}
				
			if (first.equals(last)) break;
			first.translate(delta.x,delta.y);
		}
		
		if (isBlack) { if (curregionsize > largestregionsize) largestregionsize = curregionsize; }
		
		
		double v = (double)sum / (double)count;
		
		if (count < MINPIXELS) return invalid;
		
		double[] result = { 255-v , numregions, largestregionsize, count };
		return result;
	}
	
	// interface for scoring lines.   any line where Score() applied to getLineStructureScore
	// is greater than Threshhold() will be considered a line
	public interface LineScore
	{
		double Score(double[] components);
		double Threshhold();
	}
	
	// private class for iterating over 
	private static class PointIterator
	{
		int curv;
		int terminalv;
		Point delta;
		boolean isVertical;
		Rectangle bounding;
		
		public PointIterator(BufferedImage img, boolean isVertical)
		{
			this.isVertical = isVertical;
			bounding = GetBounding(img);
			curv = isVertical ? bounding.x : bounding.y;
			terminalv = curv + (isVertical ? bounding.width : bounding.height);
			delta = isVertical ? new Point(0,1) : new Point(1,0);
		}
			
		Point getLowPoint()
		{
			return isVertical ? new Point(curv,bounding.y) : new Point(bounding.x,curv);
		}
		
		Point getHighPoint()
		{
			return isVertical ? new Point(curv,bounding.y+bounding.height) : new Point(bounding.x+bounding.width,curv);
		}
		
		Point getDelta() { return delta; }
		void Increment() { ++curv; }
		boolean isDone() { return curv > terminalv; }
		int getV() { return curv; }
	}
	
	// given a scorer and an image, will find all lines, either horizontal or vertical
	// where the scorer indicates the line is above threshhold
	public static Vector<Integer> GetGridLines(BufferedImage img,LineScore scorer,boolean isVertical)
	{
		Vector<Integer> result = new Vector<Integer>();
		
		int firstin = -1;
		int bestin = -1;
		double bestval = 0;
		
		for (PointIterator pi = new PointIterator(img,isVertical)  ; !pi.isDone()  ; pi.Increment())
		{
			double[] lines = getLineStructureScore(img,pi.getLowPoint(),pi.getHighPoint(),pi.getDelta());
			double score = lines != null ? scorer.Score(lines) : 0.0;
			
			if (score < scorer.Threshhold())
			{
				if (firstin != -1) // if we get here, we are closing a collection
				{
					result.add(bestin);
					firstin = -1;
					bestin = -1;
					bestval = 0;
				}
			}
			else
			{
				if (firstin == -1)
				{
					firstin = pi.getV();
					bestin = pi.getV();
					bestval = score;
				}
				else
				{
					if (score > bestval) bestin = pi.getV();
				}
			}
		}
		if (firstin != -1) result.add(bestin);
		return result;
	}
	
	public static void ShowScores(BufferedImage img,boolean isVertical)
	{
		for (PointIterator pi = new PointIterator(img,isVertical)  ; !pi.isDone()  ; pi.Increment())
		{
			double[] lines = getLineStructureScore(img,pi.getLowPoint(),pi.getHighPoint(),pi.getDelta());
			System.out.print(pi.getV());
			for (double d : lines) { System.out.print("," + d); }
			System.out.println("");
		}
	}

	public static double averageSignal(BufferedImage img, java.util.List<Point> points)
	{
		double signal = 0;
		double count = 0;
		for (Point p : points) { ++count ; signal += Signal(img,p.x,p.y); }
		return signal/count;
	}

	
	public static int[] GetSignals(BufferedImage img, Point center, int octants, int distance,int inset)
	{
		int[] result = new int[distance-inset];
		for (int i = 0 ; i < distance-inset ; ++i)
		{	
			Circle c = new Circle(center.x,center.y,i+inset);
			result[i] = (int)averageSignal(img,c.GetArcPoints(octants));
		}
		
		return result;
	}
	
	public static DotColor GetDotColor(BufferedImage img, Point center, int octants, int distance)
	{
		int[] scancircle = GetSignals(img,center,octants,distance,4);
		int whitecount=0;
		int blackcount=0;
		for (int i : scancircle) if (i < 125) ++blackcount; else ++whitecount; 
		if (blackcount == 0) return DotColor.NONE;
		if (blackcount > distance/2) return DotColor.BLACK;
		return DotColor.WHITE;
	}
	
	
	
	
	
	
	
	
		
	public static boolean IsEdgelet(BufferedImage img, Point start,Point end,Point delta,boolean info)
	{
		int count = 0;
		int off = 0;
		
		for (Point p = new Point(start) ; !p.equals(end) ; p.translate(delta.x,delta.y))
		{
			++count;
			if (Signal(img,p.x,p.y) < 255) ++off;
		}
		if (info) System.out.println("off " + off + " count " + count + " average: " + (double)off/count);
		return (double)off/(double)count > EDGELETMIN;
	}
	
}
				