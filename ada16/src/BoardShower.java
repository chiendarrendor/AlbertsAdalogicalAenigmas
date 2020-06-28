import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.image.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public class BoardShower
{
	private List<Board> boards = new Vector<Board>();
	private List<PathMaker> paths = new ArrayList<>();
	private GridFrame gf = null;

	private void processPaths() {
		for (Board b : boards) {
			paths.add(new PathMaker(b));
		}
	}



	private class MyListener implements GridPanel.MultiGridListener
	{
		private int boardindex = 0;
		private Board b() { return boards.get(boardindex); }
		private PathMaker path() { return paths.get(boardindex); }
		
		public int getNumXCells() { return b().width; }
		public int getNumYCells() { return b().height; }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public String[] getAnswerLines() { return new String[] { path().getPathString(),b().gfr.getVar("SOLUTION")}; }
		
		public boolean drawCellContents(int x,int y,BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();
			// insetting slightly
			int INSET = 2;
			int ulx = INSET;
			int uly = INSET;
			
			int w = bi.getWidth() - 2*INSET;
			int h = bi.getHeight() - 2*INSET;
						
			if (b().isTile(x,y))
			{
				g.setColor(Color.blue);
				g.fillRect(ulx,uly,w,h);
			}
			
			if (b().isTile(x,y))
			{
				if (b().isStart(x,y)) { GridPanel.DrawStringInCell(bi,Color.white,"S"); }
				if (b().isEnd(x,y)) { GridPanel.DrawStringInCell(bi,Color.white,"E");  }
				GridPanel.DrawStringInCorner(bi,path().onPath(x,y) ? Color.RED : Color.WHITE,""+path().getDistance(x,y), Direction.SOUTHEAST);
			}
			
			
			
			if (b().isTree(x,y))
			{
				g.setColor(Color.red);
				g.drawOval(ulx,uly,w,h);
			}
			
			if (b().hasLetter(x,y))
			{
				GridPanel.DrawStringInCell(bi,Color.black,"" + b().getLetter(x,y));
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

	public BoardShower(List<Board> boards)
	{
		this.boards = boards;
		processPaths();
		gf = new GridFrame("Adalogical Aenigma #16 Board",1024,768,new MyListener());
	}
	
	public BoardShower(Board board)
	{
		boards.add(board);
		processPaths();
		gf = new GridFrame("Adalogical Aenigma #16 Board",1024,768,new MyListener());
	}
	
	public void dispose() { gf.dispose(); }

}