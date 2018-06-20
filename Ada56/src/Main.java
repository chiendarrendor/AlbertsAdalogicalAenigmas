import grid.assistant.AssistantMove;
import grid.assistant.BoardHolder;
import grid.assistant.CellClicker;
import grid.assistant.EdgeClicker;
import grid.assistant.MovePair;
import grid.assistant.SolverAssistantConfig;
import grid.assistant.SolverAssistantFrame;
import grid.graph.GridGraph;
import grid.letter.LetterRotate;
import grid.logic.flatten.FlattenLogicer;
import grid.spring.GridFrame;
import grid.spring.GridPanel;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

public class Main {

    public static void drawCharInCell(BufferedImage bi, Color ch, char c, int fontSize, boolean isBold) {
        Graphics2D g = (Graphics2D)bi.getGraphics();
        Font currentFont = g.getFont();
        Font newFont = currentFont.deriveFont(isBold ? Font.BOLD : Font.PLAIN,(float)fontSize);
        g.setFont(newFont);
        GridPanel.DrawStringInCell(g,ch,0,0,bi.getWidth(),bi.getHeight(),""+c);
    }


    private static class MyAssistantConfig implements SolverAssistantConfig<Board> {
        Board ib;
        FlattenLogicer<Board> logicer;

        public MyAssistantConfig(Board ib, FlattenLogicer<Board> logicer) { this.ib = ib; this.logicer = logicer; }
        public Board getInitialBoard() { return ib; }
        public FlattenLogicer<Board> getLogicer() { return logicer; }

        public GridPanel.GridListener getGridListener(BoardHolder<Board> holder) {
            return new MyGridListener(holder);
        }

        public GridPanel.EdgeListener getEdgeListener(BoardHolder<Board> holder) {
            return new MyEdgeListener(holder);
        }

        public CellClicker<Board> getCellClicker() { return (orig,cur,x,y) -> {

                if (orig.getCell(x,y) == CellType.UNKNOWN) {
                    switch(cur.getCell(x,y)) {
                        case UNKNOWN:
                            cur.setCell(x,y,CellType.WALL);
                            return new MovePair<Board>(
                                    new Board.MyMove(x,y,CellType.WALL),
                                    new Board.MyMove(x,y,CellType.PATH));
                        case WALL:
                            cur.setCell(x,y,CellType.PATH);
                            return new MovePair<Board>(
                                    new Board.MyMove(x,y,CellType.PATH),
                                    new Board.MyMove(x,y,CellType.WALL));
                        case PATH:
                            cur.setCell(x,y,CellType.UNKNOWN);
                            return new MovePair<Board>();
                    }
                }
                if (orig.getCell(x,y) == CellType.WALL) return null;
                if (orig.getPath(x,y) != PathStatus.UNKNOWN) return null;

                switch(cur.getPath(x,y)) {
                    case ONPATH:
                        cur.setPath(x,y,PathStatus.NOTONPATH);
                        return new MovePair<Board>(
                                new Board.MyMove(x,y,PathStatus.NOTONPATH),
                                new Board.MyMove(x,y,PathStatus.ONPATH));
                    case NOTONPATH:
                        cur.setPath(x,y,PathStatus.UNKNOWN);
                        return new MovePair<Board>();
                    case UNKNOWN:
                        cur.setPath(x,y,PathStatus.ONPATH);
                        return new MovePair<Board>(
                                new Board.MyMove(x,y,PathStatus.ONPATH),
                                new Board.MyMove(x,y,PathStatus.NOTONPATH));
                    default:
                        throw new RuntimeException("How did that happen?");
                }
            };
        }

        public EdgeClicker getEdgeClicker() { return null; }

        // do not use slashes in serialize.
        public String serialize(AssistantMove<Board> move) {
            Board.MyMove mm = (Board.MyMove)move;
            if (mm.isPath) return String.format("%b,%d,%d,%s",true,mm.x,mm.y,mm.ps);
            else return String.format("%b,%d,%d,%s",false,mm.x,mm.y,mm.ct);
        }

        public AssistantMove<Board> deserialize(String s) {
            String[] parts = s.split(",");
            boolean isPath = Boolean.parseBoolean(parts[0]);
            if (isPath) {
                return new Board.MyMove(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),
                        Enum.valueOf(PathStatus.class,parts[3]));
            } else {
                return new Board.MyMove(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),
                        Enum.valueOf(CellType.class,parts[3]));
            }
        }

        private static class SolutionRef implements GridGraph.GridReference {
            Board b;
            public SolutionRef(Board b) { this.b = b; }
            public int getWidth() { return b.getWidth(); }
            public int getHeight() { return b.getHeight(); }
            public boolean isIncludedCell(int x,int y) { return b.getCell(x,y) == CellType.PATH; }
            public boolean edgeExitsEast(int x,int y) { return true; }
            public boolean edgeExitsSouth(int x,int y) { return true; }
        }

        public void displaySolution(Board solution) {
            GridGraph gg = new GridGraph(new SolutionRef(solution));
            StringBuffer sb = new StringBuffer();
            final Point spoint = solution.getStart();

            solution.forEachCell((x,y) -> {
                if (solution.getCell(x,y) != CellType.PATH) return;
                if (solution.getPath(x,y) == PathStatus.ONPATH) return;
                if (!solution.hasLetter(x,y)) return;



                List<Point> path = gg.shortestPathBetween(spoint,new Point(x,y));
                int circount = (int)path.stream().filter(p->solution.getShape(p.x,p.y)==CellShape.CIRCLE).count();

                System.out.println("X: " + x + " Y: " + y + " : " + solution.getLetter(x,y) + " R: " + circount);

                sb.append(LetterRotate.Rotate(solution.getLetter(x,y),circount));
            });

            String[] lines = new String[2];
            lines[0] = sb.toString();
            lines[1] = solution.getSolution();




            GridFrame theframe = new GridFrame("Ada 56 Solver",1000,800,
                    new MyGridListener(()->solution,lines),new MyEdgeListener(()->solution));
        }


    }





    public static void main(String[] args) {
        if (args.length != 1) throw new RuntimeException("Bad Command Line");

        Board b = new Board(args[0]);
        Solver s = new Solver(b);

        SolverAssistantFrame<Board> saf = new SolverAssistantFrame<>("Ada 56 Solver",1000,800,
                new MyAssistantConfig(b,s));

    }
}
