

public abstract class ClueConstraint extends Constraint
{
	public abstract void nums();
	protected void go(String ... sigils) { for (String s : sigils) { numids.add(s); } }

	public ClueConstraint() { nums(); }
		
	// for an equation of the form  a = b / c,
	// returns true iff b/c is exactly a.		
	protected boolean dividesExactly(int a,int b,int c)
	{
		return a * c == b;
	}
}
