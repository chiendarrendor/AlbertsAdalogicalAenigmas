package grid.solverrecipes.genericloopyflatten;

public class SingleLoopTest {
    public static void main(String[] args) {
        LoopyBoard lb = new LoopyBoard();
        lb.addEdge("A","E","AE");
        lb.addEdge("A","B","AB");
        lb.addEdge("B","C","BC");
        lb.addEdge("C","D","CD");
        lb.addEdge("B","D","BD");
        lb.addEdge("D","E","DE");
        lb.addEdge("A","F","AF");
        lb.addEdge("E","F","EF");

        SingleLoopLogicStep slls = new SingleLoopLogicStep();
        slls.debug();

        // test 1, multple chains
        LoopyBoard t1 = new LoopyBoard(lb);

        t1.setEdge("AF",LineState.PATH);
        t1.setEdge("EF",LineState.PATH);
        t1.setEdge("CD",LineState.PATH);
        t1.setEdge("BD",LineState.PATH);
        System.out.println("Test: Two disconnected chains");
        System.out.println("T1: " + slls.apply(t1));

        // test 2, triple conn
        LoopyBoard t2 = new LoopyBoard(t1);
        t2.setEdge("DE",LineState.PATH);
        System.out.println("Test: one vertex triple-connected");
        System.out.println("T2: " + slls.apply(t2));

        // test 3, loop by itself
        LoopyBoard t3 = new LoopyBoard(lb);
        t3.setEdge("EF",LineState.PATH);
        t3.setEdge("AF",LineState.PATH);
        t3.setEdge("AE",LineState.PATH);
        System.out.println("Test: one loop");
        System.out.println("T3: " + slls.apply(t3));

        // test 4, loop and a string
        LoopyBoard t4 = new LoopyBoard(t3);
        t4.setEdge("BC",LineState.PATH);
        System.out.println("Test: loop + 1 chain");
        System.out.println("T4: " + slls.apply(t4));

        // test 5, two loops
        LoopyBoard t5 = new LoopyBoard(t4);
        t5.setEdge("CD",LineState.PATH);
        t5.setEdge("BD",LineState.PATH);
        System.out.println("Test: two loops");
        System.out.println("T5: " + slls.apply(t5));


        System.exit(0);
    }
}
