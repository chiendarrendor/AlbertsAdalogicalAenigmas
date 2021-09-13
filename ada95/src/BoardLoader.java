import grid.puzzlebits.Direction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BoardLoader {
    public static void Load(String fname,Board board) {
        try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
            String line;
            while((line = br.readLine()) != null) {
                if (line.length() == 0 || line.charAt(0) == '#') continue;
                String[] parts = line.split("\\s+");
                if (parts.length != 4) throw new RuntimeException("bad load line");
                if (parts[0].equals("DISTRICT")) {
                    board.getDistrict(Integer.parseInt(parts[1]),Integer.parseInt(parts[2])).setNumber(Integer.parseInt(parts[3]));
                } else if (parts[0].equals("WALL")) {
                    board.setEdge(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]), Direction.fromShort(parts[3]),EdgeState.WALL);
                } else if (parts[0].equals("PATH")) {
                    board.setEdge(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]), Direction.fromShort(parts[3]),EdgeState.PATH);
                } else {
                    throw new RuntimeException("Bad load line");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
