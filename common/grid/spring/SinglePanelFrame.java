package grid.spring;

import javax.swing.JFrame;
import java.awt.Component;

public class SinglePanelFrame extends JFrame
{

	private void start(Component c)
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(c);
		
		pack();
		setVisible(true);		
	}
	
	public SinglePanelFrame(String title, Component c)
	{
		super(title);

		javax.swing.SwingUtilities.invokeLater(new Runnable() { 
			public void run() { start(c); }
		});			
	}		
}