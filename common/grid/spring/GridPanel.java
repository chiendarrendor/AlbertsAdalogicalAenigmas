package grid.spring;

import grid.puzzlebits.Direction;

import java.awt.*;
import java.awt.image.*;

public class GridPanel extends FixedSizePanel
{
	// the presence of an edgeListener means that the 
	// client wants to mediate internal edge drawing themselves.
	// as such, GridListener's 'drawGridLines' will be considered false.
	GridListener listener = null;
	EdgeListener edgeListener = null;

	private boolean isDrawGridLines()
	{
		if (edgeListener != null) return false;
		return listener.drawGridLines();
	}
	
	private boolean isDrawBoundary()
	{
		if (edgeListener != null) return false;
		return listener.drawBoundary();
	}
	
	
	public interface GridListener
	{
		int getNumXCells();
		int getNumYCells();
		boolean drawGridNumbers();
		boolean drawGridLines();
		boolean drawBoundary();
		boolean drawCellContents(int cx,int cy, BufferedImage bi); // bi provides width,height,and a graphics.
		String[] getAnswerLines();
	}
	
	public interface MultiGridListener extends GridListener
	{
		// this interface extension can be used if there are multiple grids to display.
		// if an object of this sub-interface is passed in, buttons will appear for moving
		// between grids.
		// hasNext should be true iff moveToNext() would be sensical
		// hasPrev should be true iff moveToPrev() would be sensical
		boolean hasNext();
		void moveToNext();
		boolean hasPrev();
		void moveToPrev();	
	}
	
	public interface EdgeListener
	{
		public class EdgeDescriptor
		{
			Color color;
			int width;
			public EdgeDescriptor(Color c,int w) { color = c; width = w; }
			
			public void set(Graphics2D g)
			{
				if (color == null) throw new RuntimeException("Why did you give me an EdgeDescriptor with no color?");
				g.setColor(color);
				g.setStroke(new BasicStroke(width));
			}
		}
		
		EdgeDescriptor onBoundary();
		EdgeDescriptor toEast(int x,int y);
		EdgeDescriptor toSouth(int x, int y);
	}

	public static boolean DrawStringInCell(BufferedImage bi,Color c,String s)
	{
		return DrawStringInCell((Graphics2D)bi.getGraphics(),c,0,0,bi.getWidth(),bi.getHeight(),s);
	}
	
	public static boolean DrawStringUpperLeftCell(BufferedImage bi,Color c,String s)
	{
		return DrawStringUpperLeftCell((Graphics2D)bi.getGraphics(),c,0,0,bi.getWidth(),bi.getHeight(),s);
	}	

	public static boolean DrawStringInCorner(BufferedImage bi,Color c,String s,Direction d)
	{
		return DrawStringInCorner((Graphics2D)bi.getGraphics(),c,0,0,bi.getWidth(),bi.getHeight(),s,d);
	}

	
	public static boolean DrawStringUpperLeftCell(Graphics2D g,Color c,int ulx,int uly, int width,int height, String s)
	{
		return DrawStringInCorner(g,c,ulx,uly,width,height,s,Direction.NORTHWEST);
	}

	public static boolean DrawStringInCorner(Graphics2D g, Color c, int ulx, int uly, int width, int height, String s, Direction d)
	{
		g.setColor(c);
		int sHeight = g.getFontMetrics().getHeight();
		int sWidth = g.getFontMetrics().stringWidth(s);
		if (sHeight > height || sWidth > width) return false;

		int sx = 0;
		int sy = 0;
		int INSET = 3;

		switch (d)
		{
			case NORTHWEST:
			case WEST:
			case SOUTHWEST:
				sx = ulx + INSET;
				break;
			case NORTH:
			case SOUTH:
				sx = ulx + width/2 - sWidth/2;
				break;
			case NORTHEAST:
			case EAST:
			case SOUTHEAST:
				sx = ulx + width - sWidth - INSET;
				break;
		}

		switch(d)
		{
			case NORTHWEST:
			case NORTH:
			case NORTHEAST:
				sy = uly + sHeight;
				break;
			case WEST:
			case EAST:
				sy = uly + height/2 + sHeight/2;
				break;
			case SOUTHWEST:
			case SOUTH:
			case SOUTHEAST:
				sy = uly + height - INSET ;
				break;
		}

		g.drawString(s,sx,sy);
		return true;
	}



	
	
	
	public static boolean DrawStringInCell(Graphics2D g,Color c,int ulx,int uly, int width,int height, String s)
	{
		g.setColor(c);
		int sHeight = g.getFontMetrics().getHeight();
		int sWidth = g.getFontMetrics().stringWidth(s);
		if (sHeight > height || sWidth > width) return false;
		int cx = ulx + width/2;
		int cy = uly + height/2;
		int sx = cx - sWidth/2;
		int sy = cy + sHeight/2;
		g.drawString(s,sx,sy);
		return true;
	}

