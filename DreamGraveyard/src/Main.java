import OnTheFlyAStar.AStar;
import StandardSolvers.StandardLightToggle;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        StandardLightToggle slt = new StandardLightToggle(new boolean[]
                { false,true,true,false,false,true,false,true,false,false,false,false});
        slt.addButton("0",0,2,9);
        slt.addButton("1",1,4,6);
        slt.addButton("2",1,2,9);
        slt.addButton("3",0,3,10);
        slt.addButton("4",4,8,11);
        slt.addButton("5",3,5,7);
        slt.addButton("6", 5,6,10);
        slt.addButton("7",0,7,8);
        slt.addButton("8",2,8,11);
        slt.addButton("9",1,6,9);
        slt.addButton("A",3,5,10);
        slt.addButton("B",4,7,11);

        AStar.AStarSolution<StandardLightToggle> solblock = slt.go();
        List<StandardLightToggle> solution = solblock.solution;

        StringBuffer msb = new StringBuffer();
        solution.stream().forEach(sol -> {
            System.out.println("move:" + sol.getButton());
            System.out.println("state: " + sol.getCanonicalKey());
            msb.append(sol.getButton());
        });
        System.out.println(msb.toString());

    }
}
