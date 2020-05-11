
import grid.letter.LetterRotate;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class Ada7
{
	public static BoardReader br = null;
	private static JLabel statusBox = null;
	private static JLabel solutionBox = null;
	private static JLabel cluetypeBox = null;

	private static class DrawPanel extends JPanel // implements KeyListener
	{
		private static final int OFFSET=20;
		private static final int INSET=3;
		private static final int CSIZE=30;
		private static final int TEXTXFUDGE = 12;
		private static final int TEXTYFUDGE = 22;
		
		private void DrawStringInCell(Graphics g,int x,int y,String s)
		{
			g.drawString(s,OFFSET+TEXTXFUDGE+x*CSIZE,OFFSET+TEXTYFUDGE+y*CSIZE);
		}
		
		private void FillCell(Graphics g,int x, int y, Color c)
		{
			g.setColor(c);
			g.fillRect(OFFSET+x*CSIZE+1+INSET,OFFSET+y*CSIZE+1+INSET,CSIZE-1-INSET*2,CSIZE-1-INSET*2);
		}	
	
		public DrawPanel() 
		{
//			addKeyListener(this);
//			setFocusable(true);
		}
		
		public Dimension getPreferredSize() { return new Dimension(700,450); }
		public void paint(Graphics g1)
		{
			Graphics2D g = (Graphics2D)g1;
			int width = getWidth();
			int height = getHeight();
			g.setColor(getBackground());
			g.fillRect(0,0,width,height);
			
			if (br.theBoard == null) return;
			
			g.setColor(Color.black);
			
			for (int i = 0 ; i <= br.theBoard.width ; ++i)
			{
				if (i == 0 || i == br.theBoard.width) g.drawLine(OFFSET+i*CSIZE,OFFSET,OFFSET+i*CSIZE,OFFSET+br.theBoard.height*CSIZE);
				if (i!=br.theBoard.width) DrawStringInCell(g,i,-1,""+i);
			}
			for (int i = 0 ; i <= br.theBoard.height ; ++i)
			{
				if (i == 0 || i == br.theBoard.height) g.drawLine(OFFSET,OFFSET+i*CSIZE,OFFSET+br.theBoard.width*CSIZE,OFFSET+i*CSIZE);
				if (i!=br.theBoard.height) DrawStringInCell(g,-1,i,""+i);
			}
			
			for (int x = 0 ; x < br.theBoard.width; ++x)
			{
				for (int y = 0 ; y < br.theBoard.height ; ++y)
				{
					int ulx = OFFSET + x * CSIZE;
					int uly = OFFSET + y * CSIZE;
					
					if (br.theBoard.isSpecial[x][y]) FillCell(g,x,y,Color.lightGray);
					g.setColor(Color.black);
					int num = br.numbers.GetCellNumber(x,y);
					if (num != -1) DrawStringInCell(g,x,y,""+num+br.rawshades[x][y]);
					
					if (x < br.theBoard.width - 1) 
					{
						g.setStroke(new BasicStroke(5));
						g.setColor(br.theBoard.cells[x][y].id == br.theBoard.cells[x+1][y].id ? Color.lightGray : Color.black);
						g.drawLine(ulx+CSIZE,uly,ulx+CSIZE,uly+CSIZE);
						g.setStroke(new BasicStroke(1));
					}
					if (y < br.theBoard.height - 1) 
					{
						g.setStroke(new BasicStroke(5));
						g.setColor(br.theBoard.cells[x][y].id == br.theBoard.cells[x][y+1].id ? Color.lightGray : Color.black);
						g.drawLine(ulx,uly+CSIZE,ulx+CSIZE,uly+CSIZE);
						g.setStroke(new BasicStroke(1));
					}
				}
			}			
		}
	}


	
	public static void PrintRegionKeys()
	{
		Set<Character> keys = br.theBoard.regionsById.keySet();
		java.util.List<Character> list = new ArrayList<Character>(keys);
		java.util.Collections.sort(list);
		for (Character c : list)
		{
			System.out.print(c);
		}
		System.out.println("");	
	}
	
	private static void createAndShowGUI(String status)
	{
		JFrame frame = new JFrame("Adalogical Enigma 7 Board Solver");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel boxpanel = new JPanel();
		boxpanel.setLayout(new BoxLayout(boxpanel,BoxLayout.Y_AXIS));
		mainPanel.add(boxpanel,BorderLayout.SOUTH);

		statusBox = new JLabel(status);
		boxpanel.add(statusBox);

		solutionBox = new JLabel(br.solution);
		boxpanel.add(solutionBox);
		
		DrawPanel dp = new DrawPanel();
		mainPanel.add(dp,BorderLayout.CENTER);
		
		frame.getContentPane().add(mainPanel);

		//frame.pack();
		frame.setVisible(true);		
		
		Insets insets = frame.getInsets();
		Dimension psize = dp.getPreferredSize();
		frame.setSize(insets.left + insets.right + psize.width,
					  insets.top + insets.bottom + psize.height);
		

	}
	
	public static void main(String[] args)
	{
		if (args.length != 1) {
			System.out.println("Bad Command Line");
			System.exit(1);
		}

		br = new BoardReader(args[0]);
		
		Solver s = new Solver(br.numbers);
		s.Run();

		br.numbers = s.solutions.firstElement();
		StringBuffer sb = new StringBuffer();

		if (br.cluetype.equals("sumcolumn")) {
			for (int x = 0; x < br.numbers.getWidth(); ++x) {
				int colsum = 0;
				for (int y = 0; y < br.numbers.getHeight(); ++y) {
					if (br.theBoard.isSpecial[x][y]) colsum += br.numbers.GetCellNumber(x, y);
				}
				if (colsum == 0) sb.append("-");
				else sb.append((char) (colsum + 'A' - 1));
			}
		} else if (br.cluetype.equals("mod3")) {
			for (int y = 0 ; y < br.numbers.getHeight() ; ++y) {
				for (int x = 0 ; x < br.numbers.getWidth() ; ++x) {
					if (br.numbers.GetCellNumber(x,y) % 3 != 0) continue;
					if (br.rawshades[x][y] == '.') continue;
					sb.append(LetterRotate.Rotate(br.rawshades[x][y],br.numbers.GetCellNumber(x,y)));
				}
			}
		} else {
			sb.append("unknown clue type: " + br.cluetype);
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() { 
			public void run() { createAndShowGUI(sb.toString()); }
		});		
	}
}
		