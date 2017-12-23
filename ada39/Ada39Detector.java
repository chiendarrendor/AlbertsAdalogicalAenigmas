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

	public static void main(String[] args)
	{
		BufferedImage img = ImageUtilities.LoadImage("Ada39Solution.png");

		int ulx = 33;
		int uly = 41;
		int dx = 68;
		int dy = 68;
		int nx = 22;
		int ny = 13;
		
		int coff = 10;
		
		for (int y = 0 ; y < ny - 1 ; ++y)
		{
			for (int x = 0 ; x < nx-1 ; ++x)
			{
				int culx = ulx + dx * x;
				int culy = uly + dy * y;
				int cx = culx + dx/2;
				int cy = culy + dy/2;
				
				if (ImageUtilities.Signal(img,cx-coff,cy-coff) == 0 &&
					ImageUtilities.Signal(img,cx+coff,cy+coff) == 0)
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
		
		