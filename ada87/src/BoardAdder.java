import grid.logic.flatten.FlattenLogicer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

public class BoardAdder {
    public static void adder(Board b, Solver s, String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;

            while((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                // adding ability to put in comments
                if (line.charAt(0) == '#') continue;

                String[] parts = line.split("\\s+");
                if (parts[0].equals("pattern")) {
                    if (parts.length != 5) throw new RuntimeException("pattern code invalid");
                    char pid = parts[1].charAt(0);
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);
                    PatternCell pc = PatternCell.UNKNOWN;
                    if (parts[4].equals("inside") || parts[4].equals("outside")) {
                        pc = parts[4].equals("inside") ? PatternCell.INSIDE : PatternCell.OUTSIDE;
                    } else {
                        throw new RuntimeException("invalid patterncell in pattern code");
                    }
                    b.getPattern(pid).setCell(x, y, pc);
                } else if (parts[0].equals("run")) {
                    FlattenLogicer.RecursionStatus rs = s.recursiveApplyLogic(b);
                    b.patterns.show();
                    System.out.println("RS: " + rs);
                } else if (parts[0].equals("showregion")) {
                    if (parts.length != 3) throw new RuntimeException("showregion code invalid");
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    RegionSet regions = b.getRegionSet(x,y);
                    System.out.println("Regions of " + x + " " + y);
                    for (RegionId rid : regions.getRegions()) {
                        System.out.println("  " + rid);
                    }
                } else {
                    throw new RuntimeException("Unknown directive type " + parts[0]);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
