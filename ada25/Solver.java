
import java.util.*;

public class Solver
{
	Numbers theNumbers;
	Constraints theConstraints;
	

	Set<Constraint> activeConstraints = new HashSet<Constraint>();
	Set<Constraint> unusedConstraints = new HashSet<Constraint>();
	Set<Number> usedNumbers = new HashSet<Number>();
	Set<String> usedNames = new HashSet<String>();
	Set<Number> unusedNumbers = new HashSet<Number>();
	
	Vector<Literals> solutions = new Vector<Literals>();
	
	
	private Vector<Literals> InitializeQueue()
	{
		Vector<Literals> queue = new Vector<Literals>();
		Number largest = null;
		int largestcount = 0;
		
		for (Number n : unusedNumbers)
		{
			if (theConstraints.byNumber.get(n.sigil).size() > largestcount)
			{
				largestcount = theConstraints.byNumber.get(n.sigil).size();
				largest = n;
			}
		}
		
		// temp:
		largest = theNumbers.numberMap.get("10A");
		
		
		unusedNumbers.remove(largest);
		usedNumbers.add(largest);
		usedNames.add(largest.sigil);
		System.out.println("Initial: " + largest.sigil);
		for (Constraint c : theConstraints.byNumber.get(largest.sigil)) { System.out.println("  -- " + c); }
		
		for (int i = largest.minimum ; i <= largest.maximum ; ++i)
		{
			Literals lit = new Literals();
			lit.put(largest.sigil,i);
			queue.add(lit);
		}
		return queue;
	}
	
	private Number FindBestNumber()
	{
		// clear activeConstraints
		activeConstraints.clear();
		// choose the best number
		Set<Number> nextNumbers = new HashSet<Number>();
		Set<Constraint> nextConstraints = new HashSet<Constraint>();
		
		for (Number n : usedNumbers)
		{
			for (Constraint c : theConstraints.byNumber.get(n.sigil))
			{
				if (!unusedConstraints.contains(c)) continue;
				nextConstraints.add(c);
				for (String nums : c.GetRequestedNumbers())
				{
					Number nextn = theNumbers.numberMap.get(nums);
					if (usedNumbers.contains(nextn)) continue;
					nextNumbers.add(nextn);
				}
			}
		}
		int maxcons = 0;
		Number bestN = null;
		for (Number n : nextNumbers)
		{
			int concount = 0;
			for (Constraint c : nextConstraints) if (c.Active(usedNames,n.sigil)) ++concount;
			if (concount > maxcons)
			{
				bestN = n;
				maxcons = concount;
			}
		}
/*		
		System.out.println("Size of next: " + nextNumbers.size());
		
		System.out.print("used:" );
		for (Number n : usedNumbers) { System.out.print(" " + n.sigil); }
		System.out.println("");
		
		System.out.print("next:");
		for (Number n : nextNumbers) { System.out.print(" " + n.sigil); }
		System.out.println("");
*/		
		System.out.println("next: " + bestN.sigil);
		
		// remove it from unused
		// add it to used
		// add it to used names		
		unusedNumbers.remove(bestN);
		usedNumbers.add(bestN);
		usedNames.add(bestN.sigil);
		// move all Active constraints to activeConstraints.
		for (Constraint c : nextConstraints)
		{
			if (!c.Active(usedNames)) continue;
			unusedConstraints.remove(c);
			activeConstraints.add(c);
			System.out.println("  " + c);
		}
		return bestN;
	}
	

	
	Solver(Numbers nums,Constraints cons)
	{	
		theNumbers = nums;
		theConstraints = cons;
		for (Number n : nums.numberMap.values()) { unusedNumbers.add(n); }
		for (Constraint c : cons.allCons) { unusedConstraints.add(c); }
		Vector<Literals> queue = InitializeQueue();
		
		while(unusedNumbers.size() > 0)
		{
			// this will choose one number, move it from unused to used,
			// clear activeConstraints, and move all new feasible constraints from
			// unused to active.
			Number bestnum = FindBestNumber();
			
			Vector<Literals> newqueue = new Vector<Literals>();
			for (Literals lit : queue)
			{
				for (int i = bestnum.minimum ; i <= bestnum.maximum ; ++i)
				{
					Literals newlit = new Literals(lit);
					newlit.put(bestnum.sigil,i);
					if (!newlit.ApplyConstraints(activeConstraints)) continue;
					newqueue.add(newlit);
				}
			}
			queue = newqueue;
			System.out.println("Queue.size: " + queue.size() + " solutions.size: " + solutions.size());
		}
		solutions = queue;
		
			
			
			
		
	}
}
		