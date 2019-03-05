package grid.logic.flatten;

import java.util.Vector;

/**
 * Created by chien on 5/20/2017.
 */
public class FlattenSolvableTuple<T>
{
    public String code = null;
    public Vector<T> choices = new Vector<T>();
    public Vector<Object> antimoves = new Vector<Object>();

    public void addTuple(T choice,Object antimove)
    {
        choices.add(choice);
        antimoves.add(antimove);
    }

    public FlattenSolvableTuple(String s) { code = s; }
    public FlattenSolvableTuple() { this(null); }


    public FlattenSolvableTuple(T c1,Object m1, T c2, Object m2) { this(null,c1,m1,c2,m2); }
    public FlattenSolvableTuple(String s, T c1,Object m1, T c2, Object m2)
    {
        code = s;
        addTuple(c1,m2);
        addTuple(c2,m1);
    }

}