import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Solver extends FlattenLogicer<Board>
{
    public Solver(Board b)
    {
        b.forEachCell((x,y)-> {
            CellNumbers cn = b.getWorkBlock(x,y);
            if (cn == null) return;
            addLogicStep(new CellLogicStep(x,y));
        });

        for (int rix = 0 ; rix < b.getRectangles().length ; ++rix)
        {
            Rectangle r = b.getRectangles()[rix];

            for (int x = r.x ; x < r.x+r.width ; ++x)
            {
                List<Point> points = new ArrayList<Point>();
                for (int y = r.y ; y < r.y+r.height; ++y) if (b.getWorkBlock(x,y) != null) points.add(new Point(x,y));
                addLogicStep(new UniqueLogicStep(points));
            }

            for (int y = r.y ; y < r.y+r.height ; ++y)
            {
                List<Point> points = new ArrayList<Point>();
                for (int x = r.x ; x < r.x + r.width; ++x) if (b.getWorkBlock(x,y) != null) points.add(new Point(x,y));
                addLogicStep(new UniqueLogicStep(points));
            }

            for (int x = r.x ; x < r.x + r.width ; ++x )
            {
                for (int y = r.y ; y < r.y + r.height ; ++y)
                {
                    if (b.getCell(x,y) != CellType.NUMBER) continue;
                    List<Point> points = new ArrayList<>();
                    for (Direction d:Direction.values())
                    {
                        int nx = x + d.DX();
                        int ny = y + d.DY();
                        if (nx < r.x || ny < r.y) continue;
                        if (nx >= r.x+r.width || ny >= r.y+r.width) continue;
                        if (b.getWorkBlock(nx,ny) == null) continue;
                        points.add(new Point(nx,ny));
                    }
                    addLogicStep(new SumLogicStep(rix,x,y,b.getNumber(x,y),points));
                }
            }


        }




    }
}
