import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.logic.flatten.FlattenLogicer;
import grid.solverrecipes.genericloopyflatten.LoopyBoard;

import java.util.List;

public class Solver extends FlattenLogicer<Board> {
    private class LoopyAdapter implements LogicStep<Board> {
        private LogicStep<LoopyBoard> llogic;
        public LoopyAdapter(LogicStep<LoopyBoard> llogic) { this.llogic = llogic; }
        @Override public LogicStatus apply(Board b) {return llogic.apply(b.loopy); }

    }


    public Solver(Board b) {
        b.loopy.getLogic().stream().forEach(log->addLogicStep(new LoopyAdapter(log)));
        b.getEdgeCrosses().stream().forEach(ec->addLogicStep(new EdgeCrossLogicStep(ec)));
        b.stream().filter(p->b.hasPod(p.x,p.y)).forEach(p->addLogicStep(new PodLogicStep(p,b.getPodValue(p.x,p.y))));
        b.largecells.stream().forEach(lc->addLogicStep(new LargeClueCellLogicStep(lc)));
        b.vertexClues.keySet().stream().forEach(v->addLogicStep(new VertexClueLogicStep(v,b.vertexClues.get(v))));


    }
}
