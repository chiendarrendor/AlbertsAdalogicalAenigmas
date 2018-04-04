
import grid.file.GridFileReader;
import grid.letter.LetterRotate;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.awt.image.BufferedImage;

public class Ada23
{

	private static class MyListener implements GridPanel.GridListener
	{
		private Board myBoard;
		private String answer;
		private CellContainer<Shot> shotinfo;
		
		public MyListener(Board b, String answer) { myBoard = b; this.answer = answer; shotinfo = myBoard.getShotInfo(); }
	
		public int getNumXCells() { return myBoard.getWidth(); }
		public int getNumYCells() { return myBoard.getHeight(); }
		public boolean drawGridNumbers() { return true; }
		public boolean drawGridLines() { return true; }
		public boolean drawBoundary() { return true; }
		public String getAnswerText() { return answer; }
		public boolean drawCellContents(int cx,int cy, BufferedImage bi)
		{
			Graphics2D g = (Graphics2D)bi.getGraphics();
			CellState cs = myBoard.getCellStates().getCell(cx,cy);
            int inset = 10;

			switch(cs) {
                case EMPTY:
                    break;
                case BALL:
                    g.setColor(Color.black);
                    g.setStroke(new BasicStroke(1));
                    g.drawOval(inset,inset,bi.getWidth() - 2*inset,bi.getHeight()-2*inset);
                    GridPanel.DrawStringInCell(bi,Color.black, myBoard.getBall(cx,cy));
                    break;
                case PATH:
                    GridPanel.DrawStringInCell(bi,Color.BLUE,""+shotinfo.getCell(cx,cy).d.getSymbol());
                    break;
                case LIE:
                    g.setColor(Color.blue);
                    g.setStroke(new BasicStroke(10));
                    g.fillOval(inset,inset,bi.getWidth() - 2*inset,bi.getHeight()-2*inset);

                    // this detects the 'LIE' that is actually where we started...we'll want this later.
                    Shot s = shotinfo.getCell(cx,cy);
                    if (s.parent == null && cx == s.starting.x && cy == s.starting.y) {
                        g.setColor(Color.GREEN);
                        g.drawOval(inset,inset,bi.getWidth() - 2*inset,bi.getHeight()-2*inset);
                    }


                    GridPanel.DrawStringInCell(bi,Color.WHITE,""+shotinfo.getCell(cx,cy).d.getSymbol());
                    break;
                case HOLE:
                    g.setColor(Color.green);
                    g.setStroke(new BasicStroke(10));
                    g.drawOval(inset,inset,bi.getWidth() - 2*inset,bi.getHeight()-2*inset);
                    break;
                case FULL:
                    g.setColor(Color.gray);
                    g.setStroke(new BasicStroke(10));
                    g.fillOval(inset,inset,bi.getWidth() - 2*inset,bi.getHeight()-2*inset);
                    break;
                case TRAP:
                    g.setColor(Color.blue);
                    g.fillRect(0,0,bi.getWidth(),bi.getHeight());
                    break;
            }

             if (myBoard.getLetter(cx,cy) != '.')
			{
				GridPanel.DrawStringUpperLeftCell(bi,Color.black,"" + myBoard.getLetter(cx,cy));
			}
			
			
			return true;
		}
	}

			



	public static void main(String[] args)
	{
		if (args.length != 1) throw new RuntimeException("bad command line");
		String fname = args[0];

		Board b = new Board(fname);


        Solver s = new Solver(b);
        s.Solve(b);

        if (s.GetSolutions().size() == 0) {
            System.out.println("No Solutions");
        } else {
            b = s.GetSolutions().get(0);
            if (s.GetSolutions().size() > 1) System.out.println("Multiple Solutions!");
        }

        b.getCharMap().show();


        final Board fb = b;
        String soltype = fb.getSolutionType();
        StringBuffer answer = new StringBuffer();
        answer.append("<html><font size=\"5\">");
        if ("SIMPLEEMPTY".equals(soltype)) {
            fb.forEachCell((x, y) -> {
                if (fb.getCellStates().getCell(x, y) == CellState.EMPTY) answer.append(fb.getLetter(x, y));
            });
        } else if ("ROTATELIES".equals(soltype)) {
            CellContainer<Shot> shotcon = fb.getShotInfo();

            fb.forEachCell((x,y)-> {
                if (fb.getCellStates().getCell(x,y) != CellState.LIE) return;
                Shot par = shotcon.getCell(x,y).parent;
                if (par == null) return;
                answer.append(LetterRotate.Rotate(fb.getLetter(x,y),par.length));

            });
        } else {
            answer.append("Unknown Solution type");
        }

        answer.append("<br>");
        answer.append(b.getCookedSolution());
        answer.append("</font>");



		GridFrame gf = new GridFrame("Adalogical Aenigma #23",1300,768,new MyListener(fb,answer.toString()));
		
	}
}