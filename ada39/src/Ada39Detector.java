import grid.spring.image.ImageUtilities;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;
import java.awt.*;
import java.util.regex.*;

// will read the 'slant' solution file for which areas have slashes and which have backslashes
// doing this cheap and dirty, not using the grid dectector because the dark grey lines on light
// grey are going to be hard to detect.

public class Ada39Detector
{
	private static class GridConfig {
		String filename;
		int ulx;
		int uly;
		int dx;
		int dy;
		int nx;
		int ny;
		int coff;
		public GridConfig(String filename,int ulx,int uly,int dx,int dy,int nx,int ny, int coff) {
			this.filename = filename;
			this.ulx = ulx; this.uly = uly;
			this.dx = dx; this.dy = dy;
			this.nx = nx; this.ny = ny;
			this.coff = coff;
		}
	}

	private static GridConfig adalogical39 = new GridConfig(
			"Ada39Solution.png",
			33,41,
			68,68,
			22,13,10);

	private static GridConfig addendum35 = new GridConfig(
			"Addendum35.png",
		26,24,
		74-26,72-24,
			22,13,
			10
	);


	public static void main(String[] args)
	{
		GridConfig gc = addendum35;
		BufferedImage img = ImageUtilities.LoadImage(gc.filename);



		for (int y = 0 ; y < gc.ny - 1 ; ++y)
		{
			for (int x = 0 ; x < gc.nx-1 ; ++x)
			{
				int culx = gc.ulx + gc.dx * x;
				int culy = gc.uly + gc.dy * y;
				int cx = culx + gc.dx/2;
				int cy = culy + gc.dy/2;

				//System.out.println("checking pixel " + (cx-gc.coff) + "," + (cy-gc.coff) + " and " +
				//		(cx+gc.coff) + "," + (cy+gc.coff));
				//System.out.println("Color: " + ImageUtilities.Signal(img, cx-gc.coff,cy-gc.coff));
				//System.out.println("Color2: " + ImageUtilities.Signal(img, cx+gc.coff,cy+gc.coff));


				if (ImageUtilities.Signal(img,cx-gc.coff,cy-gc.coff) == 0 &&
					ImageUtilities.Signal(img,cx+gc.coff,cy+gc.coff) == 0)
				{
					System.out.print("\\");
				}
				else
				{
					System.out.print("/");
				}
			}
			System.out.println("");
		}

	}
}		
		
		