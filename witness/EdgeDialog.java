

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class EdgeDialog
{
	public static JPanel compPanel = null;
	public static JCheckBox blockerBox = null;
	
	public static void OpenEdgeDialog(WitnessBoard.Edge e)
	{
		CreateCompPanel();
		FillCompPanel(e);
		
		int result = JOptionPane.showConfirmDialog(null,compPanel,"Set Edge Properties",JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.CANCEL_OPTION)
		{
			return;
		}
		
		ClearEdgeSigils(e);
		SetEdgeSigils(e);
	}
	
	public static void CreateCompPanel()
	
	{
		if (compPanel != null) return;
		compPanel = new JPanel(new GridLayout(0,1));
		blockerBox = new JCheckBox("Edge is Blocked");
		compPanel.add(blockerBox);
	}
	
	public static void FillCompPanel(WitnessBoard.Edge e)
	{
		blockerBox.setSelected(EdgeSigils.HasBlockerSigil(e));
	}
	
	public static void ClearEdgeSigils(WitnessBoard.Edge e)
	{
		EdgeSigils.ClearAllSigils(e);
	}
	
	public static void SetEdgeSigils(WitnessBoard.Edge e)
	{
		if (blockerBox.isSelected()) EdgeSigils.AddBlockerSigil(e);
	}
}
