import grid.logic.LogicStep;

import java.awt.Point;
import java.util.List;

public class LineContainsKnightsLogicStep extends ExactCellCountLogicStep {
    public LineContainsKnightsLogicStep(List<Point> line, int knightsPerLine ) { init(line,knightsPerLine); }
}
