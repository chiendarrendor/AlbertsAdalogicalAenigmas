package grid.spring;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;


public class GridPanelContainer extends JPanel
{
	GridPanel gp = null;
	GridPanel.GridListener gl = null;
	GridPanel.MultiGridListener mgl = null;
	JButton prevButton = new JButton("<");
	JButton nextButton = new JButton(">");
	JLabel answer = new JLabel();

	private void setAnswer() {
		String[] lines = gl.getAnswerLines();
		StringBuffer sb = new StringBuffer();
		sb.append("<html><font size=\"5\">");
		Arrays.stream(lines).forEach(line->sb.append(line).append("<br>"));
		sb.append("</font></html>");
		answer.setText(sb.toString());
	}



	private class PrevHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			mgl.moveToPrev();
			prevButton.setEnabled(mgl.hasPrev());
			nextButton.setEnabled(mgl.hasNext());
			setAnswer();
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
			setAnswer();
			gp.repaint();
		}
	}	

	private GridPanelFactory gpf = new GridPanelFactoryImpl();


	public GridPanelContainer(GridPanelFactory gpf,int width, int height,GridPanel.GridListener listener) { this(gpf,width,height,listener,null); }
	public GridPanelContainer(int width, int height,GridPanel.GridListener listener,GridPanel.EdgeListener edgeListener) { this(null,width,height,listener,edgeListener); }
	public GridPanelContainer(int width, int height,GridPanel.GridListener listener) { this(null,width,height,listener,null); }

	public GridPanelContainer(GridPanelFactory i_gpf,int width, int height,GridPanel.GridListener listener,GridPanel.EdgeListener edgeListener)
	{
		super(new BorderLayout());

		if (i_gpf != null) {
			gpf = i_gpf;
		}

		gl = listener;

		if (listener instanceof GridPanel.MultiGridListener)
		{
			mgl = (GridPanel.MultiGridListener)listener;
		}

		gp = gpf.getGridPanel(width, height, listener, edgeListener);
		add(gp, BorderLayout.CENTER);
		add(answer,BorderLayout.SOUTH);

		setAnswer();

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

