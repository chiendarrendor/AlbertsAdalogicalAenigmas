
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;
import java.io.*;

public class Galaxies
{
	private static GalaxyBoard theBoard = null;
	private static GalaxyBoard curBoard = null;
	private static GalaxyAI gai = null;
	
	private static JLabel statusBox = null;

	public static void MakeBoard(String filename)
	{
		Pattern startPattern = Pattern.compile("^(\\d+)\\s+(\\d+)$");	
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line = reader.readLine();
			if (line == null) throw new RuntimeException("invalid input file");
			Matcher start = startPattern.matcher(line);
			if (!start.find()) throw new RuntimeException("invalid input file header");
			theBoard = new GalaxyBoard(Integer.parseInt(start.group(1)),Integer.parseInt(start.group(2)));
			
			while((line = reader.readLine()) != null)
			{	
				String[] parts = line.split("\\s+");

				if (parts[0].equals("B"))
				{
					theBoard.AddNewBlackArea(Double.parseDouble(parts[1]),Double.parseDouble(parts[2]));
				}
				else
				{
					theBoard.AddNewWhiteArea(Double.parseDouble(parts[1]),Double.parseDouble(parts[2]));
				}	
			}
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}	
		curBoard = theBoard;
		gai = new GalaxyAI(theBoard);
	}
	
	private static class DrawPanel extends JPanel implements KeyListener
	{
		private static final int OFFSET=20;
		private static final int CSIZE=20;
		private static final int TEXTXFUDGE = 4;
		private static final int TEXTYFUDGE = 14;
		
		private void DrawStringInCell(Graphics g,int x,int y,String s)
		{
			g.drawString(s,OFFSET+TEXTXFUDGE+x*CSIZE,OFFSET+TEXTYFUDGE+y*CSIZE);
		}
		
		private void FillCell(Graphics g,int x, int y, Color c)
		{
			g.setColor(c);
			g.fillRect(OFFSET+x*CSIZE+1,OFFSET+y*CSIZE+1,CSIZE-2,CSIZE-2);
		}
	

		public void keyPressed(KeyEvent e) {}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e)
		{
			
			gai.RunOne();
			statusBox.setText("Queue Size: " + gai.queue.size() + " # of solutions: " + gai.solutions.size());
			if (gai.queue.size() > 0)
			{
				curBoard = gai.queue.elementAt(0);
				repaint();
			}
			else if (gai.solutions.size() > 0)
			{
				curBoard = gai.solutions.elementAt(0);
				repaint();
			} 
			else
			{
				curBoard = null;
				repaint();
			}		
		}

	
	
		public DrawPanel() 
		{
			addKeyListener(this);
			setFocusable(true);
		}
		
		public Dimension getPreferredSize() { return new Dimension(700,450); }
		public void paint(Graphics g)
		{
			int OFFSET = 20;
			int CSIZE = 20;
			int TEXTXFUDGE = 4;
			int TEXTYFUDGE = 14;
			
			int width = getWidth();
			int height = getHeight();
			g.setColor(getBackground());
			g.fillRect(0,0,width,height);
			
			if (curBoard == null) return;
			
			g.setColor(Color.black);
			for (int i = 0 ; i <= curBoard.width ; ++i)
			{
				g.drawLine(OFFSET+i*CSIZE,OFFSET,OFFSET+i*CSIZE,OFFSET+curBoard.height*CSIZE);
				if (i!=curBoard.width) DrawStringInCell(g,i,-1,""+i);
			}
			for (int i = 0 ; i <= curBoard.height ; ++i)
			{
				g.drawLine(OFFSET,OFFSET+i*CSIZE,OFFSET+curBoard.width*CSIZE,OFFSET+i*CSIZE);
				if (i!=curBoard.height) DrawStringInCell(g,-1,i,""+i);
			}
			
			for (int x = 0 ; x < curBoard.width; ++x)
			{
				for (int y = 0 ; y < curBoard.height ; ++y)
				{
					GalaxyBoard.Area.Pair ap = curBoard.getBoardCell(x,y);
					if (ap != null)
					{
						GalaxyBoard.Area area = curBoard.getAreaById(ap.getAreaId());
					
						FillCell(g,x,y,area.getColor());

						g.setColor(Color.red);
						DrawStringInCell(g,x,y,""+ap.getAreaId());
					} 					
				}
			}			
		}
	}
	
	
	
	private static void createAndShowGUI()
	{
		JFrame frame = new JFrame("Adalogical Enigma 30 Board Solver");
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
		if (args.length != 1) throw new RuntimeException("invalid command line");
	
		MakeBoard(args[0]);
		javax.swing.SwingUtilities.invokeLater(new Runnable() { 
			public void run() { createAndShowGUI(); }
		});
	}
	
	
}
		