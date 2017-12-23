import java.util.stream.*;
import java.awt.image.*;
import java.awt.*;
import java.util.*;

public class SpaceSigils
{
	public static class OnlySpaceSigil implements WitnessBoard.SpaceSigil
	{
		public Color myColor;
		public OnlySpaceSigil(Color c) { myColor = c; }
		
		public boolean ValidateBoard(WitnessBoard wb,WitnessBoard.Space s,Path p)
		{
			Color myColor = GetOnlySpaceSigilColor(s);
			Vector<WitnessBoard.Space> spaces = WitnessBoardAI.GetConnectedSpaces(wb,p,s);
			for (WitnessBoard.Space ts : spaces)
			{
				if (!HasOnlySpaceSigil(ts)) continue;
				Color tColor = GetOnlySpaceSigilColor(ts);
				if (!WitnessColors.colorsMatch(myColor,tColor)) return false;
			}
			return true;
		}
	}
	
	public static void ClearAllSigils(WitnessBoard.Space s) { s.sigils.clear(); }
	public static boolean HasOnlySpaceSigil(WitnessBoard.Space s) { return s.sigils.stream().filter(u -> u instanceof  OnlySpaceSigil).collect(Collectors.toList()).size() > 0; }
	public static Color GetOnlySpaceSigilColor(WitnessBoard.Space s) 
	{ 
		WitnessBoard.SpaceSigil wss = s.sigils.stream().filter(u -> u instanceof  OnlySpaceSigil).collect(Collectors.toList()).get(0);
		OnlySpaceSigil oss = (OnlySpaceSigil)wss;
		return oss.myColor;
	}

	public static void AddOnlySpaceSigil(WitnessBoard.Space s,Color c) { s.sigils.add(new OnlySpaceSigil(c)); }
	
	public static BufferedImage Draw(BufferedImage bi,WitnessBoard.Space s)
	{
		boolean nullSigil = true;
		
		if (HasOnlySpaceSigil(s))
		{
			Graphics2D g = (Graphics2D)(bi.getGraphics());
			int SIZE = 20;
			
			g.setColor(GetOnlySpaceSigilColor(s));
			g.fillRect((bi.getWidth()-SIZE)/2,(bi.getHeight()-SIZE)/2,SIZE,SIZE);
			nullSigil = false;
		}
		
		return nullSigil ? null : bi;
	}
}