

import java.awt.Color;
import java.util.*;

public class WitnessColors
{
	public static String[] colorNames = new String[] {
		"Black",
		"Gray",
		"White",
		"Red",
		"Orange",
		"Yellow",
		"Green",
		"Cyan",
		"Blue",
		"Magenta"
	};
	
	public static Color[] colors = new Color[] {
		Color.black,
		Color.darkGray,
		Color.white,
		Color.red,
		Color.orange,
		Color.yellow,
		Color.green,
		Color.cyan,
		Color.blue,
		Color.magenta
	};
	
	public static Map<Color,Integer> colorSets = new HashMap<Color,Integer>();
	static {
		colorSets.put(Color.darkGray,1);
		colorSets.put(Color.blue,2);
		colorSets.put(Color.red,3);
		colorSets.put(Color.magenta,4);
		colorSets.put(Color.green,5);
		colorSets.put(Color.cyan,6);
		colorSets.put(Color.black,7);
		colorSets.put(Color.white,8);
		colorSets.put(Color.orange,9);
		colorSets.put(Color.yellow,10);
	}
	
	public static Color colorOfString(String s)
	{
		for (int i = 0 ; i < colorNames.length ; ++i)
		{
			if (s.equals(colorNames[i])) return colors[i];
		}
		return null;
	}
	
	public static String stringOfColor(Color c)
	{
		for (int i = 0 ; i < colors.length ; ++i)
		{
			if (c.equals(colors[i])) return colorNames[i];
		}
		return null;
	}
	
	public static boolean colorsMatch(Color c1, Color c2)
	{
		int cs1 = -1;
		if (colorSets.containsKey(c1)) cs1 = colorSets.get(c1);
		int cs2 = -1;
		if (colorSets.containsKey(c2)) cs2 = colorSets.get(c2);
		
		if (cs1 == -1 && cs2 == -1) return c1.equals(c2);
		return cs1 == cs2;
	}
		
	
}