import java.awt.Color;

public enum GroundState {
    TILE(false,false,'?',Color.WHITE,null),
    WALL(false,true,'?',Color.BLACK,null),
    CYAN_TARGET(true,true,'C',Color.CYAN,LightState.REDLIGHT),
    MAGENTA_TARGET(true,true,'P',Color.MAGENTA,LightState.GREENLIGHT),
    YELLOW_TARGET(true,true,'Y',Color.YELLOW,LightState.BLUELIGHT);
    private boolean isTarget;
    private boolean blocksLight;
    private char colorLetter;
    private Color color;
    private LightState anticolor;
    private GroundState(boolean t, boolean b,char cl,Color col,LightState anticolor) {
        isTarget = t;blocksLight = b; colorLetter = cl; color = col; this.anticolor = anticolor;
    }
    public boolean isTarget() { return isTarget; }
    public boolean blocksLight() { return blocksLight; }
    public char getColorLetter() { return colorLetter; }
    public Color getColor() { return color; }
    public LightState getAnticolor() {
        return anticolor;
    }
}
