import grid.letter.LetterRotate;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;
import grid.spring.ListMultiListener;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static class MyListener extends ListMultiListener<CrateBoard> {
        String[] lines;
        public MyListener(List<CrateBoard> blist, String[] lines) {
            super(blist);
            this.lines = lines;
        }

        @Override public int getNumXCells() { return b().getWidth(); }
        @Override public int getNumYCells() { return b().getHeight(); }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true;   }
        @Override public String[] getAnswerLines() { return lines; }

        private static int INITIALINSET = 17;
        private static int FINALINSET=19;
        private static Color DARKGREEN=new Color(0x17,0x9e,0x25);

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            if (b().hasLetter(cx,cy)) {
                GridPanel.DrawStringInCorner(bi, Color.BLACK,""+b().getLetter(cx,cy), Direction.NORTHWEST);
            }

            if (b().hasClue(cx,cy)) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,""+b().getClueSize(cx,cy)+b().getClueDirection(cx,cy).getSymbol(),Direction.NORTHEAST);
            }

            if (b().hasStartingBox(cx,cy)) {
                g.setColor(Color.BLACK);
                g.drawRect(INITIALINSET,INITIALINSET,bi.getWidth()-2*INITIALINSET,bi.getHeight()-2*INITIALINSET);
            }

            CrateShiftCellHolder csch = b().cells.getCell(cx,cy);
            StringBuffer sb = new StringBuffer();
            sb.append(csch.initialSize()).append("/")
                    .append(csch.intermediateSize()).append("/")
                    .append(csch.terminalSize());

            GridPanel.DrawStringInCorner(bi,Color.BLACK,sb.toString(),Direction.SOUTHWEST);

            if (csch.terminalSize() == 1 && b().isLocked(csch.getUniqueTerminal())) {
                g.setColor(DARKGREEN);
                g.fillRect(FINALINSET,FINALINSET,bi.getWidth()-2*FINALINSET,bi.getHeight()-2*FINALINSET);
            }

            if (csch.intermediateSize() == 1 && b().isLocked(csch.getUniqueIntermediate())) {
                Direction d = csch.getUniqueIntermediate().getDirection();
                g.setColor(DARKGREEN);
                g.setStroke(new BasicStroke(5.0f));
                if (d == Direction.NORTH || d == Direction.SOUTH) {
                    g.drawLine(bi.getWidth()/2,0,bi.getWidth()/2,bi.getHeight());
                } else {
                    g.drawLine(0,bi.getHeight()/2,bi.getWidth(),bi.getHeight()/2);
                }
            }

            if (b().terminalstatus.getCell(cx,cy) == TerminalState.MUSTHAVEBOX) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,"BB",Direction.SOUTHEAST);
            }

            if (b().terminalstatus.getCell(cx,cy) == TerminalState.MUSTBEEMPTY) {
                GridPanel.DrawStringInCorner(bi,Color.BLACK,"XX",Direction.SOUTHEAST);
            }



            return true;
        }


    }

    enum CellContents { EMPTY, INITIAL, INTERMEDIATE, TERMINAL, UNMOVING };
    private static void checkingSet(CellContainer<CellContents> container,int x,int y, CellContents cc) {
        if (container.getCell(x,y) != CellContents.EMPTY) throw new RuntimeException("Overlapping Shifts!");
        container.setCell(x,y,cc);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bad command line");
            System.exit(1);
        }

        List<CrateBoard> possibles = new ArrayList<>() ;
        CrateBoard b = new CrateBoard(args[0]);
        b.load("ada77solution.txt");

        /*
        for (int i = 0 ; i < 8 ; ++i) {
            CrateBoard bi = new CrateBoard(b);
            bi.load("solutions/ada77solution" + i + ".txt");
            possibles.add(bi);
        }

        for(CrateShift shift : b.shifts) {
            boolean found = true;
            for (CrateBoard possible : possibles) {
                if(!possible.isLocked(shift)) {
                    found = false;
                    break;
                }
            }
            if (found) b.checkingSet(shift);
        }
*/


        String[] lines = new String[] { "Adalogical Aenigma","#77 solver"};

       Solver s = new Solver(b);
       s.Solve(b);



