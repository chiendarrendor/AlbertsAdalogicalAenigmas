package grid.spring;

import java.awt.Point;
import java.awt.*;
import java.util.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class Circle
{
	int cx;
	int cy;
	int r;
	
	public static final int NONE = 0x00;
	public static final int NNE = 0x01;
	public static final int SSE = 0x02;
	public static final int NNW = 0x04;
	public static final int SSW = 0x08;
	public static final int ENE = 0x10;
	public static final int ESE = 0x20;
	public static final int WNW = 0x40;
	public static final int WSW = 0x80;
	public static final int CENTER = 0x100;
	public static final int N = NNW + NNE + ENE + WNW;
	public static final int S = SSE + SSW + ESE + WSW;
	public static final int E = NNE + SSE + ENE + ESE;
	public static final int W = NNW + SSW + WNW + WSW;
	public static final int NW = NNW + WNW;
	public static final int SW = SSW + WSW;
	public static final int NE = NNE + ENE;
	public static final int SE = SSE + ESE;
	public static final int ALL = N + S;
	
	
	// this will return a set of points based soley on r
	// i.e. center shifted to 0,0
	public Vector<Point> CalculateOneOctant()
	{
		Vector<Point> result = new Vector<Point>();
		int x,y,dx,dy,rerr;
		x = r;
		y = 0;
		dx = 1 - 2*r;
		dy = 1;
		rerr = 0;
		while(x>=y)
		{
			result.add(new Point(x,y));
			++y;
			rerr += dy;
			dy += 2;
			if (2*rerr + dx > 0)
			{
				--x;
				rerr += dx;
				dx += 2;
			}
		}
		return result;
	}
	
	public Circle(int cx,int cy,int r)
	{
		this.cx = cx;
		this.cy = cy;
		this.r = r;
	}
	
	public Vector<Point> GetArcPoints(int octants)
	{
		Vector<Point> octant = CalculateOneOctant();
		Vector<Point> result = new Vector<Point>();
		
		for (Point p : octant)
		{
			if ((octants & ESE) != 0) result.add(new Point(cx+p.x,cy+p.y));   // ESE
			if ((octants & WSW) != 0) result.add(new Point(cx-p.x,cy+p.y)); // WSW
			if ((octants & WNW) != 0) result.add(new Point(cx-p.x,cy-p.y)); // WNW
			if ((octants & ENE) != 0) result.add(new Point(cx+p.x,cy-p.y)); // ENE
			if ((octants & SSE) != 0) result.add(new Point(cx+p.y,cy+p.x));  // SSE
			if ((octants & SSW) != 0) result.add(new Point(cx-p.y,cy+p.x)); // SSW
			if ((octants & NNW) != 0) result.add(new Point(cx-p.y,cy-p.x)); // NNW
			if ((octants & NNE) != 0) result.add(new Point(cx+p.y,cy-p.x)); // NNE
		}
		return result;
	}
}
	
		
