
import java.awt.image.*;
import java.util.*;
import java.awt.*;
import java.util.regex.*;

public class Ada30Scanner
{
	public static class MyLineScorer implements ImageUtilities.LineScore
	{
		public double Score(double[] components) 
		{ 
			if (components[3] < 80) return 0;
			if (components[2] > 200) return 500; // special case for the 'Floor' image with a solid outline.
			return components[0] * components[1] * components[1] / components[3]; 
		}
		public double Threshhold() { return 100; }
	}


	public static void MakeRuledImage(String imgname)
	{
		BufferedImage img = ImageUtilities.LoadImage(imgname);
		Vector<Integer> hlines = ImageUtilities.GetGridLines(img,new MyLineScorer(),false);
		Vector<Integer> vlines = ImageUtilities.GetGridLines(img,new MyLineScorer(),true);
		Rectangle bounding = ImageUtilities.GetBounding(img);
		int bulx = bounding.x;
		int buly = bounding.y;
		int blrx = bounding.x + bounding.width;
		int blry = bounding.y + bounding.height;
		
		for (int x = bulx ; x <= blrx ; ++x)
		{
			for (int y : hlines)
			{
				img.setRGB(x,y,Color.green.getRGB());
			}
		}
		for (int y = buly ; y < blry ; ++y)
		{
			for (int x : vlines)
			{
				img.setRGB(x,y,Color.green.getRGB());
			}
		}
		
		ImageUtilities.SaveImage(img,"output.png");
	}
	
	public static void CellDotDetector(BufferedImage img, int cellx, int celly, int ulx, int uly, int lrx , int lry , Board b, boolean isFloor)
	{
		int dist = (lrx-ulx)/4 +3;
		DotColor dc;
		
		if (!isFloor || !b.isEdge(cellx,celly,Circle.NW))
		{
			dc = ImageUtilities.GetDotColor(img,new Point(ulx,uly),Circle.SE,dist);
			if (dc != DotColor.NONE) b.AddDot(cellx,celly,dc,Circle.NW);
		}
		
		if (!isFloor || !b.isEdge(cellx,celly,Circle.N))
		{		
			dc = ImageUtilities.GetDotColor(img,new Point((ulx+lrx)/2,uly),Circle.S,dist);
			if (dc != DotColor.NONE) b.AddDot(cellx,celly,dc,Circle.N);
		}
		
		if (!isFloor || !b.isEdge(cellx,celly,Circle.NE))
		{		
			dc = ImageUtilities.GetDotColor(img,new Point(lrx,uly),Circle.SW,dist);
			if (dc != DotColor.NONE) b.AddDot(cellx,celly,dc,Circle.NE);
		}
		
		if (!isFloor || !b.isEdge(cellx,celly,Circle.W))
		{		
			dc = ImageUtilities.GetDotColor(img,new Point(ulx,(uly+lry)/2),Circle.E,dist);
			if (dc != DotColor.NONE) b.AddDot(cellx,celly,dc,Circle.W);
		}
		
	
		dc = ImageUtilities.GetDotColor(img,new Point((ulx+lrx)/2,(uly+lry)/2),Circle.ALL,dist);
		if (dc != DotColor.NONE) b.AddDot(cellx,celly,dc,Circle.CENTER);
		
		
		if (!isFloor || !b.isEdge(cellx,celly,Circle.E))
		{		
			dc = ImageUtilities.GetDotColor(img,new Point(lrx,(uly+lry)/2),Circle.W,dist);
			if (dc != DotColor.NONE) b.AddDot(cellx,celly,dc,Circle.E);
		}
		
		if (!isFloor || !b.isEdge(cellx,celly,Circle.SW))
		{		
			dc = ImageUtilities.GetDotColor(img,new Point(ulx,lry),Circle.NE,dist);
			if (dc != DotColor.NONE) b.AddDot(cellx,celly,dc,Circle.SW);
		}
			
		if (!isFloor || !b.isEdge(cellx,celly,Circle.S))
		{		
			dc = ImageUtilities.GetDotColor(img,new Point((ulx+lrx)/2,lry),Circle.N,dist);
			if (dc != DotColor.NONE) b.AddDot(cellx,celly,dc,Circle.S);
		}
		
		if (!isFloor || !b.isEdge(cellx,celly,Circle.SE))
		{		
			dc = ImageUtilities.GetDotColor(img,new Point(lrx,lry),Circle.NW,dist);
			if (dc != DotColor.NONE) b.AddDot(cellx,celly,dc,Circle.SE);
		}
	}
		
		
		
	
	
	
	

	public static void main(String[] args)
	{
		if (args.length != 2) { System.out.println("Bad Command Line"); System.exit(1); }
		MakeRuledImage(args[0]);
		BufferedImage img = ImageUtilities.LoadImage(args[0]);

		Pattern p = Pattern.compile("Tile(.*)\\.png");
		Matcher m = p.matcher(args[0]);
		boolean isFloor = true;
		int tileid = 16;
		if (m.find()) {
			isFloor = false;
			tileid = Integer.parseInt(m.group(1),16);
		}

		boolean edgeOnlyCells = false;
		boolean skipOuterEdges = true;
		
		
		Rectangle bounding = ImageUtilities.GetBounding(img);
		int bulx = bounding.x;
		int buly = bounding.y;
		int blrx = bounding.x + bounding.width;
		int blry = bounding.y + bounding.height;
				
		Vector<Integer> hlines = ImageUtilities.GetGridLines(img,new MyLineScorer(),false);
		Vector<Integer> vlines = ImageUtilities.GetGridLines(img,new MyLineScorer(),true);
				
		int width = vlines.size() - 1;
		int height = hlines.size() - 1;
		System.out.println("Width: " + width + " Height: " + height);
		Board b = new Board(width,height);
		b.setCellId(tileid);
		
		for (int x = 0 ; x < width ; ++x)
		{
			int lx = vlines.get(x);
			int rx = vlines.get(x+1);
		
			for (int y = 0 ; y < height ; ++y)
			{
				if (edgeOnlyCells && y != 0 && y != height-1 && x != 0 && x != width-1) continue;
				int uy = hlines.get(y);
				int dy = hlines.get(y+1);
				
				System.out.println("Doing Cell " + x + "," + y);


				boolean doInfo = false;
				
				boolean le = ImageUtilities.IsEdgelet(img,new Point(lx,uy),new Point(lx,dy),new Point(0,1),doInfo);
				boolean re = ImageUtilities.IsEdgelet(img,new Point(rx,uy),new Point(rx,dy),new Point(0,1),doInfo);
				boolean ue = ImageUtilities.IsEdgelet(img,new Point(lx,uy),new Point(rx,uy),new Point(1,0),doInfo);
				boolean de = ImageUtilities.IsEdgelet(img,new Point(lx,dy),new Point(rx,dy),new Point(1,0),doInfo);
				
				
				
				if (!le || !de || !ue || !re) continue;
				b.setCellOn(x,y);
				
				CellDotDetector(img,x,y,lx,uy,rx,dy,b, skipOuterEdges);
				
				for (int dx = lx+5 ; dx < rx-5 ; ++dx)
				{
					for (int dey = uy+5 ; dey < dy-5 ; ++dey)
					{
						img.setRGB(dx,dey,Color.red.getRGB());
					}
				}
			}
		}

		b.Show();
		b.Write(args[1]);
		
		ImageUtilities.SaveImage(img,"output2.png");

	}
}