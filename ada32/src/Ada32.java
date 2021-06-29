import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Ada32
{
	private static class MyListener implements GridPanel.GridListener {
		Board b;
		String[] lines;
		public MyListener(Board b,String[] lines) { this.b = b; this.lines = lines; }

		@Override public int getNumXCells() { return b.getWidth(); }
		@Override public int getNumYCells() { return b.getHeight(); }
		@Override public boolean drawGridNumbers() { return true; }
		@Override public boolean drawGridLines() { return true; }
		@Override public boolean drawBoundary() { return true; }
		@Override public String[] getAnswerLines() { return lines; }

		@Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
			Graphics2D g = (Graphics2D)bi.getGraphics();

			if (b.hasBlock(cx,cy)) {
				g.setColor(Color.BLACK);
				g.fillRect(0,0,bi.getWidth(),bi.getHeight());
				if (b.hasClue(cx,cy)) {
					GridPanel.DrawStringInCell(bi,Color.WHITE,""+b.getClueNumber(cx,cy)+b.getClueDirection(cx,cy).getSymbol());
				}
				return true;
			}



			NumCell ns = b.getCell(cx,cy);

			if (ns.isBroken()) {
				g.setColor(Color.RED);
				g.fillRect(0,0,bi.getWidth(),bi.getHeight());
			} else if (ns.isDone() && ns.getComplete() == 0) {
				g.setColor(Color.GREEN);
				g.fillRect(0,0,bi.getWidth(),bi.getHeight());
			} else {
				StringBuffer sb = new StringBuffer();
				for (int v : ns.getPossibles())  sb.append(v);
				GridPanel.DrawStringInCell(bi,Color.BLACK,sb.toString());
			}

			GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
			return true;
		}
	}


	public static void main(String[] args)
	{
		if (args.length != 1) {
			System.out.println("Bad Command Line");
			System.exit(1);
		}

		Board b = new Board(args[0]);
		String[] lines = new String[] {"Adalogical Aenigma","#32 Solver"};

		//SnakeImages si = new SnakeImages();
		//if (si != null) System.exit(0);

		// knowns!
		/*
		b.getCell(20,7).remove(1);
		b.getCell(20,6).remove(1);
		b.getCell(20,2).remove(1);
		b.getCell(20,3).remove(1);
		b.getCell(20,4).remove(1);
		b.getCell(2,0).remove(0);
*/


		Solver s = new Solver(b,true);
		//s.debug();

		//s.testRecursion(b);
		s.Solve(b);

		if (s.GetSolutions().size() == 1) {
			b = s.GetSolutions().get(0);
			Board fb = b;
			StringBuffer sb = new StringBuffer();
			fb.forEachCell((x,y)->{
				NumCell nc = fb.getCell(x,y);
				int v = nc.getComplete();
				if (v != 1 && v != 5) return;
				sb.append(LetterRotate.Rotate(fb.getLetter(x,y),v));
			});

			lines[0] = sb.toString();
			lines[1] = fb.gfr.getVar("SOLUTION");
		}




		MyListener myl = new MyListener(b,lines);
		GridFrame gf = new GridFrame("Adalogical Aenigma #32 Solver",1200,800,myl);
	}
}