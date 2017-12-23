import java.util.*;
import java.awt.Point;

public class Ada25
{

	public static void main(String[] args)
	{
		GridFileReader gfr = new GridFileReader("ada25.txt");	
		Board b = new Board(gfr);
		Numbers nums = new Numbers(b);
		Constraints cons = new Constraints(b,nums,Arrays.asList(Ada25Clues.clues));
		
		System.out.println("num count: " + nums.numberMap.size() + " constraint count: " + cons.allCons.size());
		Solver solv = new Solver(nums,cons);
		
		int[][] finalb = new int[b.width][b.height];
		for (int x = 0 ; x < b.width ; ++x)
		{
			for (int y = 0 ; y < b.height ; ++y)
			{
				finalb[x][y] = -1;
			}
		}
		
		Literals solution = solv.solutions.firstElement();
		for (Number n : nums.numberMap.values())
		{
			int v = solution.get(n.sigil);
			for (int i = 0 ; i < n.points.size() ; ++i)
			{
				Point p = n.points.get(i);
				finalb[p.x][p.y] = GridConstraint.getDigit(v,n.points.size() - i - 1);
			}
		}
		
		for (int y = 0 ; y < b.height ; ++y)
		{
			for (int x = 0 ; x < b.width ; ++x)
			{
				if (finalb[x][y] == -1) System.out.print("#");
				else System.out.print(finalb[x][y]);
			}
			System.out.println("");
		}
		
		for (int row = 0 ; row < b.height ; ++row)
		{
			char rawc = b.verticalChars.charAt(row);
			int offset = 0;
			for (int col = 0 ; col < b.width ; ++col)
			{
				if (!b.isSpecial[col][row]) continue;
				offset += finalb[col][row];
			}
			System.out.print(LetterRotate.Rotate(rawc,offset));
		}
		
		for (int col = 0 ; col < b.width ; ++col)
		{
			char rawc = b.horizontalChars.charAt(col);
			int offset = 0;
			for (int row = 0 ; row < b.height ; ++row)
			{
				if (!b.isSpecial[col][row]) continue;
				offset += finalb[col][row];
			}
			System.out.print(LetterRotate.Rotate(rawc,offset));
		}
		System.out.println("");
		
		
		
		
		
	}
}

		