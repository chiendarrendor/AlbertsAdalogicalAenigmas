package grid.spring;

import javax.swing.*;
import java.awt.*;

public class VerticalCenterPanel extends Box
{
	public VerticalCenterPanel(Component c)
	{
		super(BoxLayout.Y_AXIS);
		add(Box.createVerticalGlue());
		add(c);
		add(Box.createVerticalGlue());
	}
}
		