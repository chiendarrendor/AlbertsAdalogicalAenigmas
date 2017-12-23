
import java.util.*;

public abstract class Constraint
{
	protected Vector<String>  numids = new Vector<String>();
	public List<String> GetRequestedNumbers() { return numids; }
	
	// this function takes a List<Integer>, and can assume that it 
	// is of identical cardinality as numids, and will contain
	// the current values of the requested numbers on the grid
	public abstract boolean isFulfilled(int... numvalues);
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().toString() + ":");
		for (String s : numids) { sb.append(" " + s); }
		return sb.toString();
	}
	
	public boolean Active(Set<String> currentnumids,String extranumid)
	{
		for (String id : numids)
		{
			if (!currentnumids.contains(id) && !extranumid.equals(id)) return false;
		}
		return true;
	}
	
	public boolean Active(Set<String> currentnumids)
	{
		for (String id : numids) { if (!currentnumids.contains(id)) return false; }
		return true;
	}
	
	
}
