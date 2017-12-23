
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class Ada7
{
	public static BoardReader br = null;
	private static JLabel statusBox = null;

	private static class DrawPanel extends JPanel // implements KeyListener
	{
		private static final int OFFSET=20;
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
			g.fillRect(OFFSET+x*CSIZE+1,OFFSET+y*CSIZE+1,CSIZE-1,CSIZE-1);
		}	
	
		public DrawPanel() 
		{
//			addKeyListener(this);
//			setFocusable(true);
		}
		
		public Dimension getPreferredSize() { return new Dimension(700,450); }
		public void paint(Graphics g)
		{			
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
					if (num != -1) DrawStringInCell(g,x,y,""+num);
					
					if (x < br.theBoard.width - 1) 
					{
						g.setColor(br.theBoard.cells[x][y].id == br.theBoard.cells[x+1][y].id ? Color.lightGray : Color.black);
						g.drawLine(ulx+CSIZE,uly,ulx+CSIZE,uly+CSIZE);
					}
					if (y < br.theBoard.height - 1) 
					{
						g.setColor(br.theBoard.cells[x][y].id == br.theBoard.cells[x][y+1].id ? Color.lightGray : Color.black);
						g.drawLine(ulx,uly+CSIZE,ulx+CSIZE,uly+CSIZE);
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
	
	private static void createAndShowGUI()
	{
		JFrame frame = new JFrame("Adalogical Enigma 7 Board Solver");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		statusBox = new JLabel("STATUS");
		mainPanel.add(statusBox,BorderLayout.SOUTH);
		
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
		br = new BoardReader("ada7.txt");
		
		Solver s = new Solver(br.numbers);
		s.Run();

		br.numbers = s.solutions.firstElement();
		
		for (int x = 0 ; x < br.numbers.getWidth() ; ++x)
		{
			int colsum = 0;
			for (int y = 0 ; y < br.numbers.getHeight() ; ++y)
			{
				if (br.theBoard.isSpecial[x][y]) colsum += br.numbers.GetCellNumber(x,y);
			}
			if (colsum == 0) System.out.print("-");
			else System.out.print((char)(colsum+'A'-1));
		}
		System.out.println("");
		
		
		
		
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() { 
			public void run() { createAndShowGUI(); }
		});		
	}
}
		