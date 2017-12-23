package grid.logic;

public interface LogicStep<R>
{
	LogicStatus apply(R thing);
}