package grid.spring;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;


public class GridPanelContainer extends JPanel
{
	GridPanel gp = null;
	GridPanel.MultiGridListener mgl = null;
	JButton prevButton = new JButton("<");
	JButton nextButton = new JButton(">");
	JLabel answer = new JLabel();
	
	private class PrevHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			mgl.moveToPrev();
			prevButton.setEnabled(mgl.hasPrev());
			nextButton.setEnabled(mgl.hasNext());
			gp.repaint();
		}
	}
	
	private class NextHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			mgl.moveToNext();
			prevButton.setEnabled(mgl.hasPrev());
			nextButton.setEnabled(mgl.hasNext());
			gp.repaint();
		}
	}	
	
	public GridPanelContainer(int width, int height,GridPanel.GridListener listener)
	{
		this(width,height,listener,null);
	}
	
	public GridPanelContainer(int width, int height,GridPanel.GridListener listener,GridPanel.EdgeListener edgeListener)
	{
		super(new BorderLayout());
		
		if (listener instanceof GridPanel.MultiGridListener)
		{
			mgl = (GridPanel.MultiGridListener)listener;
		}
		
		gp = new GridPanel(width,height,listener,edgeListener);
		add(gp, BorderLayout.CENTER);
		add(answer,BorderLayout.SOUTH);

		String[] lines = listener.getAnswerLines();
		StringBuffer sb = new StringBuffer();
		sb.append("<html><font size=\"5\">");
		Arrays.stream(lines).forEach(line->sb.append(line).append("<br>"));
		sb.append("</font></html>");
		answer.setText(sb.toString());

        Border border = BorderFactory.createLineBorder(Color.BLUE, 5);
        answer.setBorder(border);


        if (mgl != null)
		{
			add(new VerticalCenterPanel(prevButton),BorderLayout.WEST);
			add(new VerticalCenterPanel(nextButton),BorderLayout.EAST);
			
			prevButton.setEnabled(mgl.hasPrev());
			nextButton.setEnabled(mgl.hasNext());
			
			prevButton.addActionListener(new PrevHandler());
			nextButton.addActionListener(new NextHandler());
			
		}
	}
}

