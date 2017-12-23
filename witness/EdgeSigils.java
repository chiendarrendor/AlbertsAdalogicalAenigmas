import java.util.stream.*;
import java.awt.image.*;
import java.awt.*;

public class EdgeSigils
{
	public static class BlockerEdgeSigil implements WitnessBoard.EdgeSigil
	{
	}
		
	public static void ClearAllSigils(WitnessBoard.Edge v) { v.sigils.clear(); }
	public static boolean HasBlockerSigil(WitnessBoard.Edge e) { return e.sigils.stream().filter(u -> u instanceof  BlockerEdgeSigil).collect(Collectors.toList()).size() > 0; }
	public static void AddBlockerSigil(WitnessBoard.Edge e) { e.sigils.add(new BlockerEdgeSigil()); }
	
	public static BufferedImage Draw(BufferedImage bi,WitnessBoard.Edge e,Path p)
	{
		boolean nullSigil = false;	
		if (HasBlockerSigil(e))
		{
			Graphics2D g = (Graphics2D)(bi.getGraphics());
			g.setColor(Color.black);
			g.setStroke(new BasicStroke(3));
			g.drawLine(0,0,bi.getWidth()-1,bi.getHeight()-1);
			g.drawLine(bi.getWidth()-1,0,0,bi.getHeight()-1);
		}
		else
		{
			nullSigil = true;
		}
		
		if (p != null && p.hasEdge(e))
		{		
			nullSigil = false;
			Graphics2D g = (Graphics2D)(bi.getGraphics());
			g.setColor(Color.blue);
			g.setStroke(new BasicStroke(3));
			if (e.location.direction == Direction.HORIZONTAL)
			{
				g.drawLine(0,bi.getHeight() / 2,bi.getWidth()-1,bi.getHeight() / 2);
			}
			else
			{
				g.drawLine(bi.getWidth()/2,0,bi.getWidth()/2,bi.getHeight()-1);
			}
		}
		
		return nullSigil ? null : bi;
		
	}
	
}