import java.io.*;

public class BoardWriter
{
	public static void Write(Board b,String filename)
	{
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(filename);
		}
		catch(Exception ex)
		{
			throw new RuntimeException("some problem with board write!");
		}

		writer.println(b.width + " " + b.height);
		for (int y = 0 ; y < b.height ; ++y)
		{
			for (int x = 0 ; x < b.width ; ++x)
			{
				if (b.cellColors[x][y] == DotColor.NONE) continue;
				String color = b.cellColors[x][y] == DotColor.BLACK ? "B " : "W ";
				int loc = b.dotLoc[x][y];
				switch(loc)
				{
					case Circle.NONE:
					case Circle.N:
					case Circle.NW:
					case Circle.NE:
					case Circle.W:
					case Circle.SW:
						break;
					case Circle.CENTER:
						writer.println(color + " " + x + " " + y);
						break;
					case Circle.E:
						writer.println(color + " " + (x+0.5) + " " + y);
						break;
					case Circle.S:
						writer.println(color + " " + x + " " + (y+0.5));
						break;
					case Circle.SE:
						writer.println(color + " " + (x+0.5) + " " + (y+0.5));
						break;
				}
			}
		}
		writer.close();
	}
}