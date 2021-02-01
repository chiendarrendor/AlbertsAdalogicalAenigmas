import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.CanonicalPointSet;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import org.omg.PortableInterceptor.INACTIVE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class Main {
	private static class MyListener implements GridPanel.EdgeListener, GridPanel.GridListener {
		private Board b;
		private String[] lines;
		public MyListener(Board b,String[] lines) { this.lines = lines; this.b = b; }

		private static final EdgeDescriptor WALL = new EdgeDescriptor(Color.BLACK,5);
		private static final EdgeDescriptor PATH = new EdgeDescriptor(Color.GRAY,1);
		private static final EdgeDescriptor UNKNOWN = new EdgeDescriptor(Color.BLACK,1);

		private EdgeDescriptor inDirection(int x1,int y1,int x2,int y2) {
			Set<RegionId> r1 = new HashSet<>(b.getRegionSet(x1,y1).getRegions());
			int s1 = r1.size();
			Set<RegionId> r2 = new HashSet<>(b.getRegionSet(x2,y2).getRegions());
			int s2 = r2.size();
			r1.retainAll(r2);

			if (r1.size() == 0) return WALL;
			if (r1.size() == s1 && s1 == s2) return PATH;

			return UNKNOWN;
		}




		@Override public EdgeDescriptor onBoundary() { return WALL; }
		@Override public EdgeDescriptor toEast(int x, int y) { return inDirection(x,y,x+1,y); }
		@Override public EdgeDescriptor toSouth(int x, int y) { return inDirection(x,y,x,y+1); }
		@Override public int getNumXCells() { return b.getWidth(); }
		@Override public int getNumYCells() { return b.getHeight(); }
		@Override public boolean drawGridNumbers() { return true;  }
		@Override public boolean drawGridLines() { return true;	}
		@Override public boolean drawBoundary() { return true;  }
		@Override public String[] getAnswerLines() { return lines; }

		private static final int INSET = 5;
		private static final int ARC = 10;
		@Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
			if (b.hasLetter(cx,cy)) GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b.getLetter(cx,cy), Direction.NORTHWEST);
			RegionSet rs = b.getRegionSet(cx,cy);
			GridPanel.DrawStringInCorner(bi,Color.BLACK,""+rs.size(),Direction.SOUTHWEST);
			if (b.hasTag(cx,cy)) {
				Graphics2D g = (Graphics2D)bi.getGraphics();
				g.setColor(Color.BLACK);
				g.drawRoundRect(INSET,INSET,bi.getWidth()-2*INSET,bi.getHeight()-2*INSET,ARC,ARC);
				GridPanel.DrawStringInCell(bi,Color.BLACK,""+b.getTag(cx,cy));
			}
			return true;
		}


	}

	private static boolean isPrime(int n) {
		if (n <= 1) return false;

		for(int i = 2 ; i < n ; ++i) {
			if (n%i==0) return false;
		}
		return true;
	}





    public static void main(String[] args) {
	    if (args.length < 0 || args.length > 2) {
	        System.out.println("Bad Command Line");
	        System.exit(1);
        }

        Board b = new Board(args[0]);
		String[] lines = new String[] { "Adalogical Aenigma", "#87 Solver" };
		Solver s = new Solver(b);

	    if (args.length == 2) {
	    	BoardAdder.adder(b,s,args[1]);
		}

		s.Solve(b);






	    if (s.GetSolutions().size() == 1) {
	    	b = s.GetSolutions().get(0);
	    	StringBuffer sb = new StringBuffer();
	    	Board fb = b;
	    	fb.forEachCell((x,y)-> {
	    		if (!fb.hasLetter(x,y)) return;
	    		RegionId rid = fb.getUniqueRegion(x,y);
	    		Pattern p = fb.getPattern(rid.getTag());
	    		if (!isPrime(p.getFinalSize())) return;
	    		sb.append(LetterRotate.Rotate(fb.getLetter(x,y),p.getFinalSize()));
			});
	    	lines[0] = sb.toString();
	    	lines[1] = b.gfr.getVar("SOLUTION");
		}



	    MyListener myl = new MyListener(b,lines);
		GridFrame gf = new GridFrame("Adalogical Aenigma #7 Solver",1200,800,myl,myl);

    }
}
