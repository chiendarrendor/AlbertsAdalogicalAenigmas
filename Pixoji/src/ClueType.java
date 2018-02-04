import java.awt.*;

public enum ClueType
{
    BOX("BO",new char[] {'□'},new Color[] {Color.BLACK}),
    ARROW("AR",new char[] {'▶','▷'},new Color[] {Color.GREEN,Color.BLACK}),
    HEART("HE",new char[] {'♥','♡'},new Color[] {Color.RED,Color.BLACK}),
    CROWN("CR", new char[] {'♔'},new Color[] { Color.BLACK}),
    CAKE("CA",new char[] {'■','═','□'},new Color[] {Color.PINK,Color.WHITE,Color.BLACK}),
    SMILE("SM",new char[] {'☺'},new Color[] { Color.BLACK }),
    PAINT("PA",new char[] {'✎'},new Color[] { Color.BLACK }),
    CIRCLE("CI",new char[] {'◯'},new Color[] { Color.BLACK}),
    ;

    private String code;
    private char[] sigils;
    private Color[] colors;
    ClueType(String code,char[] sigils,Color[] colors)
    {
        this.code = code;
        this.sigils = sigils;
        this.colors = colors;

        if (colors.length != sigils.length) throw new RuntimeException("Illegal configuration in ClueType");
    }

    public String getCode() { return code; }
    public int getSigilCount() { return sigils.length; }
    public char getSigil(int x) { return sigils[x];}
    public Color getColor(int x) { return colors[x]; }

    public static ClueType getByCode(String code)
    {
        for (ClueType ct : ClueType.values())  if (code.equals(ct.getCode())) return ct;
        return null;
    }
}
