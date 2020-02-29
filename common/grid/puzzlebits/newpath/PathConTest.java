package grid.puzzlebits.newpath;

import java.awt.Point;

public class PathConTest {

    public static class MyAssistant implements PathContainer.PathContainerAssistant {

        @Override public int getWidth() { return 10; }
        @Override public int getHeight() { return 7; }

        private int ICEX = 4;
        private int ICEY = 3;

        @Override public boolean isLinkable(PathContainer.Port porta, PathContainer.Port portb) {
            if (porta.getLocation().x == ICEX && porta.getLocation().y == ICEY) {
                return porta.getDirection() == portb.getDirection().getOpp();
            }
            return true;
        }

        @Override public int maxPorts(int x, int y) {
            if (x == ICEX && y == ICEY) {
                return 4;
            }
            return 2;
        }

        @Override public int maxUnlinked(int x, int y) {
            if (x == ICEX && y == ICEY) {
                return 2;
            }
            return 0;
        }
    }



    public static void main(String[] args) {
        PathContainer pc1 = new PathContainer(new MyAssistant());
        pc1.newPair(new Point(3,3),new Point(4,3),false);
        pc1.newPair(new Point(3,2),new Point(3,3),true);
        pc1.newPair(new Point(4,2),new Point(4,3),true);
        pc1.newPair(new Point(4,4),new Point(4,3),false);
        pc1.newPair(new Point(5,3),new Point(4,3),true);
        pc1.newPair(new Point(5,4),new Point(5,3),true);
        pc1.newPair(new Point(5,4),new Point(4,4),true);

        System.out.println("Clean: " + pc1.clean());
        PathContainer pc2 = new PathContainer(pc1);
        pc2.newPair(new Point(3,1),new Point(3,2),false);
        System.out.println("pc2 clean: " + pc2.clean());
        PathContainer pc3 = new PathContainer(pc2);
        pc3.newPair(new Point(4,1),new Point(4,2),true);
        pc3.newPair(new Point(4,1),new Point(3,1),false);
        System.out.println("pc3 clean: " + pc3.clean());

        System.out.println(pc1);
        System.out.println(pc2);
        System.out.println(pc3);


    }
}
