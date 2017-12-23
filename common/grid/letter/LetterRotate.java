package grid.letter;

public class LetterRotate
{
	// c is a letter between A and Z inclusive
	// dist is any number, positive or negative
	public static char Rotate(char c,int dist)
	{
		char result = c;
		
		if (dist < 0)
		{
			while(dist != 0)
			{
				if (result == 'A')
				{
					result = 'Z';
				}
				else
				{
					--result;
				}
				++dist;
			}
		}
		else if (dist > 0)
		{
			while(dist != 0)
			{
				if (result == 'Z')
				{
					result = 'A';
				}
				else
				{
					++result;
				}
				--dist;
			}
		}
		return result;
	}
}