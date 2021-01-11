import java.awt.Point;

public class SolutionValidator {
    public static void ValidateSolution(Board b) {
        boolean ok = true;
        Board.CellType testcells[][] = new Board.CellType[b.width][b.height];
        for (int y = 0 ; y < b.height ; ++y) {
            for (int x = 0 ; x < b.width ; ++x) {
                testcells[x][y] = Board.CellType.UNKNOWN;
            }
        }

        for (int y = 0 ; y < b.height ; ++y) {
            int oppy = b.height  - 1 - y;
            for (int x = 0 ; x < b.width ; ++x) {
                int oppx = b.width - 1 - x;
                Board.CellInfo ci = b.cells[x][y];
                Board.CellInfo oci = b.cells[oppx][oppy];
                if (ci.containsToken != oci.containsToken) ok = false;
                if (!ci.containsToken) continue;
                Point sp = ci.tokenOriginalPosition;
                Point ep = ci.tokenCurrentPosition;
                if (ci.tokenMoveDist >= 0) {
                    if (sp.x != ep.x && sp.y != ep.y) {
                        System.out.println("Non linear movement!");
                        ok = false;
                    }
                    if (Math.abs(sp.y - ep.y) + Math.abs(sp.x - ep.x) != ci.tokenMoveDist) {
                        System.out.println("invalid movement!");
                        ok = false;
                    }
                }
                int dx = 0;
                int dy = 0;
                int dist = Math.abs(sp.y - ep.y) + Math.abs(sp.x - ep.x);
                if (ep.y - sp.y != 0) dy = ep.y-sp.y > 0 ? 1 : -1;
                if (ep.x - sp.x != 0) dx = ep.x-sp.x > 0 ? 1 : -1;
                for (int d = 0 ; d <= dist ; ++d) {
                    int px = sp.x + dx*d;
                    int py = sp.y + dy*d;
                    if (testcells[px][py] != Board.CellType.UNKNOWN) {
                        System.out.println("Cell movement can't be placed! "
                                + d + " " + dist + " " + sp.x + "," + sp.y);
                        ok = false;
                    }
                    else testcells[px][py] = Board.CellType.TARGET;
                }


            }
        }
        System.out.println("Solution is valid: " + ok);
    }
}
