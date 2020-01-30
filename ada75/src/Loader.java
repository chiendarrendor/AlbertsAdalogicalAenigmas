import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Loader {
    public static void load(Board b,String fname) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(fname)));

            String line;

            while((line = br.readLine()) != null) {
                String[] parts = line.split(" +");
                if (parts.length != 4) throw new RuntimeException("Load line doesn't have 4 parts");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                Direction d = Direction.fromShort(parts[2]);
                EdgeState es = parts[3].equals("PATH") ? EdgeState.PATH : EdgeState.WALL;
                b.setEdge(x,y,d,es);
            }

        } catch (IOException e) {
            System.out.println("Can't load file");
            System.exit(1);
        }
    }
}
