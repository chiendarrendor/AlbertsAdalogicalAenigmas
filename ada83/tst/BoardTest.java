import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.spring.GridFrame;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    private static Board b;
    private static String[] lines;
    private static int curx = 1;

    private Board.CellProcessor[] doTest(Board b, EdgeState north, EdgeState south, EdgeState east, EdgeState west) {
        for (int y = 1; y <= 7; y += 2) {
            if (north != EdgeState.UNKNOWN) b.setEdge(curx, y, Direction.NORTH, north);
            if (south != EdgeState.UNKNOWN) b.setEdge(curx, y, Direction.SOUTH, south);
            if (east != EdgeState.UNKNOWN) b.setEdge(curx, y, Direction.EAST, east);
            if (west != EdgeState.UNKNOWN) b.setEdge(curx, y, Direction.WEST, west);
        }
        Board.CellProcessor[] result = new Board.CellProcessor[]{
                b.processCell(curx, 3, CellType.UNKNOWN),
                b.processCell(curx, 5, CellType.TERMINAL),
                b.processCell(curx, 7, CellType.INTERNAL)
        };
        curx += 2;
        return result;
    }

    @BeforeAll
    static void setUp() {
        b = new Board("test.txt");
        lines = new String[]{"Cell Processor Test", "Result"};
    }

    @AfterAll
    static void tearDown() {
        //Main.MyListener myl = new Main.MyListener(b, lines);
        //GridFrame gf = new GridFrame("Adalogical Aenigma #83 Cell Processor Test", 1400, 800, myl, myl);
    }

    @Test void allWalls() {
        Board.CellProcessor[] results = doTest(b, EdgeState.WALL, EdgeState.WALL, EdgeState.WALL, EdgeState.WALL);
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[0].result);
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[1].result);
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[2].result);
    }

    @Test void tooManyPaths() {
        Board.CellProcessor[] results = doTest(b,EdgeState.PATH,EdgeState.PATH,EdgeState.PATH,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[0].result);
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[1].result);
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[2].result);
    }

    @Test void twoPathBend() {
        Board.CellProcessor[] results = doTest(b,EdgeState.PATH,EdgeState.UNKNOWN,EdgeState.PATH,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[0].result);
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[1].result);
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[2].result);
    }

    @Test void twoPathStraightDone() {
        Board.CellProcessor[] results = doTest(b,EdgeState.PATH,EdgeState.PATH,EdgeState.WALL,EdgeState.WALL);
        Assertions.assertEquals(LogicStatus.STYMIED,results[0].result);
        Assertions.assertEquals(CellType.INTERNAL,results[0].ct);
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[1].result);
        Assertions.assertEquals(LogicStatus.STYMIED,results[2].result);
        Assertions.assertEquals(CellType.INTERNAL,results[2].ct);
    }

    @Test void twoPathStraightLogiced() {
        Board.CellProcessor[] results = doTest(b,EdgeState.PATH,EdgeState.PATH,EdgeState.UNKNOWN,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.LOGICED,results[0].result);
        Assertions.assertEquals(CellType.INTERNAL,results[0].ct);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[0].x,results[0].y,Direction.EAST));
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[0].x,results[0].y,Direction.WEST));
        Assertions.assertEquals(LogicStatus.CONTRADICTION, results[1].result);
        Assertions.assertEquals(LogicStatus.LOGICED,results[2].result);
        Assertions.assertEquals(CellType.INTERNAL,results[2].ct);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[2].x,results[2].y,Direction.EAST));
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[2].x,results[2].y,Direction.WEST));
    }

    @Test void threeWallDone() {
        Board.CellProcessor[] results = doTest(b,EdgeState.WALL,EdgeState.WALL,EdgeState.WALL,EdgeState.PATH);
        Assertions.assertEquals(LogicStatus.STYMIED,results[0].result);
        Assertions.assertEquals(CellType.TERMINAL,results[0].ct);
        Assertions.assertEquals(LogicStatus.STYMIED,results[1].result);
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);
        Assertions.assertEquals(LogicStatus.CONTRADICTION,results[2].result);
    }

    @Test void threeWallOpen() {
        Board.CellProcessor[] results = doTest(b,EdgeState.WALL,EdgeState.WALL,EdgeState.WALL,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.LOGICED,results[0].result);
        Assertions.assertEquals(CellType.TERMINAL,results[0].ct);
        Assertions.assertEquals(EdgeState.PATH,b.getEdge(results[0].x,results[0].y,Direction.WEST));
        Assertions.assertEquals(LogicStatus.LOGICED,results[1].result);
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);
        Assertions.assertEquals(EdgeState.PATH,b.getEdge(results[1].x,results[1].y,Direction.WEST));
        Assertions.assertEquals(LogicStatus.CONTRADICTION,results[2].result);
    }


    @Test void twoOppositeWallsNoPath() {
        Board.CellProcessor[] results = doTest(b,EdgeState.WALL,EdgeState.WALL,EdgeState.UNKNOWN,EdgeState.UNKNOWN);
        Assertions.assertEquals(CellType.UNKNOWN,results[0].ct);
        Assertions.assertEquals(LogicStatus.STYMIED,results[0].result);
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);
        Assertions.assertEquals(LogicStatus.STYMIED,results[1].result);
        Assertions.assertEquals(CellType.INTERNAL,results[2].ct);
        Assertions.assertEquals(EdgeState.PATH,b.getEdge(results[2].x,results[2].y,Direction.EAST));
        Assertions.assertEquals(EdgeState.PATH,b.getEdge(results[2].x,results[2].y,Direction.WEST));
    }

    @Test void twoAdjacentWallsNoPath() {
        Board.CellProcessor[] results = doTest(b,EdgeState.WALL,EdgeState.UNKNOWN,EdgeState.WALL,EdgeState.UNKNOWN);
        Assertions.assertEquals(CellType.TERMINAL,results[0].ct);
        Assertions.assertEquals(LogicStatus.STYMIED,results[0].result);
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);
        Assertions.assertEquals(LogicStatus.STYMIED,results[1].result);
        Assertions.assertEquals(LogicStatus.CONTRADICTION,results[2].result);
    }

    @Test void noPathsNoWalls() {
        Board.CellProcessor[] results = doTest(b,EdgeState.UNKNOWN,EdgeState.UNKNOWN,EdgeState.UNKNOWN,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.STYMIED,results[0].result);
        Assertions.assertEquals(CellType.UNKNOWN,results[0].ct);
        Assertions.assertEquals(LogicStatus.STYMIED,results[1].result);
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);
        Assertions.assertEquals(LogicStatus.STYMIED,results[2].result);
        Assertions.assertEquals(CellType.INTERNAL,results[2].ct);
    }

    @Test void oneWall() {
        Board.CellProcessor[] results = doTest(b,EdgeState.WALL,EdgeState.UNKNOWN,EdgeState.UNKNOWN,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.STYMIED,results[0].result);
        Assertions.assertEquals(CellType.UNKNOWN,results[0].ct);
        Assertions.assertEquals(LogicStatus.STYMIED,results[1].result);
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);
        Assertions.assertEquals(LogicStatus.STYMIED,results[2].result);
        Assertions.assertEquals(CellType.INTERNAL,results[2].ct);
    }

    @Test void onePathNoWalls() {
        Board.CellProcessor[] results = doTest(b,EdgeState.PATH,EdgeState.UNKNOWN,EdgeState.UNKNOWN,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.STYMIED,results[0].result);
        Assertions.assertEquals(CellType.UNKNOWN,results[0].ct);
        Assertions.assertEquals(LogicStatus.LOGICED,results[1].result);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[1].x,results[1].y,Direction.EAST));
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[1].x,results[1].y,Direction.WEST));
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[1].x,results[1].y,Direction.SOUTH));
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);
        Assertions.assertEquals(LogicStatus.LOGICED,results[2].result);
        Assertions.assertEquals(CellType.INTERNAL,results[2].ct);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[2].x,results[2].y,Direction.EAST));
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[2].x,results[2].y,Direction.WEST));
        Assertions.assertEquals(EdgeState.PATH,b.getEdge(results[2].x,results[2].y,Direction.SOUTH));
    }

    @Test void onePathOneWallOpposite() {
        Board.CellProcessor[] results = doTest(b,EdgeState.PATH,EdgeState.WALL,EdgeState.UNKNOWN,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.LOGICED,results[0].result);
        Assertions.assertEquals(CellType.TERMINAL,results[0].ct);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[0].x,results[0].y,Direction.EAST));
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[0].x,results[0].y,Direction.WEST));

        Assertions.assertEquals(LogicStatus.LOGICED,results[1].result);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[1].x,results[1].y,Direction.EAST));
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[1].x,results[1].y,Direction.WEST));
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);

        Assertions.assertEquals(LogicStatus.CONTRADICTION,results[2].result);
    }

    @Test void onePathOneWallAdjacent() {
        Board.CellProcessor[] results = doTest(b,EdgeState.PATH,EdgeState.UNKNOWN,EdgeState.WALL,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.STYMIED,results[0].result);
        Assertions.assertEquals(CellType.UNKNOWN,results[0].ct);
        Assertions.assertEquals(LogicStatus.LOGICED,results[1].result);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[1].x,results[1].y,Direction.EAST));
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[1].x,results[1].y,Direction.WEST));
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[1].x,results[1].y,Direction.SOUTH));
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);
        Assertions.assertEquals(LogicStatus.LOGICED,results[2].result);
        Assertions.assertEquals(CellType.INTERNAL,results[2].ct);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[2].x,results[2].y,Direction.EAST));
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[2].x,results[2].y,Direction.WEST));
        Assertions.assertEquals(EdgeState.PATH,b.getEdge(results[2].x,results[2].y,Direction.SOUTH));
    }

    @Test void onePathTwoWallsOpposite() {
        Board.CellProcessor[] results = doTest(b,EdgeState.WALL,EdgeState.WALL,EdgeState.PATH,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.STYMIED,results[0].result);
        Assertions.assertEquals(CellType.UNKNOWN,results[0].ct);

        Assertions.assertEquals(LogicStatus.LOGICED,results[1].result);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[1].x,results[1].y,Direction.WEST));
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);

        Assertions.assertEquals(LogicStatus.LOGICED,results[2].result);
        Assertions.assertEquals(CellType.INTERNAL,results[2].ct);
        Assertions.assertEquals(EdgeState.PATH,b.getEdge(results[2].x,results[2].y,Direction.WEST));
    }

    @Test void onePathTwoWallsAdjacent() {
        Board.CellProcessor[] results = doTest(b,EdgeState.WALL,EdgeState.PATH,EdgeState.WALL,EdgeState.UNKNOWN);
        Assertions.assertEquals(LogicStatus.LOGICED,results[0].result);
        Assertions.assertEquals(CellType.TERMINAL,results[0].ct);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[0].x,results[0].y,Direction.WEST));

        Assertions.assertEquals(LogicStatus.LOGICED,results[1].result);
        Assertions.assertEquals(EdgeState.WALL,b.getEdge(results[1].x,results[1].y,Direction.WEST));
        Assertions.assertEquals(CellType.TERMINAL,results[1].ct);

        Assertions.assertEquals(LogicStatus.CONTRADICTION,results[2].result);
    }



    // UNKNOWN,TERMINAL,INTERNAL
}
