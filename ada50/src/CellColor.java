/**
 * Created by chien on 12/10/2017.
 */
public enum CellColor
{
    UNKNOWN('.'),
    WHITE('o'),
    BLACK('x');

    private CellColor(char ch) { this.ch = ch; }
    private char ch;
    public char getChar() { return ch; }
}
