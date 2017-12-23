
import javax.swing.*;
import java.awt.*;

public class WitnessBoardSolver
{

	private static void createAndShowGUI()
	{
		JFrame frame = new JFrame("Witness Board Solver");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		WitnessPanel wp = new WitnessPanel();
		
		frame.getContentPane().add(wp);

		//frame.pack();
		frame.setVisible(true);		
		
		Insets insets = frame.getInsets();
		Dimension psize = wp.getPreferredSize();
		frame.setSize(insets.left + insets.right + psize.width,
					  insets.top + insets.bottom + psize.height);
		

	}

	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() { 
			public void run() { createAndShowGUI(); }
		});
	}
}