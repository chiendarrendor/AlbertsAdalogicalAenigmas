import java.util.stream.*;
import java.awt.image.*;
import java.awt.*;

public class VertexSigils
{
	public static class StartVertexSigil implements WitnessBoard.VertexSigil
	{
	}
	
	public static class EndVertexSigil implements WitnessBoard.VertexSigil
	{
	}
	
	public static class ThroughVertexSigil implements WitnessBoard.VertexSigil
	{
	}
	
	public static void ClearAllSigils(WitnessBoard.Vertex v) { v.sigils.clear(); }
	public static boolean HasStartSigil(WitnessBoard.Vertex v) { return v.sigils.stream().filter(u -> u instanceof StartVertexSigil).collect(Collectors.toList()).size() > 0; }
	public static boolean HasEndSigil(WitnessBoard.Vertex v) { return v.sigils.stream().filter(u -> u instanceof EndVertexSigil).collect(Collectors.toList()).size() > 0; }
	public static boolean HasThroughSigil(WitnessBoard.Vertex v) { return v.sigils.stream().filter(u -> u instanceof ThroughVertexSigil).collect(Collectors.toList()).size() > 0; }	
	public static void AddThroughSigil(WitnessBoard.Vertex v) { v.sigils.add(new ThroughVertexSigil()); }
	public static void AddStartSigil(WitnessBoard.Vertex v) { v.sigils.add(new StartVertexSigil()); }
	public static void AddEndSigil(WitnessBoard.Vertex v) { v.sigils.add(new EndVertexSigil()); }
	
	public static final int INSET=0;
	public static BufferedImage Draw(BufferedImage bi,WitnessBoard.Vertex v,Path p)
	{
		boolean nullSigil = false;
		if (HasThroughSigil(v))
		{
			Graphics g = bi.getGraphics();
			g.setColor(Color.black);
			g.fillRect(0+INSET,0+INSET,bi.getWidth()-1-INSET,bi.getHeight()-1-INSET);
		}
		else if (HasStartSigil(v))
		{
			Graphics g = bi.getGraphics();
			g.setColor(Color.green);
			g.fillRect(0,0,bi.getWidth()-1,bi.getHeight()-1);
		}
		else if (HasEndSigil(v))
		{
			Graphics g = bi.getGraphics();
			g.setColor(Color.red);
			g.fillRect(0,0,bi.getWidth()-1,bi.getHeight()-1);
		} 
		else
		{
			nullSigil = true;
		}
		
		if (p != null && p.hasVertex(v))
		{
			int inset = 5;
			nullSigil = false;
			Graphics g = bi.getGraphics();
			g.setColor(Color.blue);
			g.fillOval(inset,inset,bi.getWidth() - 2 * inset , bi.getHeight() - 2 * inset);
		}
		
		return nullSigil ? null : bi;
	}
	
}