

public class ForEachCell 
{
	public interface CellOp
	{
		boolean op(int x,int y);
	}
	
	public static void op(int width, int height, CellOp co)
	{
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				if (!co.op(x,y)) return;
			}
		}
	}
}
		