import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.SubReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.puzzlebits.CellContainer;
import javafx.scene.effect.Light;

import java.awt.Point;

public class SubBoard {
    @Shallow SubReader sr;
    @Shallow int unknowns;
    @Deep CellContainer<LightState> lights;

    public SubBoard(SubReader sr) {
        this.sr = sr;
        unknowns = 0;

        LambdaInteger unk = new LambdaInteger(0);

        lights = new CellContainer<LightState>(getWidth(),getHeight(),(x,y)-> {
            if (isBlocker(x,y)) {
                return LightState.BLOCKED;
            } else {
                unk.inc();
                return LightState.UNKNOWN;
            }
        });

        unknowns = unk.get();
    }

    public SubBoard(SubBoard right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return sr.getWidth(); }
    public int getHeight() { return sr.getHeight(); }
    public String getString(int x, int y) { return sr.getCell("GRID",x,y); }
    public boolean isNumber(int x,int y) { return Character.isDigit(getString(x,y).charAt(0)); }
    public boolean isEmptyBlocker(int x,int y) { return getString(x,y).charAt(0) == '.'; }
    public boolean isBlocker(int x,int y) { return isNumber(x,y) || isEmptyBlocker(x,y); }
    public int getNumber(int x,int y) { return Integer.parseInt(getString(x,y)); }
    public boolean inBounds(int x,int y) { return sr.inBounds(x,y); }
    public boolean inBounds(Point p) { return sr.inBounds(p); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public int getUnknowns() { return unknowns; }
    public LightState getLightState(int x,int y) { return lights.getCell(x,y); }
    public void setLightState(int x,int y,LightState ls) { --unknowns; lights.setCell(x,y,ls); }





}
