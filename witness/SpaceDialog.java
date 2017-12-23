

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class SpaceDialog
{
	public static JPanel compPanel = null;
	public static JRadioButton noneButton = null;
//	public static JRadioButton pairButton = null;
	public static JRadioButton onlyButton = null;
	public static JComboBox<String> colorBox = null;
	
	public static void OpenSpaceDialog(WitnessBoard.Space s)
	{
		CreateCompPanel();
		FillCompPanel(s);
		
		int result = JOptionPane.showConfirmDialog(null,compPanel,"Set Space Properties",JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.CANCEL_OPTION)
		{
			return;
		}
		
		ClearSpaceSigils(s);
		SetSpaceSigils(s);
	}
	
	public static void CreateCompPanel()
	{
		if (compPanel != null) return;
		compPanel = new JPanel(new BorderLayout());
		
		JPanel buttonPanel = new JPanel(new GridLayout(0,1));
		compPanel.add(buttonPanel,BorderLayout.CENTER);
		
		ButtonGroup bg = new ButtonGroup();
		
		noneButton = new JRadioButton("Nothing Special");
		buttonPanel.add(noneButton);
		bg.add(noneButton);
		noneButton.setSelected(true);
		
//		pairButton = new JRadioButton("Exactly Two Of this Color Per Area");
//		buttonPanel.add(pairButton);
//		bg.add(pairButton);
		
		onlyButton = new JRadioButton("Only one color may be in an area");
		buttonPanel.add(onlyButton);
		bg.add(onlyButton);
		
		String[] colorStrings = WitnessColors.colorNames;
		colorBox = new JComboBox<String>(colorStrings);
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.add(Box.createVerticalGlue());
		verticalBox.add(colorBox);
		verticalBox.add(Box.createVerticalGlue());
		
		
		compPanel.add(verticalBox,BorderLayout.EAST);
	}
	
	public static void FillCompPanel(WitnessBoard.Space s)
	{
		if (SpaceSigils.HasOnlySpaceSigil(s))
		{
			onlyButton.setSelected(true);
			colorBox.setSelectedItem(WitnessColors.stringOfColor(SpaceSigils.GetOnlySpaceSigilColor(s)));
		}
		else
		{
			noneButton.setSelected(true);
		}
	}
	
	public static void ClearSpaceSigils(WitnessBoard.Space s)
	{
		SpaceSigils.ClearAllSigils(s);
	}
	
	public static void SetSpaceSigils(WitnessBoard.Space s)
	{
		if (onlyButton.isSelected())
		{
			SpaceSigils.AddOnlySpaceSigil(s,WitnessColors.colorOfString((String)colorBox.getSelectedItem()));
		}
	}
}
