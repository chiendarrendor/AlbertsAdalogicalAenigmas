/**
 * Created by chien on 7/25/2017.
 */
public class AlcazarRuntimeException extends RuntimeException
{
    Board b;
    public AlcazarRuntimeException(String msg, Board b)
    {
        super(msg);
        this.b = b;
    }
}
