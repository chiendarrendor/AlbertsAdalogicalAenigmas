
import java.util.*;
import java.util.regex.*;
import java.awt.Point;
import java.io.*;

public class Board
{
	int width;
	int height;
	
	enum CellType { TARGET, NONTARGET, UNKNOWN, PATH };
	enum ResultType { LOGIC, STYMIED, CONTRADICTION };
	// notes: 
	//   all TARGETs must become TOKENs before the board can be done
	//   all TOKENs must be moved (have a non-null tokenOriginalPosition) before the board can be done
	//   a TOKEN may move through UNKNOWN and NONTARGET only
	//   PATH is placed on every cell a TOKEN moves through (including initial)
	//   TOKEN may only land on TARGET and UNKNOWN
	//   TARGET is placed RMI (rotational-mirror-image) from a moved TOKEN
	//   NONTARGET is placed RMI from PATH

	class CellInfo
	{
		CellType type = CellType.UNKNOWN;
		boolean containsToken = false;
		int tokenMoveDist = -1; // -1 if unspecified
		char tokenCode = '@'; // '@' if unspecified
		Point tokenOriginalPosition = null; // null if token has not been moved yet.
		Point tokenCurrentPosition = null;
		
		public CellInfo() {}
		public CellInfo(CellType type) { this.type = type; }
		public CellInfo(int dist,char code, int x,int y) 
		{ 
			containsToken = true;
			tokenMoveDist = dist; 
			tokenCode = code; 
			tokenCurrentPosition = new Point(x,y);
		}
		public CellInfo(CellInfo right)
		{
			type = right.type;
			containsToken = right.containsToken;
			tokenMoveDist = right.tokenMoveDist;
			tokenCode = right.tokenCode;
			tokenOriginalPosition = right.tokenOriginalPosition == null ? null : new Point(right.tokenOriginalPosition.x,right.tokenOriginalPosition.y);
			tokenCurrentPosition = right.tokenCurrentPosition == null ? null : new Point(right.tokenCurrentPosition.x,right.tokenCurrentPosition.y);
		}
		
		public String tokenString()
		{
			StringBuffer sb = new StringBuffer();
			if (tokenMoveDist != -1) sb.append(tokenMoveDist);
			if (tokenCode != '@') sb.append(tokenCode);
			return sb.toString();
		}
		
		public boolean blocksPath() { return type == CellType.PATH || containsToken; }
		public boolean endsPath() { return type == CellType.TARGET; }
		public boolean cantStop() { return type == CellType.NONTARGET; }
		public boolean isPotentialTarget(int curcount) { return tokenMoveDist == -1 || tokenMoveDist == curcount; }
		public void makeNonTarget() { if (type != CellType.PATH) type = CellType.NONTARGET; }
	}
	
	class DrawVector
	{
		boolean pendown;
		Vector<CellInfo> tokens = new Vector<CellInfo>();
		
		public Point getVector()
		{
			Point result = new Point(0,0);
			for (CellInfo token : tokens)
			{
				result.x += token.tokenCurrentPosition.x - token.tokenOriginalPosition.x;
				result.y += token.tokenCurrentPosition.y - token.tokenOriginalPosition.y;
			}
			return result;
		}
		
		public DrawVector() {}
		public DrawVector(DrawVector right)
		{
			pendown = right.pendown;
			for (CellInfo rci : right.tokens)
			{
				tokens.add(cells[rci.tokenCurrentPosition.x][rci.tokenCurrentPosition.y]);
			}
		}
	}
	
	
	
	
	CellInfo cells[][] = null;
	Vector<DrawVector> vectors = new Vector<DrawVector>();
	Vector<CellInfo> tokens = new Vector<CellInfo>();
	Vector<String> why = new Vector<String>();
	
	public Board(Board right)
	{
		width = right.width;
		height = right.height;
		cells = new CellInfo[width][height];
		for (int y = 0 ; y < height ; ++y)
		{
			for (int x = 0 ; x < width ; ++x)
			{
				cells[x][y] = new CellInfo(right.cells[x][y]);
				if (cells[x][y].containsToken) tokens.add(cells[x][y]);
			}
		}
		
		for (DrawVector dv : right.vectors)
		{
			vectors.add(new DrawVector(dv));
		}
		
		for (String s : right.why)
		{
			why.add(s);
		}
		
	}
	
	
	
	
	public boolean isOnBoard(Point p) { return isOnBoard(p.x,p.y); }
	public boolean isOnBoard(int x,int y) { return x >= 0 && x < width && y >= 0 && y < height; }
	public Point GetRMIPoint(Point p) { return new Point(width - 1 - p.x,height - 1 - p.y); }
	public CellInfo GetRMICell(Point p) { Point rp = GetRMIPoint(p); return cells[rp.x][rp.y]; }
	
