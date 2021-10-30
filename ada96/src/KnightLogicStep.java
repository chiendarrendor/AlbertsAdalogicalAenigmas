import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class KnightLogicStep implements LogicStep<Board> {
    int knightid;
    public KnightLogicStep(int knightid) { this.knightid = knightid; }

    LogicStatus recursionStatus = LogicStatus.STYMIED;

    // step 1:
    // determine if each node of the tree (except root) is legal:
    //    a) <deleted>
    //    b) does its target point land on a board-placed knights path?     (step is invalid)
    //    c) does its line cross another board-placed knights path?         (step is invalid)
    //    d) is the target point CANT_STOP_HERE?                            (step is infeasible)
    //    e) is the target point MUST_HAVE_KNIGHT?                          (all children are invalid)
    //
    //    after recursion returns:  if a step is expanded, has no children, and infeasible, it is invalid

    // also, count # of feasible steps (including root) and keep a list of unexpanded leaves
    private void recursiveProcess(Board thing, Knight knight, KnightStep step) {

        if (step.getParent() != null) {
            SegmentSet ss = new SegmentSet();
            ss.addSegment(step.getJumpSegment());

            if (!thing.getMasterSegmentSet().addable(ss)) {
                step.delete();
                recursionStatus = LogicStatus.LOGICED;
                return;
            }

            switch(thing.getCell(step.getPoint().x,step.getPoint().y)) {
                case UNKNOWN: break;
                case CANT_STOP_HERE:
                    if (step.isFeasible()) {
                        step.markInfeasable();
                        recursionStatus = LogicStatus.LOGICED;
                    }
                    break;
                case MUST_HAVE_KNIGHT:
                    if (!step.isExpanded() || step.getChildCount() > 0) {
                        recursionStatus = LogicStatus.LOGICED;
                        step.removeChildren();
                    }
                    break;
                case POSITION_INITIAL:
                case POSITION_INTERMEDIATE:
                case POSITION_FINAL:
                    step.delete();
                    recursionStatus = LogicStatus.LOGICED;
                    return;
            }
        }

        List<KnightStep> originalList = new ArrayList<>();
        originalList.addAll(step.getChildren());

        for (KnightStep ks : originalList) {
            recursiveProcess(thing, knight, ks);
        }

        if (step.getParent() != null && step.isExpanded() && !step.isFeasible() && step.getChildCount() == 0) {
            step.delete();
            recursionStatus = LogicStatus.LOGICED;
            return;
        }

        // so, if we didn't get here, that's because the step we were looking on was deleted. if we're here, let's do stats...
        if (step.isFeasible()) knight.addFeasible(step);
        if (!step.isExpanded()) knight.addUnexpanded(step);

    }

    @Override public LogicStatus apply(Board thing) {
        Knight knight = thing.getKnight(knightid);
        // a locked knight has been added to the board, so it is no longer interesting to play with.
        if (knight.isLocked()) return LogicStatus.STYMIED;

        knight.clearEphemera();

        recursionStatus = LogicStatus.STYMIED;

        // part 1 (see details above)
        recursiveProcess(thing,knight, knight.getStepTree());

        // now, handle result of validating every tree node.
        if (knight.feasibleCount() == 0 && knight.unexpandedCount() == 0) return LogicStatus.CONTRADICTION;

        if (knight.feasibleCount() == 1 && knight.unexpandedCount() == 0) {
            KnightStep theFeasible = knight.getFeasibles().iterator().next();
            thing.placePathOnBoard(knight,theFeasible);
            return LogicStatus.LOGICED;
        }


        if (knight.unexpandedCount() == 0) return recursionStatus;
        if (recursionStatus == LogicStatus.LOGICED) return recursionStatus;

        // if we get here, we have at least one unexpanded step, and we've made no progress on anything else... let's expand one.

        // we don't want the Knight object to get too large, so let's not just willy-nilly expand out every time.
        if (knight.feasibleCount() > knight.getAllowableFeasibles()) return LogicStatus.STYMIED;

        KnightStep expandingStep = knight.getUnexpandeds().iterator().next();
        int maxDepth = knight.getMaxDepth();


        // expand children with appropriate feasibiilty
        if (maxDepth == -1) {
            expandingStep.expand(true);
        } else if (expandingStep.getDepth() == maxDepth) {
            expandingStep.removeChildren();
        } else if (expandingStep.getDepth() == maxDepth - 1) {
            expandingStep.expand(true);
        } else {
            expandingStep.expand(false);
        }

        // make segment set out of all tree parent jumps
        SegmentSet treeSegments = new SegmentSet();
        List<Segment> segmentsInOrder = new ArrayList<>();
        for(KnightStep treeWalker = expandingStep ; treeWalker.getParent() != null ; treeWalker = treeWalker.getParent()) {
            segmentsInOrder.add(0,treeWalker.getJumpSegment());
        }
        for (Segment s : segmentsInOrder) {
            treeSegments.addSegment(s);
        }

        // children have to be all on board and non-intersecting with the earlier jumps of this step
        List<KnightStep> originalChildren = new ArrayList<>();
        originalChildren.addAll(expandingStep.getChildren());
        for (KnightStep child : originalChildren) {
            if (!thing.inBounds(child.getPoint())) {
                child.delete();
                continue;
            }
            if (!treeSegments.addable(child.getJumpSegment())) {
                child.delete();
                continue;
            }
        }


       return LogicStatus.LOGICED;

    }
}
