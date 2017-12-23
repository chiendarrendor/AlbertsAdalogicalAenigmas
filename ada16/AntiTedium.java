

// AntiTedium operates on a board thusly:
// for each row and each column:
// four tiles in a row -> CONTRADICTION
// three tiles in a row -> a tree must be on both ends -> LOGIC
// 2 . 2 , 2 . 1, 1 . 2 -> a tree must be in the middle -> LOGIC
// otherwise stymied

public class AntiTedium
{

	private static LogicStatus ApplyOne(Board b, int sx,int sy, int dx, int dy , int repeat)
	{
		int priorBlockCount = 0;
		int currentBlockCount = 0;
		int ltx = -1;
		int lty = -1;
		LogicStatus result = LogicStatus.STYMIED;
		
		for (int i = 0 ; i < repeat ; ++i)
		{
			int curx = sx + i * dx;
			int cury = sy + i * dy;
						
			if (b.isTile(curx,cury))
			{
				++currentBlockCount;
				if (currentBlockCount == 4) return LogicStatus.CONTRADICTION;
				
				if (currentBlockCount + priorBlockCount + 1 >= 4)
				{
					if (ltx != -1 && !b.isTree(ltx,lty))
					{
						b.addTree(ltx,lty);
						result = LogicStatus.LOGICED;
					}
				}
			}
			else
			{
				if (currentBlockCount == 3)
				{
					if (!b.isTree(curx,cury))
					{
						b.addTree(curx,cury);
						result = LogicStatus.LOGICED;
					}
				}
		
			
			
				ltx = curx;
				lty = cury;
				priorBlockCount = currentBlockCount;
				currentBlockCount = 0;
			}
		}
		return result;
	}
			
			





	public static LogicStatus ApplyAntiTedium(Board b)
	{
		LogicStatus result = LogicStatus.STYMIED;
		
		for (int x = 0 ; x < b.width ; ++x)
		{
			LogicStatus oner = ApplyOne(b,x,0,0,1,b.height);
			if (oner == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
			if (oner == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
		}
		
		for (int y = 0 ; y < b.height ; ++y)
		{
			LogicStatus oner = ApplyOne(b,0,y,1,0,b.width);
			if (oner == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
			if (oner == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
		}	
		return result;
	}
}