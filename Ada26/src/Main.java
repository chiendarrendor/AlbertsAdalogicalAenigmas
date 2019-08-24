import grid.logic.ContainerRuntimeException;
import grid.spring.GridFrame;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad Command Line");
            System.exit(1);
        }
	    Board b = new Board(args[0]);
        Solver s = new Solver(b);


        try {
            s.Solve(b);
            System.out.println("# of Solutions: " + s.GetSolutions().size());
        } catch(ContainerRuntimeException cre) {
            System.out.println("exception caught: " + cre);
            b = (Board)cre.getContained();
        }

        final Board fb = b;
        List<String> lar = new ArrayList<>();
        MyReference myr = new MyReference(fb,lar);


        String solver = fb.gfr.getVar("SOLVER");
        NearCenterSolution ncs = null;
        if (solver.equals("NEARCENTER1") || solver.equals("NEARCENTER2")) {
            ncs = new NearCenterSolution(fb);
            lar.add(ncs.getResult());
            lar.add(fb.gfr.getVar("SOLUTION1"));
            myr.addHelper(ncs);
        }

        if (solver.equals("NEARCENTER2")) {
            SecondNearCenterSolution sncs = new SecondNearCenterSolution(ncs,fb);
            lar.add(sncs.getResult());
            lar.add(fb.gfr.getVar("SOLUTION2"));
            myr.addHelper(sncs);
        }

        if (solver.equals("LIARSOLVER")) {
            LiarSolver ls = new LiarSolver(fb);
            lar.add(ls.getResult());
            lar.add(fb.gfr.getVar("SOLUTION"));
            myr.addHelper(ls);
        }



        GridFrame gf = new GridFrame("Adalogical Aenigma #26 Solver",1200,800,myr,myr);

    }
}
