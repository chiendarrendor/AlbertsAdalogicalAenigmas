/**
 * Created by chien on 8/18/2017.
 */
public class BadBoardRuntimeException extends RuntimeException
{
    public Board lb;
    public BadBoardRuntimeException(String message,Board b) { super(message); lb = b; }
}
