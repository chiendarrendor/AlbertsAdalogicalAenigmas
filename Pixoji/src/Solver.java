import grid.lambda.CellLambda;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Solver extends FlattenLogicer<Board>
{
    private List<Point> cellsAround(Board b,int x,int y)
    {
        List<Point> result = new ArrayList<Point>();
        result.add(new Point(x,y));
        for(Direction d : Direction.values())
        {
            if (b.inBounds(x+d.DX(),y+d.DY())) result.add(new Point(x+d.DX(),y+d.DY()));
        }
        return result;
    }

    private List<Point> cellsInColumn(Board b, int x)
    {
        List<Point> result = new ArrayList<Point>();
        for (int y = 0 ; y < b.getHeight() ; ++y) result.add(new Point(x,y));
        return result;
    }

    private List<Point> cellsInRow(Board b,int y)
    {
        List<Point> result = new ArrayList<Point>();
        for (int x = 0 ; x < b.getWidth() ; ++x) result.add(new Point(x,y));
        return result;
    }


    public Solver(Board b)
    {
        CellLambda.forEachCell(b.getWidth(),b.getHeight(),(x,y)->{
            Clue c = b.getClue(x,y);
            if (c == null) return;

            switch(c.ct)
            {
                case BOX: addLogicStep(new CellBlockSomeOfLogicStep(c.size, cellsAround(b,x,y))); break;
                case HEART: addLogicStep(new StarPatternLogicStep(b,c.size,CellType.WHITE,cellsAround(b,x,y))); break;
                case SMILE: addLogicStep(new StarPatternLogicStep(b,c.size,CellType.BLACK,cellsAround(b,x,y))); break;
                case PAINT: addLogicStep(new PaintLogicStep(x,y,c.size)); break;
                case CIRCLE: break; // this has been handled by the board clue parser, but we need to ignore it so it still prints on the board.
                default: throw new RuntimeException("Unknown clue! " + c);
            }
        });

        for(int x = 0 ; x < b.getWidth() ; ++x)
        {
            Clue c = b.getTopClue(x);
            if (c == null) continue;

            switch(c.ct)
            {
                case ARROW: addLogicStep(new CellBlockSomeOfLogicStep(c.size,cellsInColumn(b,x))); break;
                case HEART: addLogicStep(new StarPatternLogicStep(b,c.size,CellType.WHITE,cellsInColumn(b,x))); break;
                case SMILE: addLogicStep(new StarPatternLogicStep(b,c.size,CellType.BLACK,cellsInColumn(b,x))); break;
                case CROWN: addLogicStep(new CrownLogicStep(c.size,cellsInColumn(b,x),"COL " + x)); break;
                case CAKE: addLogicStep(new CakeLogicStep(c.size,cellsInColumn(b,x))); break;
                default: throw new RuntimeException("Unknown Top Clue! " + c);
            }
        }

        for (int y = 0 ; y < b.getHeight() ; ++y)
        {
            Clue c = b.getLeftClue(y);
            if (c == null) continue;

            switch(c.ct)
            {
                case ARROW: addLogicStep(new CellBlockSomeOfLogicStep(c.size,cellsInRow(b,y))); break;
                case HEART: addLogicStep(new StarPatternLogicStep(b,c.size,CellType.WHITE,cellsInRow(b,y))); break;
                case SMILE: addLogicStep(new StarPatternLogicStep(b,c.size,CellType.BLACK,cellsInRow(b,y))); break;
                case CROWN: addLogicStep(new CrownLogicStep(c.size,cellsInRow(b,y),"ROW " + y)); break;
                case CAKE: addLogicStep(new CakeLogicStep(c.size,cellsInRow(b,y))); break;
                default: throw new RuntimeException("Unknown Left Clue! " + c);
            }
        }

        for(char rid : b.getRegionIds())
        {
            addLogicStep(new RegionLogicStep(b.getCellsOfRegion(rid)));
        }


    }


}
