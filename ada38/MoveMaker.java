import java.util.*;
import java.util.regex.*;
import java.io.*;


public class MoveMaker
{
	public static State.ActorInfo FindActor(State theState, String actorString)
	{
		String[] parts = actorString.split(" ");
		if (parts.length != 3) throw new RuntimeException("invalid Actor Designator " + actorString);
		int x = Integer.parseInt(parts[1]);
		int y = Integer.parseInt(parts[2]);
		State.ActorInfo result = theState.actorCurrentlyAt(x,y);
		if (result == null) throw new RuntimeException("actor designator does not point to an actor " + actorString);
		if (result.house != parts[0].charAt(0)) throw new RuntimeException("actor designator house mismatch " + actorString);
		return result;
	}
	
	public static void DoMoves(State theState,State.ActorInfo theActor,String moveString)
	{
		String[] moves = moveString.split(" ");
				
		for (String move : moves)
		{
			if (move.length() == 0) continue;
			char dir = move.charAt(0);
			if (dir == 'N') theState.Move(theActor,theActor.curx,theActor.cury-1);
			else if (dir == 'S') theState.Move(theActor,theActor.curx,theActor.cury+1);
			else if (dir == 'W') theState.Move(theActor,theActor.curx-1,theActor.cury);
			else if (dir == 'E') theState.Move(theActor,theActor.curx+1,theActor.cury);
			else throw new RuntimeException("Invalid move designator " + dir);
		}
	}

	public static void DoWas(State theState,State.ActorInfo theActor,String wasString)
	{
		String[] parts = wasString.split(" ");
		if (parts.length != 2) throw new RuntimeException("invalid Was Designator " + wasString);
		int x = Integer.parseInt(parts[0]);
		int y = Integer.parseInt(parts[1]);
		theState.SetWas(theActor,x,y);
	}
	


	public static void MoveMaker(String filename,State theState)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line = null;		
			
			while((line = reader.readLine()) != null)
			{	
				line = line.trim();
				System.out.println("Parsing line: " + line);
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;
				String[] parts = line.split("/");
				
				if (parts.length == 0) throw new RuntimeException("Invalid parting!");
				
				State.ActorInfo ai = FindActor(theState,parts[0]);
				if (parts.length == 1) continue;
				DoMoves(theState,ai,parts[1]);
				if (parts.length == 2) continue;
				for (int i = 2 ; i < parts.length ; ++i)
				{
					DoWas(theState,ai,parts[i]);
				}		
			}
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
				
							