public class EdgeCoordinate extends Coordinate
{
	public Direction direction;
	public EdgeCoordinate() { super(); direction = Direction.UNKNOWN; }
	public EdgeCoordinate(int x,int y,Direction dir) { super(x,y); direction = dir; }
	
	@Override public boolean equals(Object rhs)
	{
		if ( ! (rhs instanceof EdgeCoordinate) ) { return false; }
		EdgeCoordinate crhs = (EdgeCoordinate) rhs;
		if (!super.equals(crhs)) return false;
		return crhs.direction == direction;
	}
	
	@Override public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + direction.ordinal();
		return result;
	}
	
	public String toString()
	{
		return "(" + x + "," + y + "," + (direction == Direction.HORIZONTAL ? "H" : "V") + ")";
	}
	
}
