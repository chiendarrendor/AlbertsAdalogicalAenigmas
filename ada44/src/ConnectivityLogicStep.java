import grid.logic.LogicStatus;

/**
 * Created by chien on 6/13/2017.
 */
public class ConnectivityLogicStep implements grid.logic.LogicStep<Board>
{
    boolean useLetters;
    public ConnectivityLogicStep(boolean useLetters)
    {
        this.useLetters = useLetters;
    }

    public ConnectivityLogicStep()
    {
        this(false);
    }

    @Override
    public LogicStatus apply(Board b)
    {
        BoardCore thing = b.getBoardCore();
        DiagonalGridGraph dgg = new DiagonalGridGraph(thing,useLetters);
        return dgg.isConnected() ? LogicStatus.STYMIED : LogicStatus.CONTRADICTION;
    }
}
