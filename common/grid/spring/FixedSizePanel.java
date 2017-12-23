package grid.spring;

import javax.swing.*;
import java.util.*;
import java.awt.*;

public class FixedSizePanel extends JPanel
{
	private int fixWidth;
	private int fixHeight;

	public int getFixedWidth() { return fixWidth; }
	public int getFixedHeight() { return fixHeight; }
	public Dimension getPreferredSize() { return new Dimension(fixWidth,fixHeight); }
	
	public FixedSizePanel(int width,int height) { fixWidth = width ; fixHeight = height; }
}