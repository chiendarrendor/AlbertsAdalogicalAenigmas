
import java.awt.Point;
import java.util.*;

public class Ada4
{
	public static void main(String[] args)
	{
		if (args.length != 1) throw new RuntimeException("Bad command line");
		Board b = new Board(args[0]);
		Solver s = new Solver(b);

		s.Solve(b);

		Board sb = s.GetSolutions().get(0);
		sb.show();

		Map<Character,StringBuffer> markstrings = new HashMap<>();
		for(int x = 0 ; x < sb.getWidth() ; ++x)
		{
			for (int y = 0 ; y < sb.getHeight() ; ++y)
			{
				char mark = sb.getMark(x,y);
				if (mark == '.') continue;
				if (!markstrings.containsKey(mark))
				{
					markstrings.put(mark,new StringBuffer());
				}
				markstrings.get(mark).append(sb.charAt(x,y));
			}
		}
		for(char mark : markstrings.keySet())
		{
			System.out.println("MARK " + mark + ": " + markstrings.get(mark));
		}





	}
}
			
			
			
		
		