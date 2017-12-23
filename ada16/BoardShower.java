import java.awt.image.*;
import java.awt.*;
import java.util.*;


public class BoardShower
{
	private Vector<Board> boards = new Vector<Board>();
	private GridFrame gf = null;
	
	private class MyListener implements GridPanel.MultiGridListener
	{
		private int boardindex = 0;
		
		public int getNumXCells() { return boards.elementAt(boardindex).width; }
		public int getNumYCells() { return boards.elementAt(boardindex).height; }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		
		public boolean drawCellContents(int x,int y,BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();
			// insetting slightly
			int INSET = 2;
			int ulx = INSET;
			int uly = INSET;
			
			int w = bi.getWidth() - 2*INSET;
			int h = bi.getHeight() - 2*INSET;
						
			if (boards.elementAt(boardindex).isTile(x,y))
			{
				g.setColor(Color.blue);
				g.fillRect(ulx,uly,w,h);
			}
			
			if (boards.elementAt(boardindex).isTile(x,y))
			{
				if (boards.elementAt(boardindex).isStart(x,y)) { GridPanel.DrawStringInCell(bi,Color.white,"S"); }
				if (boards.elementAt(boardindex).isEnd(x,y)) { GridPanel.DrawStringInCell(bi,Color.white,"E");  }
			}
			
			
			
			if (boards.elementAt(boardindex).isTree(x,y))
			{
				g.setColor(Color.red);
				g.drawOval(ulx,uly,w,h);
			}
			
			if (boards.elementAt(boardindex).hasLetter(x,y))
			{
				GridPanel.DrawStringInCell(bi,Color.black,"" + boards.elementAt(boardindex).getLetter(x,y));
			}
			return true;
		}
		
		public boolean hasPrev() 
		{ 
			return boardindex > 0;
		}
		
		public boolean hasNext() 
		{ 
			return boardindex < boards.size() - 1;
		}
		public void moveToPrev() 
		{
			--boardindex;
		}
		
		public void moveToNext() 
		{
			++boardindex;
		}
	}

	public BoardShower(Vector<Board> boards)
	{
		this.boards = boards;
		gf = new GridFrame("Adalogical Aenigma #16 Board",1024,768,new MyListener());
	}
	
	public BoardShower(Board board)
	{
		boards.add(board);
		gf = new GridFrame("Adalogical Aenigma #16 Board",1024,768,new MyListener());
	}
	
	public void dispose() { gf.dispose(); }

}