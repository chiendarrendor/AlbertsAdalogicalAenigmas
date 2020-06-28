import java.awt.image.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Ada16
{
	static Board theBoard = null;
	static Vector<Board> boards = null;


	

	

	public static void main(String[] args)
	{
		if (args.length == 0) 
		{
			System.out.println("Bad Command Line");;
			System.exit(1);
		}
		theBoard = new Board(args[0]);
		Solver s = new Solver();
		s.Solve(theBoard);

		if (s.GetSolutions().size() == 0)
		{
			System.out.println("no solutions");
			System.exit(1);
		}
		BoardShower bs = new BoardShower(s.GetSolutions());



						
		
		
	}
}
		
		