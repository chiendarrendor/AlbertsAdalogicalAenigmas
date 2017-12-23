
import java.awt.image.*;
import java.awt.*;
import java.util.*;


public class Ada24
{
	private static class MyListener implements GridPanel.GridListener
	{
		Board myBoard;
		State myState;
		public MyListener(Board b,State s) { myBoard = b; myState = s;}
	
		public int getNumXCells() { return myBoard.width; }
		public int getNumYCells() { return myBoard.height; }
		public boolean drawGridNumbers() { return false; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public boolean drawCellContents(int cx,int cy, BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();
			Point p = new Point(cx,cy);
			
			if (cx < 0 || cy < 0 || cx >= myBoard.width || cy >= myBoard.height)
			{
                            Color drawColor = Color.black;
                            String drawString = "";
                            if (cx >= 0 && cx < myBoard.width) drawString = "" + cx;
                            else if (cy >= 0 && cy < myBoard.height) drawString = "" + cy;
                            
                            if (myBoard.pathsByPoint.containsKey(p))
                            {
                                    Board.Path path = myBoard.pathsByPoint.get(p);
                                    if (myState.fired.contains(path.id)) drawColor = Color.red;
                                    drawString += "(" + path.id + path.numMirrors + ")";
                            }
                            GridPanel.DrawStringInCell(bi, drawColor, drawString);
                            return true;
			}
			
			GridPanel.DrawStringInCell(bi,Color.black,"" + myBoard.letters[cx][cy]);
			
			g.setStroke(new BasicStroke(5));
			g.setColor(Color.red);
			switch(myState.state[cx][cy])
			{
				case EMPTY: g.drawOval(0,0,bi.getWidth(),bi.getHeight()); break;
				case UNKNOWN: break;
				case GENERICMIRROR:
					g.drawLine(0,0,bi.getWidth(),bi.getHeight());
					g.drawLine(0,bi.getWidth(),bi.getHeight(),0);
					break;
				case BACKSLASH: g.drawLine(0,0,bi.getWidth(),bi.getHeight()); break;
				case SLASH: g.drawLine(0,bi.getWidth(),bi.getHeight(),0); break;
			}
			
			
			
			
			return true;
		}
	}
	
	private static class MyEdgeListener implements GridPanel.EdgeListener
	{
		private Board myBoard = null;
		public MyEdgeListener(Board b) { myBoard = b; }
		
		public GridPanel.EdgeListener.EdgeDescriptor onBoundary()
		{
			return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,5);
		}
		
		public GridPanel.EdgeListener.EdgeDescriptor toEast(int x,int y)
		{
			int w = myBoard.regions[x][y] == myBoard.regions[x+1][y] ? 1 : 5;
			return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,w);
		}	
	
		public GridPanel.EdgeListener.EdgeDescriptor toSouth(int x,int y)
		{
			int w = myBoard.regions[x][y] == myBoard.regions[x][y+1] ? 1 : 5;
			return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,w);
		}
	}
	
	

	public static void main(String[] args)
	{
		GridFileReader gfr = new GridFileReader("ada24.txt");
		Board b = new Board(gfr);
		State s = new State(b);
                
                Logicer<State> logicer = new Logicer<>();
		Logic log = new Logic(logicer);
               
		ActionReader.ReadFile("moves.txt",s);

               
		LogicStatus status = logicer.RecursiveApplyLogic(s);
		System.out.println("Logic status: " + status);
		



		System.out.print("unfired: ");
		for (char c : s.unfired) { System.out.print(c); }
		System.out.println("");
		
		System.out.print("fired: ");
		for (char c : s.fired) { System.out.print(c); }
		System.out.println("");	

 //               char pathid = 'U';
 //               System.out.println("TestFire " + pathid);
 //               Board.Path path = b.pathsById.get(pathid);
 //               FireField ff = new FireField(s,path,path.p1);
 //               for (FirePath fp : ff.paths)
 //               {
 //                   System.out.println(fp.toString());
  //              }
                
//                System.out.println("TestFire Reverse " + pathid);
//                Board.Path path2 = b.pathsById.get(pathid);
//                FireField ff2 = new FireField(s,path2,path2.p2);
//                for (FirePath fp : ff2.paths)
 //               {
 //                   System.out.println(fp.toString());
  //              }               
                
   
                logicer.Solve(s);
                
                s = logicer.GetSolutions().get(0);

  
  
  
                boolean dotrep = false;
                for (int y = 0 ; y < b.height; ++y)
                {
                    for (int x = 0 ; x < b.width; ++x)
                    {
                        switch(s.state[x][y])
                        {
                            case EMPTY: break;
                            case UNKNOWN: if (!dotrep) { System.out.print(".."); dotrep = true; } break;
                            case GENERICMIRROR: 
                                System.out.print("(");
                                System.out.print(LetterRotate.Rotate(b.letters[x][y], -1));
                                System.out.print(LetterRotate.Rotate(b.letters[x][y], 1));
                                System.out.print(")");
                                dotrep = false; 
                                break;
                            case SLASH: System.out.print(LetterRotate.Rotate(b.letters[x][y], 1)); dotrep=false;  break;
                            case BACKSLASH: System.out.print(LetterRotate.Rotate(b.letters[x][y], -1)); dotrep = false; break;
                        }
                    }
                }
                System.out.println("");
                
                
		GridFrame gf = new GridFrame("Adalogical Aenigma #24",1300,768,new MyListener(b,s),new MyEdgeListener(b));
	}
}
