
import java.io.*;

public class Rotator
{
	public static void main(String[] args)
	{
		for (String filename : args)
		{
			try
			{
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				String line;
				StringBuffer sb = new StringBuffer();
				while((line = br.readLine()) != null)
				{
					String[] parts = line.split("\\s+");
					if (parts.length != 2) throw new RuntimeException("Invalid line " + line + " in file " + filename);
				
					char nl = LetterRotate.Rotate(parts[0].charAt(0),Integer.parseInt(parts[1]));
					System.out.println(parts[0] + "\t" + parts[1] + "\t" + nl );
					sb.append(nl);
				}
				br.close();
				System.out.println(sb.toString());
			}
			catch(Exception ex)
			{
				System.out.println("File Failed: " + ex);
			}
		}
	}
}