/*
        while(true) {
            FlattenLogicer.RecursionStatus rs1 = s.recursiveApplyLogic(b);
            System.out.println("RAL 1: " + rs1);
            if (rs1 != FlattenLogicer.RecursionStatus.GO) break;

            LogicStatus ls = s.applyTupleSuccessors(b);
            System.out.println("ATS: " + ls);
            if (ls != LogicStatus.LOGICED) break;

            FlattenLogicer.RecursionStatus rs2 = s.recursiveApplyLogic(b);
            System.out.println("RAL 2: " + rs2);
            if (rs2 != FlattenLogicer.RecursionStatus.GO) break;
        }
*/

        if (s.GetSolutions().size() == 1) {
            System.out.println("Unique Solution Found");
            b = s.GetSolutions().get(0);

            CrateBoard fb = b;
            StringBuffer sb = new StringBuffer();
            fb.forEachCell((x,y)-> {
                if (!fb.hasLetter(x,y)) return;
                CrateShiftCellHolder csch = fb.cells.getCell(x,y);
                if (csch.initialSize() > 0 || csch.terminalSize() > 0) return;
                if (csch.intermediateSize() == 0) return;
                sb.append(LetterRotate.Rotate(fb.getLetter(x,y),csch.getUniqueIntermediate().getSize()));
            });
            lines[0] = sb.toString();
            lines[1] = fb.gfr.getVar("SOLUTION");
        }

        List<CrateBoard> result = new ArrayList<>();
        result.add(b);



        if (s.GetSolutions().size() > 1) {
            System.out.println("Multiple Solutions found!");

            int idx = 0;
            for (CrateBoard poss : s.GetSolutions()) {
//                poss.store("solutions/ada77solution" + idx++ + ".txt");
                try {
                    CellContainer<CellContents> cells = new CellContainer<CellContents>(poss.getWidth(), poss.getHeight(), (x, y) -> CellContents.EMPTY);

                    // ASSUMPTION!  the shifts have not been exhaustively validated to be straight lines
                    for (CrateShift shift : poss.shifts) {
                        if (!poss.isActive(shift)) continue;
                        if (!poss.isLocked(shift)) continue;
                        if (shift.getTerminal().equals(shift.getInitial())) {
                            checkingSet(cells, shift.getInitial().x, shift.getInitial().y, CellContents.UNMOVING);
                        } else {
                            checkingSet(cells, shift.getInitial().x, shift.getInitial().y, CellContents.INITIAL);
                            checkingSet(cells, shift.getTerminal().x, shift.getTerminal().y, CellContents.TERMINAL);
                            for (Point ip : shift.getIntermediates())
                                checkingSet(cells, ip.x, ip.y, CellContents.INTERMEDIATE);
                        }
                    }
                    // if we get here, the active and locked shifts do not overlap.
                    poss.forEachCell((x, y) -> {
                        if (!poss.hasStartingBox(x, y)) return;
                        if (cells.getCell(x, y) != CellContents.INITIAL && cells.getCell(x, y) != CellContents.UNMOVING)
                            throw new RuntimeException("Initial Box found with no shift");
                    });

                    // if we get here, every starting box was touched by an active locked shift.

                    // every clue not covered by a crate must point to the right number of crates

                    poss.forEachCell((x, y) -> {
                        if (!poss.hasClue(x, y)) return;
                        if (cells.getCell(x, y) == CellContents.TERMINAL) return;
                        int size = poss.getClueSize(x, y);
                        Direction d = poss.getClueDirection(x, y);
                        int count = 0;
                        for (int i = 1; ; ++i) {
                            Point np = d.delta(x, y, i);
                            if (!poss.onBoard(np)) break;
                            if (cells.getCell(np.x, np.y) == CellContents.TERMINAL || cells.getCell(np.x, np.y) == CellContents.UNMOVING)
                                ++count;
                        }
                        if (count != size) throw new RuntimeException("Clue is not fulfilled " + x + "," + y + " " + size + " " + d);
                    });
                    System.out.println("Board is good!");

                } catch (RuntimeException rex) {
                    System.out.println("Board is bad! " + rex );
                    System.out.println("Solver agreement? " + s.recursiveApplyLogic(poss));


                }
            }
            result = s.GetSolutions();
        }







        MyListener myl = new MyListener(result,lines);
        GridFrame gf = new GridFrame("Adalogical Aenigma #77 Solver",1200,800,myl);

    }


}
