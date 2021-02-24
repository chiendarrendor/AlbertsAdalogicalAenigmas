import grid.puzzlebits.Direction;
import grid.spring.GridFrame;

import java.awt.Point;

public class BugCatcher {

    public static void main(String[] args) {
        Board b = new Board("empty.txt");

        b.setEdge(1,2,Direction.NORTH,EdgeType.NOTPATH);
        b.setEdge(1,2,Direction.WEST,EdgeType.NOTPATH);
        b.setEdge(1,2,Direction.EAST,EdgeType.PATH);
        b.setEdge(1,2, Direction.SOUTH,EdgeType.PATH);
        b.setEdge(1,3,Direction.WEST,EdgeType.NOTPATH);
        b.setEdge(1,3,Direction.SOUTH,EdgeType.NOTPATH);
        b.setEdge(1,3,Direction.EAST,EdgeType.PATH);
        b.setEdge(2,3, Direction.NORTH,EdgeType.PATH);
        b.setEdge(2,3,Direction.SOUTH,EdgeType.NOTPATH);
        b.setEdge(2,3,Direction.EAST,EdgeType.NOTPATH);

        b.ps.MergeAll();
        System.out.println("Still Dirty: ");
        for(Point p : b.ps.dirty) { System.out.println("  " + p); }

        System.out.println("Paths: ");
        for (Path pth : b.ps.paths) {
            System.out.println("Path : " + pth.getPathId() + " " + pth.endOneDir() + " " + pth.endTwoDir());
            for (Point p : pth.cells) System.out.println("  " + p);
        }

        b.ps.paths.iterator().next().reverse();

        b.setEdge(2,1,Direction.SOUTH,EdgeType.PATH);

        System.out.println("Second Merge");

        b.ps.MergeAll();
        System.out.println("Still Dirty: ");
        for(Point p : b.ps.dirty) { System.out.println("  " + p); }

        System.out.println("Paths: ");
        for (Path pth : b.ps.paths) {
            System.out.println("Path : " + pth.getPathId() + " " + pth.endOneDir() + " " + pth.endTwoDir());
            for (Point p : pth.cells) System.out.println("  " + p);
        }





        String[] lines = new String[] {  "Bug","Catcher" };
        GridFrame gf = new GridFrame("Adalogical Aenigma #" + b.getSolverID(),1200,900,new MyListener(b,lines));
    }


}
