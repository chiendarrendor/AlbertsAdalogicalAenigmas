
import grid.file.GridFileReader;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class Ada30Puzzle
{
	

	public static class MyGridListener implements GridPanel.MultiGridListener,GridPanel.EdgeListener
	{
		Vector<Board> boards;
		int curboard;
		
		private void DrawOval(Graphics2D g,int cx,int cy,int width,int height, boolean isBlack)
		{
			int ulx = cx - width/2;
			int uly = cy - height/2;
			if (isBlack)
			{
				g.setColor(Color.black);
				g.fillOval(ulx,uly,width,height);
			}
			else
			{
				g.setColor(Color.white);
				g.fillOval(ulx,uly,width,height);
				g.setColor(Color.black);
				g.setStroke(new BasicStroke(2));
				g.drawOval(ulx,uly,width,height);
			}
		}
		
		
		
		public MyGridListener(Vector<Board> boards) { this.boards = boards; curboard = 0; }
		
		public int getNumXCells() { return boards.get(curboard).width; }
		public int getNumYCells() { return boards.get(curboard).height; }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public boolean drawCellContents(int cx,int cy, BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();
			Board theboard = boards.get(curboard);
			if (!theboard.isCellOn(cx,cy)) return false;
			if (theboard.dotLoc[cx][cy] == Circle.NONE) return false;
			if (theboard.cellColors[cx][cy] == DotColor.NONE) return false;
			boolean isBlack = theboard.cellColors[cx][cy] == DotColor.BLACK;
			int loc = theboard.dotLoc[cx][cy];
			int ulx = 0;
			int uly = 0;
			int cenx = bi.getWidth()/2;
			int ceny = bi.getHeight()/2;
			int lrx = bi.getWidth();
			int lry = bi.getHeight();
			int ow = bi.getWidth()/2;
			int oh = bi.getHeight()/2;
			if (loc == Circle.NW) DrawOval(g,ulx,uly,ow,oh,isBlack);
			if (loc == Circle.N)  DrawOval(g,cenx,uly,ow,oh,isBlack);
			if (loc == Circle.NE) DrawOval(g,lrx,uly,ow,oh,isBlack);
			if (loc == Circle.W) DrawOval(g,ulx,ceny,ow,oh,isBlack);
			if (loc == Circle.CENTER) DrawOval(g,cenx,ceny,ow,oh,isBlack);
			if (loc == Circle.E) DrawOval(g,lrx,ceny,ow,oh,isBlack);
			if (loc == Circle.SW) DrawOval(g,ulx,lry,ow,oh,isBlack);
			if (loc == Circle.S) DrawOval(g,cenx,lry,ow,oh,isBlack);
			if (loc == Circle.SE) DrawOval(g,lrx,lry,ow,oh,isBlack);
			
			return true;
		}
		
		public boolean hasNext() { return curboard < boards.size() - 1; }
		public boolean hasPrev() { return curboard > 0; }
		public void moveToNext() { ++curboard; }
		public void moveToPrev() { --curboard; }	
		
		public GridPanel.EdgeListener.EdgeDescriptor onBoundary()
		{
			return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,1);
		}
		
		public GridPanel.EdgeListener.EdgeDescriptor toEast(int x,int y)
		{
			int w = boards.get(curboard).cellIds[x][y] == boards.get(curboard).cellIds[x+1][y] ? 1 : 5;
			return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,w);
		}	
	
		public GridPanel.EdgeListener.EdgeDescriptor toSouth(int x,int y)
		{
			int w = boards.get(curboard).cellIds[x][y] == boards.get(curboard).cellIds[x][y+1] ? 1 : 5;
			return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,w);
		}
	}
	
	

	public static void main(String[] args)
	{
		Vector<Board> boards = new Vector<Board>();
		Board floorBoard = null;
		File cwd = new File(".");
		
		for (File fent : cwd.listFiles())
		{
			if (!fent.getName().endsWith(".txt")) continue;
			System.out.println("Loading file: " + fent.getName());
			
			Board nb = new Board(new GridFileReader(fent.getName(),new String[]{"CELLS","COLORS","DIRS"}));
			if (fent.getName().equals("Floor.txt")) floorBoard = nb;
			else boards.add(nb);
		}
		
		Solver sol = new Solver(floorBoard,boards);

		
		MyGridListener mgl = new MyGridListener(sol.solutions);
		GridFrame gf = new GridFrame("Adalogical Aenigma #30 Puzzle Part", 1300,768,mgl,mgl);
		
		BoardWriter.Write(sol.solutions.firstElement(),"solution.dat");
		
	}
}