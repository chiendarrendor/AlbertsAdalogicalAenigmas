import java.awt.*;

public class Clue
{
    String orig;
    ClueType ct;
    int size;

    boolean specialBlack;

    public Clue(String s)
    {
        orig = s;
        String tsub = s.substring(0,2);
        String ssub = s.substring(2);
        ct = ClueType.getByCode(tsub);
        if (ct == null) throw new RuntimeException("Illegal Clue Designator " + s);
        try
        {
            size = Integer.parseInt(ssub);
        }
        catch(NumberFormatException nfe)
        {
            throw new RuntimeException("Illegal Clue Designator Number " + s);
        }
        if (size < 0) throw new RuntimeException("Illegal Clue Designator Negative Number " + s);
        specialBlack = ct.getColor(ct.getSigilCount()-1) != Color.BLACK;
    }

    public int stringCount() { return ct.getSigilCount() + (specialBlack ? 1 : 0); }
    public Color stringColor(int x) { return x == ct.getSigilCount() ? Color.black : ct.getColor(x); }
    public String string(int x) { return "" + size + (x == ct.getSigilCount() ? "" : ct.getSigil(x)); }
    public String toString() { return orig; }
}
