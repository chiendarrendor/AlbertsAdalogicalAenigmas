
import java.awt.Dimension;
import java.util.Vector;

public class RectangleList
{
	public static class DimensionVector extends Vector<Dimension>
	{
	}

	private DimensionVector rectangles[];
	int width;
	int height;

	public RectangleList(int width,int height)
	{
		this.width = width;
		this.height = height;
		rectangles = new DimensionVector[width*height+1];
		
		for (int x = 1 ; x <= width ; ++x)
		{
			for (int y = 1 ; y <= height ; ++y)
			{
				int rectsize = x * y;
				if (rectangles[rectsize] == null) rectangles[rectsize] = new DimensionVector();
				rectangles[rectsize].add(new Dimension(x,y));
			}
		}
	}
	
	public DimensionVector rectanglesOfSize(int x)
	{
		return rectangles[x];
	}
	
	public void printAllRectangles()
	{
		for (int i = 1 ; i <= width*height ; ++i)
		{
			System.out.print(i + ":");
			if (rectangles[i] == null)
			{
				System.out.println(" --NONE--");
				continue;
			}
			for (Dimension d : rectangles[i]) { System.out.print(" " + d.toString()); }
			System.out.println("");
		}
	}
	
	
}
