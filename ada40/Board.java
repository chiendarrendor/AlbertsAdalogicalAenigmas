import org.w3c.dom.css.Rect;

import java.awt.*;
import java.util.Vector;

public class Board
{
	private GridFileReader gfr;
    private int width;
	private int height;
	private CellInfo[][] cells;
	private char[][] letters;

	private Vector<PotentialRectangle> potentials = new Vector<PotentialRectangle>();

	public boolean solved()
	{
		for(PotentialRectangle pr: potentials)
		{
			if (pr.rectangles.size() > 1) return false;
		}
		return true;
	}

	// returns true iff cell is on board and not WALL
	public boolean isCellEmpty(int x,int y)
	{
		if (x < 0 || x >= width) return false;
		if (y < 0 || y >= height) return false;
		if (getCellInfo(x,y).type == CellInfo.WALL) return false;
		return true;
	}



	public String getNumber(int x,int y) { return gfr.getBlock("NUMBERS")[x][y]; }

	public Board(Board right)
	{
		gfr = right.gfr;
		width = right.width;
		height = right.height;
		letters = right.letters;

		cells = new CellInfo[width][height];

		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				cells[x][y] = new CellInfo(right.cells[x][y]);
			}
		}

		for (PotentialRectangle pr : right.potentials)
		{
			potentials.add(new PotentialRectangle(pr));
		}

	}

	// choose the first potential with more than one rectangle.
	// make new boards where each rectangle is uniquely chosen
	public Vector<Board> successors()
	{
		Vector<Board> result = new Vector<Board>();
		PotentialRectangle pr = null;
		int i;
		for (i = 0 ; i < potentials.size() ; ++i)
		{
			pr = potentials.elementAt(i);
			if (pr.rectangles.size() > 1) break;
		}
		// if we are here, pr is the PotentialRectangle we are going to split, and
		// i is its index.

		for (Rectangle r : pr.rectangles)
		{
			Board nb = new Board(this);
			PotentialRectangle newpr = nb.potentials.elementAt(i);
			Vector<Rectangle> newvec = new Vector<Rectangle>();
			newvec.add(r);
			newpr.rectangles = newvec;
			if (nb.update() != LogicStatus.CONTRADICTION) result.add(nb);
		}
		return result;
	}




	public Board(GridFileReader gfr,RectangleList rl)
	{
		this.gfr = gfr;
		width = gfr.getWidth();
		height = gfr.getHeight();
		cells = new CellInfo[width][height];
		letters = new char[width][height];
		int rectId = 0;

		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				String s = gfr.getBlock("NUMBERS")[x][y];

				if (s.equals(".")) cells[x][y] = new CellInfo(CellInfo.UNKNOWN,-1,x,y);
				else
				{
					int size = Integer.parseInt(s);
					cells[x][y] = new CellInfo(CellInfo.RECTANGLE, rectId,x,y);
					potentials.add(new PotentialRectangle(x,y,size,rectId,rl,width,height));
					++rectId;
				}
				
				letters[x][y] = gfr.getBlock("LETTERS")[x][y].charAt(0);
			}
		}
	}

	public LogicStatus update()
	{
		LogicStatus result = LogicStatus.STYMIED;
		for(PotentialRectangle pr : potentials)
		{
			LogicStatus ls = pr.updateSelfAndBoard(this);
			if (ls == LogicStatus.CONTRADICTION) return ls;
			if (ls == LogicStatus.LOGICED) result = ls;
		}

		// detect a 2x2 of walls.
		for (int x = 0 ; x < width-1 ; ++x)
		{
			for (int y = 0 ; y < height - 1 ; ++y)
			{
				if (getCellInfo(x,y).type != CellInfo.WALL) continue;
				if (getCellInfo(x,y+1).type != CellInfo.WALL) continue;
				if (getCellInfo(x+1,y).type != CellInfo.WALL) continue;
				if (getCellInfo(x+1,y+1).type != CellInfo.WALL) continue;
				return LogicStatus.CONTRADICTION;
			}
		}




		// every potentialrectangle we know about should have at least one open diagonal corner
		for(PotentialRectangle pr : potentials)
		{
			if (pr.rectangles.size() > 1) continue;
			// this is a found rectangle.
			Rectangle r = pr.rectangles.elementAt(0);
			if (isCellEmpty(r.x-1,r.y-1)) continue;
			if (isCellEmpty(r.x+r.width,r.y-1)) continue;
			if (isCellEmpty(r.x-1,r.y+r.height)) continue;
			if (isCellEmpty(r.x+r.width,r.y+r.height)) continue;
			// if we get here, none of the corners are empty
			return LogicStatus.CONTRADICTION;
		}

		// detect a lack of connectivity in non-wall spaces
		BoardConnectivityCalculator bcc = new BoardConnectivityCalculator(this);
		if (!bcc.isConnected()) return LogicStatus.CONTRADICTION;




		return result;
	}


	public CellInfo getCellInfo(int x,int y) { return cells[x][y]; }
	public char getChar(int x,int y) { return letters[x][y]; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }

	public void showRectangleInfo()
	{
		for (PotentialRectangle pr : potentials)
		{
			System.out.print(pr.rectangles.size() + "\t");
		}
		System.out.println("");
	}
}
		