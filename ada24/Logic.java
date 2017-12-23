
import java.util.*;
import java.awt.Point;

public class Logic
{	
	private class RequiredMirrorStep implements LogicStep<State>
	{
		public LogicStatus apply(State s)
		{
			LogicStatus result = LogicStatus.STYMIED;
			
			for (Board.Region region : s.myBoard.regionsById.values())
			{
				int nummirrors = 0;
				int numholes = 0;
				Point holepoint = null;
				for (Point p : region.cells)
				{
					if (s.state[p.x][p.y].isMirror()) ++nummirrors;
					if (s.state[p.x][p.y] == CellState.UNKNOWN)
					{
						++numholes;
						holepoint = p;
					}
				}
				if (nummirrors > 0) continue;
				if (numholes == 0)
				{
//					System.out.println("Region " + region.id + " has no space for a mirror");
					return LogicStatus.CONTRADICTION;
				}
				if (numholes > 1) continue;
				// so, if we get here, nummirrors = 0 and numholes = 1...means that the hole must be a mirror
				s.state[holepoint.x][holepoint.y] = CellState.GENERICMIRROR;
				result = LogicStatus.LOGICED;
			}
			return result;
		}
	}
	
	private class UniqueMirrorStep implements LogicStep<State>
	{
		public LogicStatus apply(State s)
		{
			LogicStatus result = LogicStatus.STYMIED;
			
			for (Board.Region region : s.myBoard.regionsById.values())
			{
				int nummirrors = 0;
				for (Point p : region.cells)
				{
					if (s.state[p.x][p.y].isMirror()) ++nummirrors;
				}
				if (nummirrors > 1) 
				{
//					System.out.println("Region has too many mirrors!");
					return LogicStatus.CONTRADICTION;
				}
				if (nummirrors == 0) continue;
				// we have one mirror...all other spaces must be EMPTY
				for (Point p: region.cells)
				{
					if (s.state[p.x][p.y].isMirror() || s.state[p.x][p.y] == CellState.EMPTY) continue;
					result = LogicStatus.LOGICED;
					s.state[p.x][p.y] = CellState.EMPTY;
				}
			}
			return result;
		}
	}
	
	private class BeamStep implements LogicStep<State>
	{

		public LogicStatus apply(State s)
		{
			LogicStatus result = LogicStatus.STYMIED;
			for (Board.Path path : s.myBoard.paths)
			{
                            if (s.fired(path)) continue;
				if (path.numMirrors < 3)
				{
                                    FireField ff = new FireField(s,path,path.p1);
//                                    System.out.println("path: " + path.id + " num paths: " + ff.paths.size());
                                    if (ff.paths.size() == 0) return LogicStatus.CONTRADICTION;
                                    if (ff.paths.size() > 1) continue;
                                    LogicStatus ls = ff.paths.firstElement().ApplyPath(s);
                                    s.Fire(path);
                                    if (ls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
				}
			}
			return result;
		}
	}
	
	
	
	
	public Logic(Logicer<State> log)
	{
		log.addLogicStep(new RequiredMirrorStep());
		log.addLogicStep(new UniqueMirrorStep());
		log.addLogicStep(new BeamStep());
	}
	
}
