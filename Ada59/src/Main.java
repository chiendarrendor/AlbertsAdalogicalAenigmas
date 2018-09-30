import grid.assistant.AssistantMove;
import grid.assistant.BoardHolder;
import grid.assistant.CellClicker;
import grid.assistant.EdgeClicker;
import grid.assistant.SolverAssistantFrame;
import grid.letter.LetterRotate;
import grid.logic.flatten.FlattenLogicer;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import javafx.scene.transform.Rotate;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static class MyConfig implements grid.assistant.SolverAssistantConfig<Board> {
        Board b;
        Solver s;
        String[] lines;
        public MyConfig(Board b, String[] lines) {this.b = b; this.lines = lines; }
        public FlattenLogicer<Board> getLogicer() { return new Solver(b); }
        public Board getInitialBoard() { return b; }
        public GridPanel.EdgeListener getEdgeListener(BoardHolder<Board> holder) { return null; }
        public GridPanel.GridListener getGridListener(BoardHolder<Board> holder) { return new MyGridListener(holder,lines); }
        public EdgeClicker getEdgeClicker() { return null; }
        public CellClicker getCellClicker() { return new MyCellClicker();  }
        @Override public String serialize(AssistantMove<Board> move) {
            Board.MyMove mm = (Board.MyMove)move;
            return String.format("%d %d %b %d",mm.getX(),mm.getY(),mm.isOnly(),mm.getItem());
        }

        @Override public AssistantMove<Board> deserialize(String s) {
            Scanner scan = new Scanner(s);
            return new Board.MyMove(scan.nextInt(),scan.nextInt(),scan.nextBoolean(),scan.nextInt());
        }



        @Override public void displaySolution(Board solution) {
            StringBuffer primesb = new StringBuffer();
            StringBuffer compositesb = new StringBuffer();
            StringBuffer onesb = new StringBuffer();

            for (int y = 0 ; y < solution.getHeight() ; ++y) {
                for (int x = 0 ; x < solution.getWidth() ; ++x) {
                    Cell c = solution.getCell(x,y);
                    if (c == null) continue;
                    if (c.isWall()) continue;

                    char rotchar = LetterRotate.Rotate(solution.getLetter(x,y),c.getSingleNumber());
                    switch(c.getSingleNumber()) {
                        case 1:
                        case 4:
                            compositesb.append(rotchar);
                            break;
                        case 2:
                        case 3:
                        case 5:
                            primesb.append(rotchar);
                            break;
                        default:
                            compositesb.append(rotchar);
                            break;
                    }
                }
            }
            System.out.println("Primes: " + primesb.toString());
            System.out.println("Ones: " + onesb.toString());
            System.out.println("Composites: " + compositesb.toString());
            String[] lines = new String[] { primesb.toString(),solution.getSolution() };

            GridFrame gf = new GridFrame("Adalogical Aenigma #59 Solution, ",1300,800,
                    new MyGridListener(()->{return solution;},lines));

        }
    }
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }

        Board b = new Board(args[0]);
        String[] lines = { "line 1", "line 2" };

//        b.getCell(1,0).removeAllBut(6);
//        b.getCell(1,2).removeAllBut(2);
//        b.getCell(1,3).makeWall();
//        b.getCell(1,4).removeAllBut(4);
//        b.getCell(1,5).removeAllBut(5);
//        b.getCell(1,6).removeAllBut(1);
//
//        List<Integer> clues = new ArrayList<>(Arrays.asList(-1,-1,-1));
//        List<Point> cells = new ArrayList<>(Arrays.asList(new Point(1,0),new Point(1,1),
//                new Point(1,2),new Point(1,3),new Point(1,4),new Point(1,5),
//                new Point(1,6)));
//
//        BetterClueLogicStep bcls = new BetterClueLogicStep(clues,cells,b.getMaxCount(),b.getBoxes());
//
//        bcls.apply(b);
//        bcls.test();



        SolverAssistantFrame<Board> saf = new SolverAssistantFrame<Board>("Adalogical Aenigma #59 Solver Assistant",
                1300,800,new MyConfig(b,lines));


    }


}
