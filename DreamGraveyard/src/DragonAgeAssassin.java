import OnTheFlyAStar.AStar;
import StandardSolvers.StandardLightToggle;

import java.util.List;
import java.util.Vector;

public class DragonAgeAssassin {

    private static int idxFromId(int x,int y) {
        return y * 5 + x;
    }

    public static void main(String[] args) {

        StandardLightToggle slt = new StandardLightToggle(new boolean[]
            { true,true,true,true,true,
              true,true,true,true,true,
              false,false,true,true,true,
              true,true,true,true,true,
              true,true,true,true,true });

        // 0 1 2 3 4
        // 5 6 7 8 9
        // 10 11 12 13 14
        // 15 16 17 18 19
        // 20 21 22 23 24
        for (int x = 0 ; x < 5 ; ++x) {
            for (int y = 0; y < 5 ; ++y) {
                Vector<Integer> v = new Vector();
                v.add(idxFromId(x,y));
                if (y > 0) v.add(idxFromId(x,y-1));
                if (y < 4) v.add(idxFromId(x,y+1));
                if (x > 0) v.add(idxFromId(x-1,y));
                if (x < 4) v.add(idxFromId(x+1,y));
                int[] far = new int[v.size()];
                for (int i = 0 ; i < v.size() ; ++i) far[i] = v.get(i);
                slt.addButton(""+x+","+y,far);
            }
        }



        AStar.AStarSolution<StandardLightToggle> solblock = slt.go();
        List<StandardLightToggle> solution = solblock.solution;

        StringBuffer msb = new StringBuffer();
        solution.stream().forEach(sol -> {
            System.out.println("move:" + sol.getButton());
            System.out.println("state: " + sol.getCanonicalKey());
            msb.append(sol.getButton());
        });
        System.out.println(msb.toString()+"  ");

    }
}


