import grid.logic.flatten.FlattenLogicer;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Solver extends FlattenLogicer<Board> {
    public List<Point> makeRow(int row,Rectangle r) {
        List<Point> result = new ArrayList<>();
        for (int i = 0 ; i < r.width ; ++i) {
            result.add(new Point(i+r.x,row+r.y));
        }
        return result;
    }

    private List<Point> makeColumn(int col,Rectangle r) {
        List<Point> result = new ArrayList<>();
        for (int i = 0 ; i < r.height ; ++i) {
            result.add(new Point(col+r.x,i+r.y));
        }
        return result;
    }

    public List<Integer> makeClueRow(Board b,int y) {
        List<Integer> result = new ArrayList<>();
        for (int x = 0 ; x < b.getWidth(); ++x) {
            String s = b.getHorizontalClue(x,y);
            if (s.equals(".")) break;
            if (s.equals("?")) {
                result.add(-1);
            } else {
                result.add(Integer.parseInt(s));
            }
        }
        return result;
    }


    private List<Integer> makeClueColumn(Board b,int x) {
        List<Integer> result = new ArrayList<>();
        for (int y = 0 ; y < b.getHeight(); ++y) {
            String s = b.getVerticalClue(x,y);
            if (s.equals(".")) break;
            if (s.equals("?")) {
                result.add(-1);
            } else {
                result.add(Integer.parseInt(s));
            }
        }
        return result;
    }

    private <T> List<T> reverse(List<T> list) {
        List<T> result = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0 ; --i) result.add(list.get(i));
        return result;
    }


    public Solver(Board b) {
        for (Rectangle r: b.getBoxes()) {
            for (int i = 0 ; i < r.width ; ++i) {
                addLogicStep(new CellUniqueLogicStep(makeRow(i,r)));
                addLogicStep(new CellUniqueLogicStep(makeColumn(i,r)));
            }
        }

        Rectangle tot = new Rectangle(0,0,b.getWidth(),b.getHeight());

        for (int x = 0 ; x < b.getWidth(); ++x) {
            List<Integer> clue = makeClueColumn(b,x);
            List<Point> cells = makeColumn(x,tot);
            addLogicStep(new BetterClueLogicStep(clue,cells,b.getMaxCount(),b.getBoxes()));
            addLogicStep(new BetterClueLogicStep(reverse(clue),reverse(cells),b.getMaxCount(),b.getBoxes()));
        }

        for (int y = 0 ; y < b.getHeight(); ++y) {
            List<Integer> clue = makeClueRow(b,y);
            List<Point> cells = makeRow(y,tot);
            addLogicStep(new BetterClueLogicStep(clue,cells,b.getMaxCount(),b.getBoxes()));
            addLogicStep(new BetterClueLogicStep(reverse(clue),reverse(cells),b.getMaxCount(),b.getBoxes()));
        }

    }

}
