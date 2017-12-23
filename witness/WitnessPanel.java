
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class WitnessPanel extends JPanel
{
	JTextField statBox = new JTextField();
	JButton backButton = new JButton("<");
	JButton forButton = new JButton(">");
	JButton solveButton = new JButton("Solve");
	JButton editButton = new JButton("Edit");
	JButton newButton = new JButton("New Board");
	BoardPanel bpanel = null;
	WitnessBoard theBoard = new WitnessBoard(4,4);
	
	int solutionIndex = -1;
	Vector<Path> solutions = null;
	
	private void Solve()
	{
		solutions = WitnessBoardAI.FindPaths(theBoard);
		if (solutions.size() == 0)
		{
			statBox.setText("Board has no solutions!  Try again...");
			return;
		}
		statBox.setText("Board has " + solutions.size() + " solutions");
		solutionIndex = 0;
		solveButton.setEnabled(false);
		editButton.setEnabled(true);
		if (solutions.size() > 1) forButton.setEnabled(true);
		bpanel.DisableAndOverlaySolution(solutions.elementAt(solutionIndex));
	}

	private void Forward()
	{
		if (solutionIndex >= solutions.size() - 1) 
		{
			forButton.setEnabled(false);
			return;
		}
		solutionIndex++;
		bpanel.DisableAndOverlaySolution(solutions.elementAt(solutionIndex));
		if (solutionIndex == solutions.size() - 1) { forButton.setEnabled(false); }
		if (solutions.size() > 1) { backButton.setEnabled(true); }
	}

	private void Back()
	{
		if (solutionIndex <= 0) 
		{
			backButton.setEnabled(false);
			return;
		}
		solutionIndex--;
		bpanel.DisableAndOverlaySolution(solutions.elementAt(solutionIndex));
		if (solutionIndex == 0) { backButton.setEnabled(false); }
		if (solutions.size() > 1) { forButton.setEnabled(true); }
	}	
		
	private void Edit()
	{
		editButton.setEnabled(false);
		solveButton.setEnabled(true);
		forButton.setEnabled(false);
		backButton.setEnabled(false);
		bpanel.EnableAndClearSolution();
		solutions = null;
		solutionIndex = -1;
		statBox.setText("");
	}
		
	
	
	
	
	private void getNewBoard()
	{
		solutions = null;
		solutionIndex = -1;
		solveButton.setEnabled(true);
		backButton.setEnabled(false);
		forButton.setEnabled(false);
		editButton.setEnabled(false);
		
		JTextField widthField = new JTextField(5);
		JTextField heightField = new JTextField(5);
		JPanel tfpanel = new JPanel();
		tfpanel.add(new JLabel("Number of Cells across: "));
		tfpanel.add(widthField);
		tfpanel.add(Box.createHorizontalStrut(15));
		tfpanel.add(new JLabel("Number of Cells down: "));
		tfpanel.add(heightField);
		
		int result = JOptionPane.showConfirmDialog(null,tfpanel,"Create New Board",JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.CANCEL_OPTION)
		{
			statBox.setText("Cancelled Creation of new board");
			return;
		}
		
		int nw;
		try
		{
			nw = Integer.parseInt(widthField.getText());
		}
		catch(NumberFormatException e)
		{
			statBox.setText("width input " + widthField.getText() + " is not a valid integer");
			return;
		}
		if (nw < 0 || nw > 10)
		{
			statBox.setText("Invalid width input");
			return;
		}
		
		int nh;
		try
		{
			nh = Integer.parseInt(heightField.getText());
		}
		catch(NumberFormatException e)
		{
			statBox.setText("height input " + heightField.getText() + " is not a valid integer");
			return;
		}
		if (nh < 0 || nh > 10)
		{
			statBox.setText("Invalid height input");
			return;
		}	
		
		theBoard = new WitnessBoard(nw,nh);
		statBox.setText("New Board created: " + nw + " x " + nh);
		bpanel.reLayout(theBoard);
		
		JFrame daddy = (JFrame)getTopLevelAncestor();
		daddy.pack();
	}
	
	
	
	

	
	public WitnessPanel()
	{
		super(new BorderLayout());
		bpanel = new BoardPanel(theBoard);
		
		add(statBox,BorderLayout.NORTH);
		
		Box verticalBox1 = Box.createVerticalBox();
		verticalBox1.add(Box.createVerticalGlue());
		verticalBox1.add(backButton);
		verticalBox1.add(Box.createVerticalGlue());
		
		add(verticalBox1,BorderLayout.WEST);
		
		Box verticalBox2 = Box.createVerticalBox();
		verticalBox2.add(Box.createVerticalGlue());
		verticalBox2.add(forButton);
		verticalBox2.add(Box.createVerticalGlue());
		
		add(verticalBox2,BorderLayout.EAST);
		
		backButton.setEnabled(false);
		forButton.setEnabled(false);
		
		add(bpanel,BorderLayout.CENTER);
		
		JPanel butpan = new JPanel();
		butpan.add(solveButton);
		butpan.add(editButton);
		butpan.add(newButton);
		
		//solveButton.addActionListener(this);
		editButton.setEnabled(false);
		
		
		newButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { getNewBoard(); } } );
		solveButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { Solve(); } } );
		forButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { Forward(); } } );
		backButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { Back(); } } );
		editButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { Edit(); } } );
		
		add(butpan,BorderLayout.SOUTH);
	}
}
		