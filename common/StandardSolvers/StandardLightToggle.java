package StandardSolvers;

import OnTheFlyAStar.AStar;
import OnTheFlyAStar.AStarNode;

import java.util.*;

/**
 * This class is an implementation of the AStarNode to solve one of the very common
 * 'turn all the lights on' puzzle formats where the lights start in some on/off configuration
 * and there is a set of buttons that each toggle some of the lights when pressed.
 * (often the lights and the buttons are the same thing, but that's not necessary)
 * If the buttons do other things, like on->off has different behavior than off->on, or
 * the button press does something other than toggle, then this is not the class to use.
 */

public class StandardLightToggle implements AStarNode<StandardLightToggle> {
    boolean[] lights;
    Map<String,int[]> buttons;
    String button = null;

    @Override
    public int winGrade() {
        return lights.length;
    }

    @Override
    public int getGrade() {
        int count = 0;
        for (boolean b : lights) if (b) ++count;
        return count;
    }

    @Override
    public String getCanonicalKey() {
        StringBuffer sb = new StringBuffer();
        for (boolean b: lights) sb.append(b ? 1 : 0);
        return sb.toString();
    }

    @Override
    public List<StandardLightToggle> successors() {
        List<StandardLightToggle> result = new ArrayList<>();
        for (String bid : buttons.keySet()) result.add(new StandardLightToggle(this,bid));
        return result;
    }

    private void toggle(int idx) {
        lights[idx] = !lights[idx];
    }


    public StandardLightToggle(StandardLightToggle right,String button) {
        lights = Arrays.copyOf(right.lights,right.lights.length);
        buttons = right.buttons;
        this.button = button;

        for (int i : buttons.get(button)) toggle(i);
    }

    public StandardLightToggle(boolean[] initiallights) {
        lights = Arrays.copyOf(initiallights,initiallights.length);
        buttons = new HashMap<>();
    }

    public void addButton(String bid,int ... toggles) {
        buttons.put(bid,toggles);
    }

    public AStar.AStarSolution<StandardLightToggle> go() {
        return AStar.execute(this);
    }

    public String getButton() { return button; }

}