	public void MoveToken(CellInfo tokcell,Point target, Point delta, String why)
	{		
		this.why.add("Token (" + tokcell.tokenCurrentPosition.x + "," + tokcell.tokenCurrentPosition.y + ") moved to (" +
					target.x + "," + target.y + ") because " + why);
	

		// special processing for non-move case
		if (target.equals(tokcell.tokenCurrentPosition))
		{
			tokcell.type = CellType.TARGET;
			GetRMICell(tokcell.tokenCurrentPosition).type = CellType.TARGET;
			tokcell.tokenOriginalPosition = tokcell.tokenCurrentPosition;
			return;
		}
		
		// because we have a vector of token cells, when we move a token, we actually have to move the object
		// start by replacing the Cell with a new one.
		Point orig = tokcell.tokenCurrentPosition;
		cells[orig.x][orig.y] = new CellInfo(CellType.PATH);
		GetRMICell(orig).makeNonTarget();
		
		// modify the cells along the path, and along the RMI reflected path
		int index = 1;
		Point curPoint = null;
		while(true)
		{
			curPoint = new Point(orig.x + index * delta.x,orig.y + index * delta.y);
			if (curPoint.equals(target)) break;
			cells[curPoint.x][curPoint.y].type = CellType.PATH;
			GetRMICell(curPoint).makeNonTarget();
			++index;
		}
	
		// finally, fix the properties of our token's cellinfo to be right in the current place.
		cells[curPoint.x][curPoint.y] = tokcell;
		tokcell.tokenOriginalPosition = orig;
		tokcell.tokenCurrentPosition = curPoint;
		tokcell.type = CellType.TARGET;
		GetRMICell(curPoint).type = CellType.TARGET;
	}
			
			
	
	
	
	
	
	
	boolean isSolution()
	{
		for (CellInfo tokcell : tokens)
		{
			if (tokcell.type != CellType.TARGET) return false;
			if (tokcell.tokenOriginalPosition == null) return false;
		}
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				if (cells[x][y].type == CellType.TARGET && !cells[x][y].containsToken) return false;
			}
		}
	
		return true;
	}
	
	Point[] deltas = new Point[] { new Point(1,0),new Point(-1,0),new Point(0,1),new Point(0,-1) };
	
	private static class PointPair
	{
		CellInfo token;
		Point dest;
		Point delta;
		public PointPair(CellInfo token, Point dest,Point delta) { this.token = token; this.dest = dest; this.delta = delta; }
	}
	
	// we will assume that this CellInfo is 
	// a) contains a token
	// b) contains a token that has not yet moved.
	public Vector<PointPair> TokenSuccessors(CellInfo tokcell)
	{
		Vector<PointPair> result = new Vector<PointPair>();
		Point origin = tokcell.tokenCurrentPosition;
				
		if (!tokcell.cantStop() && tokcell.isPotentialTarget(0)) result.add(new PointPair(tokcell,origin,null));
		if (tokcell.endsPath()) return result;
		
		for (Point delta : deltas)
		{
			int ctr = 1;
			while(true)
			{
				Point newP = new Point(origin.x + ctr * delta.x,origin.y + ctr * delta.y);
				
				if (!isOnBoard(newP))
				{
					break;
				}
				
				CellInfo here = cells[newP.x][newP.y];
				if (here.blocksPath()) 
				{
					break;
				}
				
				if (here.cantStop())
				{
				}
				else if (tokcell.isPotentialTarget(ctr))
				{
					result.add(new PointPair(tokcell, newP,delta));
				}
				
				if (here.endsPath())
				{
					break;
				}

				++ctr;
			}
		}	
		return result;
	}
	
	public ResultType UniqueMoves()
	{
		ResultType result = ResultType.STYMIED;
		// the processing for every token cell starts independently, so we can do all of them in one run
		for (CellInfo tokcell : tokens)
		{			
			if (tokcell.tokenOriginalPosition != null)
			{
				continue;
			}

			Vector<PointPair> successors = TokenSuccessors(tokcell);
			
			if (successors.size() == 0) 
			{
				return ResultType.CONTRADICTION;
			}
			
			if (successors.size() == 1)
			{
				PointPair pp = successors.firstElement();
				MoveToken(tokcell,pp.dest,pp.delta, "token could only move one place");
				result = ResultType.LOGIC;
			}
		}
		return result;
	}
	
	// this stops after the first found target, because the move validity may then become wrong for subsequent ones.
	public ResultType UniqueTargets()
	{
		Map<Point,Vector<PointPair>> bytarget = new HashMap<Point,Vector<PointPair>>();
		
		for (CellInfo tokcell : tokens)
		{
			if (tokcell.tokenOriginalPosition != null) continue;
			Vector<PointPair> successors = TokenSuccessors(tokcell);
			for (PointPair pp : successors)
			{
				if (!bytarget.containsKey(pp.dest)) { bytarget.put(pp.dest,new Vector<PointPair>()); }
				bytarget.get(pp.dest).add(pp);
			}
		}
		
		for (int x = 0 ; x < width ; ++x)
		{
			for (int y = 0 ; y < height ; ++y)
			{
				Point p = new Point(x,y);
				CellInfo target = cells[x][y];
				if (target.type != CellType.TARGET) continue;
				if (target.containsToken) continue;
				if (!bytarget.containsKey(p)) return ResultType.CONTRADICTION;
				
				Vector<PointPair> landers = bytarget.get(p);
				if (landers.size() > 1) continue;
			
				PointPair lander = landers.firstElement();
				MoveToken(lander.token,lander.dest,lander.delta, "Target could be fulfilled from only one place");
				return ResultType.LOGIC;
			}
		}
		return ResultType.STYMIED;
	}
	
	public ResultType DoLogic()
	{
		ResultType umstatus = UniqueMoves();
		if (umstatus == ResultType.CONTRADICTION) return ResultType.CONTRADICTION;
		ResultType utstatus = UniqueTargets();
		if (utstatus == ResultType.CONTRADICTION) return ResultType.CONTRADICTION;
		if (umstatus == ResultType.LOGIC || utstatus == ResultType.LOGIC) return ResultType.LOGIC;
		return ResultType.STYMIED;
	}
	
	public Vector<Board> Guesses()
	{
		int mincount = width + height + 2; // larger than the largest possible # of spaces a given token could move to
		Vector<PointPair> successors = null;
		Vector<Board> result = new Vector<Board>();
		
		for (CellInfo tokcell : tokens)
		{
			if (tokcell.tokenOriginalPosition != null) continue;
			Vector<PointPair> mySuccessors = TokenSuccessors(tokcell);
			if (mySuccessors.size() < mincount) successors = mySuccessors;
		}
		if (successors == null) return result;
		
		for (PointPair pp : successors)
		{
			Board newB = new Board(this);
			Point origLoc = pp.token.tokenCurrentPosition;
			CellInfo newTokCell = newB.cells[origLoc.x][origLoc.y];
			newB.MoveToken(newTokCell,pp.dest,pp.delta, "I guess?");
			result.add(newB);
		}
		return result;
	}
		
	
	
	
	
	
	
	
	public Board(String filename)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line = null;
			int ycount = 0;
			Pattern pattern = Pattern.compile("^([0-9]+)?([A-Z])?$");
			Pattern vpatterndown = Pattern.compile("^[A-Z]+$");
			Pattern vpatternup = Pattern.compile("^\\(([A-Z]+)\\)$");
			Map<Character,CellInfo> byCode = new HashMap<Character,CellInfo>();
			
			while((line = reader.readLine()) != null)
			{	
				line = line.trim();
				if (cells == null)
				{
					String[] dims = line.split(" ");
					if (dims.length != 2) throw new RuntimeException("first line does not contain two ints");
					width = Integer.parseInt(dims[0]);
					height = Integer.parseInt(dims[1]);
					cells = new CellInfo[width][height];
				}
				else if (ycount < height)
				{
					String[] lineEnts = line.split(" +");
					if (lineEnts.length != width) throw new RuntimeException("wrong # of elements " + line);
					for (int x = 0 ; x < width ; ++x)
					{
						if (lineEnts[x].equals(".")) cells[x][ycount] = new CellInfo();
						else if (lineEnts[x].equals("@")) 
						{
							cells[x][ycount] = new CellInfo(-1,'@',x,ycount);
							tokens.add(cells[x][ycount]);
						}
						else
						{
							Matcher m = pattern.matcher(lineEnts[x]);
							if (!m.find()) throw new RuntimeException("bad line element " + lineEnts[x]);
							int dist = -1;
							char code = '@';
							if (m.group(1) != null && !m.group(1).equals("")) dist = Integer.parseInt(m.group(1));
							if (m.group(2) != null && !m.group(2).equals("")) code = m.group(2).charAt(0);
							cells[x][ycount] = new CellInfo(dist,code,x,ycount);
							
							if (code != '@') byCode.put(code,cells[x][ycount]);
							
							tokens.add(cells[x][ycount]);
						}
					}
					++ycount;
				}
				else
				{
					String[] lineparts = line.split(" ");
					for (String rawel : lineparts)
					{
						DrawVector dv = new DrawVector();
						vectors.add(dv);
						Matcher mdown = vpatterndown.matcher(rawel);
						Matcher mup = vpatternup.matcher(rawel);
						String el = null;
						if (mdown.find())
						{
							el = rawel;
							dv.pendown = true;
						}
						else if (mup.find())
						{
							el = mup.group(1);
							dv.pendown = false;
						}
						else
						{
							throw new RuntimeException("Illegal vector element " + rawel);
						}
						
						for(char c : el.toCharArray())
						{
							if (!byCode.containsKey(c)) throw new RuntimeException("No such token marked " + c);
							dv.tokens.add(byCode.get(c));
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
		