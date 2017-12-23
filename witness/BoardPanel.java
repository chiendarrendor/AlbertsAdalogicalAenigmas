
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class BoardPanel extends JPanel
{
	private static final int EDGELEN = 150;
	private static final int EDGEWIDTH = 25;
	private static final int TOPOFFSET = 10;
	private static final int LEFTOFFSET = 10;
	
	WitnessBoard theBoard;
	
	private static BufferedImage MakeButtonImage(JButton theButton)
	{
		int width = theButton.getWidth();
		int height = theButton.getHeight();
		width = height = Math.min(width,height);
		return new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	}
	
	private interface DisableOverlayable
	{
		public void DisableAndOverlaySolution(Path p);
		public void EnableAndClearSolution();
	}
	
	private class SpaceButton extends JButton implements DisableOverlayable, ActionListener
	{
		public WitnessBoard.Space space;
		
		public SpaceButton(WitnessBoard.Space space)
		{
			this.space = space;
		
			Insets insets = BoardPanel.this.getInsets();
			setBounds(insets.left + LEFTOFFSET + (space.location.x + 1) * EDGEWIDTH + space.location.x * EDGELEN,
					  insets.top + TOPOFFSET + (space.location.y + 1) * EDGEWIDTH + space.location.y * EDGELEN,
					  EDGELEN,EDGELEN);
			addActionListener(this);
		}
		
		public void DisableAndOverlaySolution(Path p) { setEnabled(false); }
		public void EnableAndClearSolution() { setEnabled(true); }
		
		public void actionPerformed(ActionEvent e)
		{
			SpaceDialog.OpenSpaceDialog(space);
			DrawSigils();
		}
		
		public void DrawSigils()
		{
			BufferedImage bi = MakeButtonImage(this);
			bi = SpaceSigils.Draw(bi,space);
			
			if (bi != null)
			{
				setIcon(new ImageIcon(bi));
				setDisabledIcon(new ImageIcon(bi));
			}
			else
			{
				setIcon(null);
				setDisabledIcon(null);
			}
		}
		
		
	}
	
	private class VertexButton extends JButton implements ActionListener, DisableOverlayable
	{
		public WitnessBoard.Vertex vertex;
		
		public VertexButton(WitnessBoard.Vertex vertex)
		{
			this.vertex = vertex;
			Insets insets = BoardPanel.this.getInsets();
			Coordinate c = vertex.location;
			setBounds(insets.left + LEFTOFFSET + c.x * (EDGELEN + EDGEWIDTH),
					  insets.top + TOPOFFSET + c.y * (EDGELEN + EDGEWIDTH),
					  EDGEWIDTH,EDGEWIDTH);
					  
			addActionListener(this);
		}
		
		public void DrawSigils(Path p)
		{
			BufferedImage bi = MakeButtonImage(this);
			bi = VertexSigils.Draw(bi,vertex,p);
			
			if (bi != null)
			{
				setIcon(new ImageIcon(bi));
				setDisabledIcon(new ImageIcon(bi));
			}
			else
			{
				setIcon(null);
				setDisabledIcon(null);
			}
		}
		
		public void DisableAndOverlaySolution(Path p) 
		{ 
			setEnabled(false); 
			DrawSigils(p);		
		}
		
		public void EnableAndClearSolution() 
		{ 
			setEnabled(true); 
			DrawSigils(null);
		}
			
		public void actionPerformed(ActionEvent e)
		{
			VertexDialog.OpenVertexDialog(vertex);
			DrawSigils(null);
		}
		
	}
	
	private class EdgeButton extends JButton implements ActionListener, DisableOverlayable
	{
		public WitnessBoard.Edge edge;
		public EdgeButton(WitnessBoard.Edge edge)
		{
			this.edge = edge;
			Insets insets = BoardPanel.this.getInsets();
			EdgeCoordinate ec = edge.location;
			if (ec.direction == Direction.HORIZONTAL)
			{
				setBounds(insets.left+LEFTOFFSET+ec.x*(EDGELEN+EDGEWIDTH)+EDGEWIDTH,
						  insets.top+TOPOFFSET+ec.y*(EDGELEN+EDGEWIDTH),
						  EDGELEN,EDGEWIDTH);
			}
			else
			{
				setBounds(insets.left+LEFTOFFSET+ec.x*(EDGELEN+EDGEWIDTH),
						  insets.top+TOPOFFSET+ec.y*(EDGELEN+EDGEWIDTH)+EDGEWIDTH,
						  EDGEWIDTH,EDGELEN);				
			}
			addActionListener(this);
		}
		
		private void DrawSigils(Path p)
		{
			BufferedImage bi = MakeButtonImage(this);
			bi = EdgeSigils.Draw(bi,edge,p);
			
			if (bi != null)
			{
				setIcon(new ImageIcon(bi));
				setDisabledIcon(new ImageIcon(bi));
			}
			else
			{
				setIcon(null);
				setDisabledIcon(null);
			}
		}
		
		public void DisableAndOverlaySolution(Path p) 
		{ 
			setEnabled(false); 
			DrawSigils(p);		
		}
		
		public void EnableAndClearSolution() 
		{ 
			setEnabled(true); 
			DrawSigils(null);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			EdgeDialog.OpenEdgeDialog(edge);
			DrawSigils(null);
		}
		
	}
	
	public void DisableAndOverlaySolution(Path p)
	{
		for (Component c : getComponents())
		{
			DisableOverlayable doc = (DisableOverlayable)c;
			doc.DisableAndOverlaySolution(p);
		}
	}
	
	public void EnableAndClearSolution()
	{
		for (Component c : getComponents())
		{
			DisableOverlayable doc = (DisableOverlayable)c;
			doc.EnableAndClearSolution();
		}	
	}
	
	public BoardPanel(WitnessBoard board)
	{
		reLayout(board);
	}
		
	public Dimension getPreferredSize()
	{
		Insets insets = this.getInsets();
		return new Dimension(insets.left + LEFTOFFSET + (theBoard.getWidth() + 1) * EDGEWIDTH + theBoard.getWidth() * EDGELEN + LEFTOFFSET + insets.right,
				             insets.top + TOPOFFSET + (theBoard.getHeight() + 1) * EDGEWIDTH + theBoard.getHeight() * EDGELEN + TOPOFFSET + insets.bottom);
	}
		
		
		
	public void reLayout(WitnessBoard newBoard)
	{
		theBoard = newBoard;
		setLayout(null);
		removeAll();
		for (int x = 0 ; x < theBoard.getWidth() ; ++x)
		{
			for (int y = 0 ; y < theBoard.getHeight() ; ++y)
			{
				add(new SpaceButton(theBoard.getSpace(x,y)));
			}
		}
		for (int x = 0 ; x <= theBoard.getWidth() ; ++x)
		{
			for (int y = 0 ; y <= theBoard.getHeight() ; ++y)
			{
				add(new VertexButton(theBoard.getVertex(x,y)));
			}
		}
		
		for (WitnessBoard.Edge  e : theBoard.getEdges())
		{
			add(new EdgeButton(e));
		}
	}

}