	public class DrawParams
	{
		int INSET = 50;
		int numXCells = listener.getNumXCells();
		int numYCells = listener.getNumYCells();
		int screenWidth = getFixedWidth();
		int screenHeight = getFixedHeight();
		int cellWidth = (screenWidth - 2 * INSET) / numXCells;
		int cellHeight = (screenHeight - 2 * INSET) / numYCells;

		public void repaint() { GridPanel.this.repaint(); }
	}
	
	private DrawParams params;
	public DrawParams getParams() { return params; }
	
	
	private void DrawCell(Graphics2D g,int cw,int ch,int x,int y)
	{
		BufferedImage bi = new BufferedImage(cw-1,ch-1,BufferedImage.TYPE_INT_RGB);
		Graphics2D big = (Graphics2D)bi.getGraphics();
		big.setColor(getBackground());
		big.fillRect(0,0,bi.getWidth(),bi.getHeight());
		if (!listener.drawCellContents(x,y,bi)) return;
		g.drawImage(bi,params.INSET + x * cw+1,params.INSET + y * ch+1,getBackground(),null);
	}

	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		g.setColor(getBackground());
		g.fillRect(0,0,getFixedWidth(),getFixedHeight());

		// assume that the numbers can be drawn in theparams.INSET space

		g.setColor(Color.black);

		for (int i = 0; i <= params.numXCells; ++i)
		{
			if (((i == 0 || i == params.numXCells) && isDrawBoundary()) ||
				((i > 0 && i < params.numXCells) && isDrawGridLines()))
			{
				g.drawLine(params.INSET + i * params.cellWidth ,params.INSET ,params.INSET + i * params.cellWidth ,params.INSET + params.numYCells * params.cellHeight);
			}
			
			if (i != params.numXCells && listener.drawGridNumbers()) DrawStringInCorner((Graphics2D)g,Color.black,params.INSET + i * params.cellWidth,params.INSET-params.cellHeight,params.cellWidth,params.cellHeight,""+i,Direction.SOUTH);
		}
		
		for (int i = 0 ; i <= params.numYCells ; ++i)
		{
			if (((i == 0 || i == params.numYCells) && isDrawBoundary()) ||
				((i > 0 && i < params.numYCells) && isDrawGridLines()))
			{
				g.drawLine(params.INSET ,params.INSET + i * params.cellHeight ,params.INSET + params.numXCells * params.cellWidth,params.INSET + i * params.cellHeight);
			}
			
			if (i != params.numYCells && listener.drawGridNumbers()) DrawStringInCorner((Graphics2D)g,Color.black,params.INSET - params.cellWidth,params.INSET + i * params.cellHeight,params.cellWidth,params.cellHeight,""+i,Direction.EAST);
		}

		for (int x = 0; x < params.numXCells; ++x)
		{
			for (int y = 0 ; y < params.numYCells ; ++y)
			{
				DrawCell(g2d,params.cellWidth,params.cellHeight,x,y);
			}
		}		
		
		
		if (edgeListener != null)
		{
			EdgeListener.EdgeDescriptor bed = edgeListener.onBoundary();
			if (bed != null)
			{
				bed.set(g2d);
				g.drawLine(params.INSET,params.INSET,params.INSET,params.INSET+params.numYCells * params.cellHeight);
				g.drawLine(params.INSET,params.INSET,params.INSET+ params.numXCells * params.cellWidth,params.INSET);
				g.drawLine(params.INSET+ params.numXCells * params.cellWidth,params.INSET,params.INSET+ params.numXCells * params.cellWidth,params.INSET+params.numYCells * params.cellHeight);
				g.drawLine(params.INSET,params.INSET+params.numYCells * params.cellHeight,params.INSET+ params.numXCells * params.cellWidth,params.INSET+params.numYCells * params.cellHeight);
			}
			
			for (int x = 0; x < params.numXCells; ++x)
			{
				for (int y = 0 ; y < params.numYCells ; ++y)
				{
					if (x < params.numXCells -1)
					{
						EdgeListener.EdgeDescriptor eed = edgeListener.toEast(x,y);
						if (eed != null)
						{
							eed.set(g2d);
							g.drawLine(params.INSET + (x+1) * params.cellWidth,params.INSET+y*params.cellHeight,params.INSET + (x+1) * params.cellWidth,params.INSET+(y+1)*params.cellHeight);
						}
					}
					if (y < params.numYCells - 1)
					{
						EdgeListener.EdgeDescriptor sed = edgeListener.toSouth(x,y);
						if (sed != null)
						{
							sed.set(g2d);
							g.drawLine(params.INSET + x * params.cellWidth,params.INSET+(y+1)*params.cellHeight,params.INSET + (x+1) * params.cellWidth,params.INSET+(y+1)*params.cellHeight);
						}
					}
					
					
				}
			}
		}
	}
		
	public GridPanel(int width,int height,GridListener listener,EdgeListener edgeListener)
	{
		super(width,height);
		this.listener = listener;
		this.edgeListener = edgeListener;
		params = new DrawParams();
	}
		
		
	public GridPanel(int width, int height,GridListener listener)
	{
		this(width,height,listener,null);
	}
}

		