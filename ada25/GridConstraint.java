
import java.util.*;
import java.awt.Point;

// we will create a GridConstraint for every pair of Numbers
// that overlap on the grid.  this constraints isFulfilled will return true
// iff the digit at the overlapping location is the same for both numbers.


public class GridConstraint extends Constraint
{
	int digit0;
	int digit1;
	
   // this gets the nth digit of num as a base 10 number, where the 1's place is 0, the 10s place is 1, etc.
   public static int getDigit(int num,int digit)
   {
		   while(digit > 0) { num /= 10 ; --digit; }
		   return num % 10;
   }

	public GridConstraint(Number n0, Number n1)
	{
		numids.add(n0.sigil);
		numids.add(n1.sigil);
		// 1) find the overlap:
		for (int p0i = 0 ; p0i < n0.points.size() ; ++p0i)
		{
			Point p0 = n0.points.elementAt(p0i);
			for (int p1i = 0 ; p1i < n1.points.size() ; ++p1i)
			{
				Point p1 = n1.points.elementAt(p1i);
				if (!p0.equals(p1)) continue;
				// if we're here we've found the unique overlap.
				digit0 = n0.points.size() - p0i - 1;
				digit1 = n1.points.size() - p1i - 1;
				return;
			}
		}
	}
	
	public boolean isFulfilled(int... numvalues)
	{
		return getDigit(numvalues[0],digit0) == getDigit(numvalues[1],digit1);
	}
}

	