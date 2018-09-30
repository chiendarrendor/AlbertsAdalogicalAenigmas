import java.awt.Color;

public enum LightState {
    NOLIGHT(Color.BLACK),
    REDLIGHT(Color.RED),
    GREENLIGHT(Color.GREEN),
    BLUELIGHT(Color.BLUE);
    private LightState(Color c) { color = c; }
    private Color color;
    public Color getColor() { return color; }
}
