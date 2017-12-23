
import java.util.*;

public class Literals extends HashMap<String,Integer>
{
	public Literals() {}
	
	public Literals(Literals right)
	{
		for (String s : right.keySet()) { put(s,right.get(s)); }
	}
	
	public boolean ApplyConstraints(Iterable<Constraint> cons)
	{
		for (Constraint c : cons)
		{
			int[] args = new int[c.GetRequestedNumbers().size()];
			for (int i = 0 ; i < c.GetRequestedNumbers().size() ; ++i)
			{
				String numid = c.GetRequestedNumbers().get(i);
				if (!containsKey(numid)) throw new RuntimeException("Literal missing number " + numid);
				args[i] = get(numid);
			}
			if (!c.isFulfilled(args)) return false;
		}
		return true;
	}
	
	
}