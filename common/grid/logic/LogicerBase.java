package grid.logic;

import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 5/20/2017.
 */
public abstract class LogicerBase<T>
{
    private Vector<LogicStep<T>> steps = new Vector<LogicStep<T>>();
    private Vector<T> solutions = new Vector<T>();
    protected boolean debug = false;

    public void debug() { debug = true;}
    public List<T> GetSolutions() { return solutions; }
    public void addLogicStep(LogicStep<T> ls) { steps.add(ls); }
    protected void addSolution(T thing) { solutions.add(thing); }
    public int numSolutions() { return solutions.size(); }

    public LogicStatus ApplyLogic(T thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        for (LogicStep<T> step : steps)
        {
            LogicStatus status = step.apply(thing);
            if (status == LogicStatus.CONTRADICTION)
            {
                if (debug) System.out.println("Step " + step.toString() + " reported contradiction");
                return LogicStatus.CONTRADICTION;
            }
            if (status == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }
        return result;
    }

    public abstract void Solve(T start);
}
