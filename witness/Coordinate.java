public class Coordinate
{
	public int x;
	public int y;
	public Coordinate(int x,int y) { this.x = x ; this.y = y; }
	public Coordinate() { this.x = -1 ; this.y = -1; }
	
	@Override public boolean equals(Object rhs)
	{
		if ( ! (rhs instanceof Coordinate) ) { return false; }
		Coordinate crhs = (Coordinate) rhs;
		return crhs.x == x && crhs.y == y;
	}
	
	@Override public int hashCode()
	{
		int result = 17;
		result = 31 * result + x;
		result = 31 * result + y;
		return result;
	}
	
	public String toString()
	{
		return "(" + x + "," + y + ")";
	}
		
}
