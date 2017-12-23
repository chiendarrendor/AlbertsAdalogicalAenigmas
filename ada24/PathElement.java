
import java.awt.Point;

public class PathElement
{
	Point p;
	CellState state;
	boolean isHypothetical;
	public PathElement(Point p,CellState cs,boolean hypo) { this.p = new Point(p) ; state = cs; isHypothetical = hypo; }
	public PathElement(PathElement right) { this.p = new Point(right.p); state = right.state; isHypothetical = right.isHypothetical; }
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("(" + p.x + "," + p.y + ": " + state);
		sb.append(isHypothetical ? "?" : "!");
		sb.append(")");
		return sb.toString();
	}
	
}