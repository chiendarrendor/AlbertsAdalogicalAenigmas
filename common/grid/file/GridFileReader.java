package grid.file;
// this class will read a file in the following form
// <width> <height>
// CODE:<code1>
// <block1>
// CODE:<code2>
// <block2>
// ...
// VARIABLES:
// <name>:<value>
// <name2>:<value2>
// ...
//
// This code will ensure that each block is exactly <height> lines of <width> arbitrary strings, separated by one or more spaces or tabs
// leading and trailing tabs will be ignored in the blocks

import java.awt.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class GridFileReader
{
	private Map<String,String[][]> codeBlocks = null;
	private Map<String,String> vars = new HashMap<String,String>();
	private int width;
	private int height;
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }

	public boolean inBounds(int x,int y)
	{
		return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
	}

	public boolean inBounds(Point p) { return inBounds(p.x,p.y);}


	public boolean hasBlock(String s) { return codeBlocks.containsKey(s); }
	public String[][] getBlock(String s) { return codeBlocks.get(s); }


	public boolean hasVar(String var) { return vars.containsKey(var);}

	public String getVar(String var)
	{
		if (!hasVar(var)) return null;
		return vars.get(var);
	}
	
	public static int toInt(String s)
	{
		try
		{
			return Integer.parseInt(s);
		} 
		catch (Exception ex)
		{
			throw new RuntimeException("bad integer.");
		}
	}
		
	public GridFileReader(String filename)
	{
		this(filename,null);
	}
	
	
	
	public GridFileReader(String filename,String[] requiredBlocks)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line = null;
			int ycount = 0;
			String code = null;
			Pattern startPattern = Pattern.compile("^(\\d+)\\s+(\\d+)$");
			Pattern codePattern = Pattern.compile("^CODE:\\s*(.+)$");
			Pattern varStartPattern = Pattern.compile("^VARIABLES:$");
			Pattern varPattern = Pattern.compile("^([^:]+)\\s*:\\s*(.+)$");
			boolean incode = false;
			boolean invar = false;
			
			
			while((line = reader.readLine()) != null)
			{	
				line = line.trim();
				if (line.length() == 0) continue;
				// adding ability to put in comments
				if (line.charAt(0) == '#') continue;
				
				if (codeBlocks == null)
				{
					Matcher start = startPattern.matcher(line);
					if (start.find())
					{
						width = Integer.parseInt(start.group(1));
						height = Integer.parseInt(start.group(2));
						codeBlocks = new HashMap<String,String[][]>();
						continue;
					}
					else
					{
						throw new RuntimeException("Illegal width height line");
					}
				}
				Matcher codeMatcher = codePattern.matcher(line);
				if (codeMatcher.find())
				{
					if (code != null && ycount != height) throw new RuntimeException("Block " + code + " has too few lines! " + ycount + " vs " + height);
					code = codeMatcher.group(1);
					if (codeBlocks.containsKey(code)) throw new RuntimeException("Duplicate code "+ code);
					codeBlocks.put(code,new String[width][height]);
					ycount = 0;
					incode = true;
					invar = false;
					continue;
				}
				Matcher varStartMatcher = varStartPattern.matcher(line);
				if (varStartMatcher.find())
				{
					if (code != null && ycount != height) throw new RuntimeException("Block " + code + " has too few lines! " + ycount + " vs " + height);
					incode = false;
					invar = true;
					continue;
				}
				
				if (invar)
				{
					Matcher varMatcher = varPattern.matcher(line);
					if (!varMatcher.find()) throw new RuntimeException("invalid var line: " + line);
					vars.put(varMatcher.group(1),varMatcher.group(2));
				}
				
				if (incode)
				{	
					String[] cells = line.split("\\s+");

					if (ycount >= height) throw new RuntimeException("block " + code + " has too many lines!");
					
					// special case...if there is one string of length width, then spaces aren't necessary.
					if (cells.length == 1 && width != 1 && line.length() == width)
					{
						for (int i = 0 ; i < width ; ++i) codeBlocks.get(code)[i][ycount] = line.substring(i,i+1);
					}
					else if (cells.length != width) throw new RuntimeException("Line " + ycount + " in block " + code + " does not have exactly " + width + " records");
					else
					{
						for (int i = 0 ; i < width ; ++i) codeBlocks.get(code)[i][ycount] = cells[i];
					}
					++ycount;
				}
			}
			if (ycount != height) throw new RuntimeException("Terminal Block " + code + " has too few lines! " + ycount + " vs " + height);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
		
		if (requiredBlocks == null) return;
		
		for (String block : requiredBlocks)
		{
			if (!codeBlocks.containsKey(block)) throw new RuntimeException("file does not contain required block " + block);
		}
	}
}
				
				
				
				
				
				
				
				