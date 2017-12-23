import java.util.*;

public class WitnessBoardAI
{
	// so, a path can be added to result if
	// a) the path tail vertex is an End Sigil
	// b) the path has passed through exactly throughcount through Vertices
	private static void AddToResultIfDone(Vector<Path> result,Path path,int throughcount)
	{
		if (!VertexSigils.HasEndSigil(path.tailVertex())) return;
		if (path.getThroughCount() != throughcount) return;
		result.add(path);
	}
	
	// so a path can be added back to the queue if either
	// a) we are not on a terminal vertex, or
	// b) we are on a terminal vertex, but we haven't discovered endcount ends yet.
	private static void AddToQueueIfNotDone(Vector<Path> queue,Path path,int endcount)
	{
		if (VertexSigils.HasEndSigil(path.tailVertex()) &&
			path.getEndCount() == endcount) return;
		queue.add(path);
	}
	
	private static void PrintQueue(Vector<Path> queue)
	{
		System.out.println("Queue Size: " + queue.size());
		for (Path p : queue)
		{
			System.out.println("\t" + p.toString());
		}
	}
	
	public static Vector<WitnessBoard.Space> GetConnectedSpaces(WitnessBoard wb, Path p, WitnessBoard.Space ispace)
	{
		Set<Coordinate> seenSpaces = new HashSet<Coordinate>();
		Vector<Coordinate> pending = new Vector<Coordinate>();
		Vector<WitnessBoard.Space> result = new Vector<WitnessBoard.Space>();
		
		pending.add(ispace.location);
		while (pending.size() > 0)
		{
			Coordinate active = pending.remove(0);
			WitnessBoard.Space curSpace = wb.getSpace(active);
			if (seenSpaces.contains(active)) continue;
			result.add(curSpace);
			seenSpaces.add(active);
			
			WitnessBoard.Edge[] adjedges = curSpace.adjacentEdges();
			for (int i = 0 ; i < 4 ; ++i)
			{
				if (p.hasEdge(adjedges[i])) continue;
				WitnessBoard.Space ospace = adjedges[i].GetOtherSpace(curSpace);
				if (ospace == null) continue;
				if (seenSpaces.contains(ospace.location)) continue;
				pending.add(ospace.location);
			}
		}
		return result;
	}
	
	private static void PrintConnectedSpaces(WitnessBoard wb, Path p,int x,int y)
	{
		WitnessBoard.Space thespace = wb.getSpace(x,y);
		Vector<WitnessBoard.Space> spaces = GetConnectedSpaces(wb,p,thespace);
		System.out.print("Connected Spaces of Space " + thespace.location + ":");
		for (WitnessBoard.Space s : spaces) { System.out.print(" " +s.location); }
		System.out.println("");
	}
	
	
	private static Vector<Path> FindRawPaths(WitnessBoard wb)
	{
		Vector<Path> result = new Vector<Path>();
		Vector<Path> queue = new Vector<Path>();
		
		int endcount = 0;
		int throughcount = 0;
		for (WitnessBoard.Vertex v : wb.getVertices())
		{
			if (VertexSigils.HasEndSigil(v)) { ++endcount; }
			if (VertexSigils.HasThroughSigil(v)) { ++throughcount; }
		}		

		// if there are no endpoints at all, we have no answers.
		if (endcount == 0) return result;
				
		for (WitnessBoard.Vertex v : wb.getVertices())
		{
			if (VertexSigils.HasStartSigil(v)) 
			{
				Path newPath = new Path(v);
				if (VertexSigils.HasEndSigil(v)) newPath.incrementEndCount();
				if (VertexSigils.HasThroughSigil(v)) newPath.incrementThroughCount();
				
				AddToResultIfDone(result,newPath,throughcount);
				AddToQueueIfNotDone(queue,newPath,endcount);
			}
		}
		
		// invariant: An path in the queue is one that can theoretically be explored from its end; is not
		// on a (final) goal space.
		while(queue.size() > 0)
		{
//			PrintQueue(queue);
			Path curPath = queue.remove(0);
			WitnessBoard.Vertex tail = curPath.tailVertex();
			
			Vector<WitnessBoard.Edge> successors = tail.adjacentEdges();
			for (WitnessBoard.Edge e : successors)
			{
				// is this the backtrack direction?
				if (curPath.hasEdge(e)) continue;
				// is this a blocked direction? (special hardcoded Sigil case)
				if (EdgeSigils.HasBlockerSigil(e)) continue;
				// if we get here, it's a valid direction...get the other vertex.
				WitnessBoard.Vertex overtex = e.GetOtherVertex(tail);
				// is this other vertex already in the path?
				if (curPath.hasVertex(overtex)) continue;
				
				// make a new path, and extend it.
				Path newPath = new Path(curPath);
				newPath.add(e,overtex);
				
				if (VertexSigils.HasEndSigil(overtex)) newPath.incrementEndCount();
				if (VertexSigils.HasThroughSigil(overtex)) newPath.incrementThroughCount();
				
				AddToResultIfDone(result,newPath,throughcount);
				AddToQueueIfNotDone(queue,newPath,endcount);				
			}
		}
		return result;
	}

	private static boolean ValidateSpaceSigils(WitnessBoard wb, Path path)
	{
		for (WitnessBoard.Space s : wb.getSpaces())
		{
			for (WitnessBoard.SpaceSigil ss : s.sigils)	
			{
				if (!ss.ValidateBoard(wb,s,path)) return false;
			}
		}
		return true;
	}
		
	public static Vector<Path> FindPaths(WitnessBoard wb)
	{
		Vector<Path> rpaths = FindRawPaths(wb);
		Vector<Path> result = new Vector<Path>();

		System.out.println("Number of raw solutions: " + rpaths.size());

		
		for (Path path : rpaths)
		{
			if (ValidateSpaceSigils(wb,path)) { result.add(path); }
		}
		System.out.println("Number of solutions: " + result.size());
		
		return result;	
	}
}