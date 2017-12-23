
import java.util.*;

public class Constraints
{
	Board theBoard;
	Numbers theNums;
	Map<String,Vector<Constraint>> byNumber = new HashMap<String,Vector<Constraint>>();
	Vector<Constraint> allCons = new Vector<Constraint>();

	private void AddConstraint(Constraint c)
	{
		allCons.add(c);
		for (String num : c.GetRequestedNumbers())
		{
			if (!theNums.numberMap.containsKey(num)) throw new RuntimeException("Unknown Number " + num);
		
			if (!byNumber.containsKey(num)) byNumber.put(num,new Vector<Constraint>());
			byNumber.get(num).add(c);
		}
	}
	
	private void AddGridConstraints(Board b,Numbers nums)
	{
		for (int x = 0 ; x < b.width ; ++x)
		{
			for (int y = 0 ; y < b.height ; ++y)
			{
				if (nums.numbergrid[x][y] == null) continue;
				if (nums.numbergrid[x][y].size() == 1) continue;
				if (nums.numbergrid[x][y].size() > 2) throw new RuntimeException("This shouldn't happen!");
				
				Number n1 = nums.numbergrid[x][y].get(0);
				Number n2 = nums.numbergrid[x][y].get(1);
				
				Constraint c = new GridConstraint(n1,n2);
				AddConstraint(c);
			}
		}
	}

	
	public Constraints(Board b,Numbers nums,Iterable<Constraint> externals)
	{
		theBoard = b;
		theNums = nums;
		AddGridConstraints(b,nums);
		for (Constraint c : externals) AddConstraint(c);
	}
	
	public void show()
	{
		for (Constraint c : allCons) System.out.println(c);
	}
	
}