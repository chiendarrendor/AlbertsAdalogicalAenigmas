

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class VertexDialog
{
	public static JPanel compPanel = null;
	public static JRadioButton noneButton = null;
	public static JRadioButton throughButton = null;
	public static JRadioButton startButton = null;
	public static JRadioButton endButton = null;
	
	public static void OpenVertexDialog(WitnessBoard.Vertex v)
	{
		CreateCompPanel();
		FillCompPanel(v);
		
		int result = JOptionPane.showConfirmDialog(null,compPanel,"Set Vertex Properties",JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.CANCEL_OPTION)
		{
			return;
		}
		
		ClearVertexSigils(v);
		SetVertexSigils(v);
	}
	
	public static void CreateCompPanel()
	{
		if (compPanel != null) return;
		compPanel = new JPanel(new GridLayout(0,1));
		ButtonGroup bg = new ButtonGroup();
		
		noneButton = new JRadioButton("Nothing Special");
		compPanel.add(noneButton);
		bg.add(noneButton);
		noneButton.setSelected(true);
		
		throughButton = new JRadioButton("Path MUST pass through this vertex");
		compPanel.add(throughButton);
		bg.add(throughButton);
		
		startButton = new JRadioButton("Path must START here");
		compPanel.add(startButton);
		bg.add(startButton);
		
		endButton = new JRadioButton("Path must END here");
		compPanel.add(endButton);
		bg.add(endButton);
	}
	
	public static void FillCompPanel(WitnessBoard.Vertex v)
	{
		if (VertexSigils.HasStartSigil(v)) startButton.setSelected(true);
		else if (VertexSigils.HasEndSigil(v)) endButton.setSelected(true);
		else if (VertexSigils.HasThroughSigil(v)) throughButton.setSelected(true);
		else noneButton.setSelected(true);
	}
	
	public static void ClearVertexSigils(WitnessBoard.Vertex v)
	{
		VertexSigils.ClearAllSigils(v);
	}
	
	public static void SetVertexSigils(WitnessBoard.Vertex v)
	{
			if (throughButton.isSelected()) VertexSigils.AddThroughSigil(v);
			if (startButton.isSelected()) VertexSigils.AddStartSigil(v);
			if (endButton.isSelected()) VertexSigils.AddEndSigil(v);
	}
}
