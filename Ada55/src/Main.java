import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;

import grid.spring.ClickableGridPanel;
import grid.spring.SinglePanelFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

import static grid.logic.flatten.FlattenLogicer.RecursionStatus.DEAD;
import static grid.logic.flatten.FlattenLogicer.RecursionStatus.GO;

public class Main {

    private static class LambdaButton extends JButton {
        private class MyActionListener implements ActionListener {
            private Runnable r;
            public MyActionListener(Runnable r) { this.r = r; }
            public void actionPerformed(ActionEvent e) { r.run(); }
        }
        public LambdaButton(String title,Runnable action) { super(title); addActionListener(new MyActionListener(action));}
    }





    private static FlattenLogicer.RecursionStatus rstat; // DEAD, DONE, GO
    private static Board newboard;

    private static void applyLogic(Board b) {
        newboard = new Board(b);

        Solver s = new Solver(b);
        while(true) {
            rstat = s.recursiveApplyLogic(newboard);
            if (rstat != GO) return;

            LogicStatus lstat = s.applyTupleSuccessors(newboard);
            if (lstat == LogicStatus.CONTRADICTION) {
                rstat = DEAD;
                return;
            }
            if (lstat == LogicStatus.STYMIED) {
                return;
            }

            rstat = s.recursiveApplyLogic(newboard);
            if (rstat != GO) return;
        }
    }





    private static class UIState {
        Board orig;
        Board live;
        Board antiguess;
        FlattenLogicer.RecursionStatus status;
        public UIState(Board b, FlattenLogicer.RecursionStatus status) {
            orig = b;
            live = new Board(b);
            this.status = status;
        }

        int dx;
        int dy;
        boolean disV;
        EdgeState des;
        private void setD(int x,int y,boolean isv,EdgeState es) { dx = x; dy = y; disV = isv; des = es; }
        public void makeAntiGuess() {
            antiguess = new Board(orig);
            antiguess.setEdge(dx,dy,disV,des == EdgeState.WALL ? EdgeState.PATH : EdgeState.WALL);
        }

        public int deltaCount() {
            int[] counter = new int[1];
            counter[0] = 0;
            live.getEdges().forEachEdge((x,y,isV,es) -> {
                if (es != orig.getEdges().getEdge(x,y,isV)) {
                    ++counter[0];
                    setD(x,y,isV,es);
                }
            });
            return counter[0];
        }

        public void write(PrintWriter pw,boolean hasNext) {
            pw.println("------");
            live.getEdges().forEachEdge((x,y,isV,es) -> {
                if (es != orig.getEdges().getEdge(x,y,isV)) {
                    pw.format("%d %d %b %s%n",x,y,isV,es);
                }
            });
            if (antiguess != null) {
                antiguess.getEdges().forEachEdge((x,y,isV,es) -> {
                    if (es != orig.getEdges().getEdge(x,y,isV)) {
                        pw.format("(%d %d %b %s)%n",x,y,isV,es);
                    }
                });


                if (hasNext) pw.println("GUESS");
            }
            else if (hasNext) pw.println("LOGIC");
        }
    }


    private static Vector<UIState> states = new Vector<>();
    private static UIState curstate() { return states.lastElement(); }
    private static DisplayListeners.BoardHolder boardholder = ()->curstate().live;

    private static ClickableGridPanel cgp = null;
    private static JLabel statuslabel = null;

    private static void status(String s) { statuslabel.setText(s); }
    private static void repaint() { if (cgp != null) cgp.getParams().repaint(); }

    private static void addState(UIState uis) {
        states.add(uis);
        repaint();
    }

    private static void clean() {
        curstate().live = new Board(curstate().orig);
        curstate().antiguess = null;
        status("Reset to State");
        repaint();
    }


    private static void pop() {
        if (curstate().deltaCount() > 0) clean();
        else {
            if (states.size() == 1) {
                status("Don't delete the initial board!");
                return;
            }
            states.remove(curstate()) ;
            status("State removed");
            repaint();
        }
    }

    private static void doLogic() {
        status("Starting Logic...");
        applyLogic(curstate().live);
        status("Logic Status: " + rstat + (curstate().live.isComplete() ? " (ISCOMPLETE!)" : " (NOTCOMPLETE)"));
        if (rstat == DEAD) return;
        addState(new UIState(newboard,rstat));
    }

    private static void doGuess() {
        if (curstate().deltaCount() != 1) {
            status("Guess requires exactly one delta");
            return;
        }
        curstate().makeAntiGuess();
        doLogic();
    }

    private static void doContradiction() {
        int guessidx = -1;
        for (int i = 0 ; i < states.size() ; ++i) if (states.get(i).antiguess != null) guessidx = i;
        if (guessidx == -1) {
            status("must have guess to play contradiction");
            return;
        }
        while(states.size() > guessidx+1) states.remove(curstate());
        curstate().live = curstate().antiguess;
        curstate().antiguess = null;
        doLogic();
    }

    private static final String SAVEFILE="save.txt";
    private static void doSave()  {
        status("Save start");
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(SAVEFILE));
            for (int i = 0 ; i < states.size() ; ++i) {
                UIState uis = states.get(i);
                uis.write(pw,i < states.size() - 1);
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Can't write save file!");
            System.exit(1);
        }
        status("Save End");
    }

    private static void load() {
        try {
            Files.lines(Paths.get(SAVEFILE)).forEach(line->{
                if (line.equals("GUESS")) {} //doGuess();
                else if (line.equals("LOGIC")) {} //doLogic();
                else if (line.equals("------")) {  return; }
                else if (line.charAt(0) == '(') return;
                else {
                    String[] parts = line.split(" ");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    boolean isV = Boolean.parseBoolean(parts[2]);
                    EdgeState es = Enum.valueOf(EdgeState.class,parts[3]);

                    curstate().live.setEdge(x,y,isV,es);
                    System.out.format("Edge specified: %d %d %b %s%n",x,y,isV,es);
                }
            });
        } catch (IOException e) {
            System.out.println("Can't load save file!");
        }
    }






    public static void main(String[] args) {
	    if (args.length != 1) {
	        System.out.println("Bad command line, need filename");
	        System.exit(1);
        }

        Board b = new Board(args[0]);


	    states.add(new UIState(b,null));

        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        cgp = new ClickableGridPanel(1000,700,
                new DisplayListeners.MyGridListener(boardholder),
                new DisplayListeners.MyEdgeListener(boardholder));
        jp.add(cgp,BorderLayout.NORTH);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel,BoxLayout.Y_AXIS));
        statuslabel = new JLabel("Test");
        statuslabel.setFont(new Font("SansSerif",Font.BOLD,16));
        statusPanel.add(statuslabel);


        JPanel buttonPanel = new JPanel();
        statusPanel.add(buttonPanel);
        buttonPanel.setLayout(new FlowLayout());
        jp.add(statusPanel,BorderLayout.SOUTH);


        cgp.addEdgeClicker((x,y,d)->{
            UIState uis = curstate();
            Board orig = uis.orig;
            Board live = uis.live;

            if (orig.getEdge(x,y,d) != EdgeState.UNKNOWN) return;
            switch(live.getEdge(x,y,d)) {
                case WALL: live.setEdge(x,y,d,EdgeState.PATH); break;
                case PATH: live.setEdge(x,y,d,EdgeState.UNKNOWN); break;
                case UNKNOWN: live.setEdge(x,y,d,EdgeState.WALL); break;
            }
            repaint();
        });

        load();


        System.out.println("Load complete");
        Board nb = curstate().live;
        Solver s = new Solver(nb);
        System.out.println("Validation Solve Start");
        System.out.println("Terminal? " + nb.isComplete());
        s.Solve(nb);
        System.out.println("Solutions found: " + s.GetSolutions().size());

        Board solution = s.GetSolutions().get(0);
        SolutionShower.show(solution);



        buttonPanel.add(new LambdaButton("Clear" , () -> clean() ));
        buttonPanel.add(new LambdaButton("Logic", () -> doLogic() ));
        buttonPanel.add(new LambdaButton("Backup",() -> pop() ));
        buttonPanel.add(new LambdaButton("Guess",()-> doGuess() ));
        buttonPanel.add(new LambdaButton("Contradiction",() -> doContradiction() ));
        buttonPanel.add(new LambdaButton("Save", ()-> doSave() ));



        SinglePanelFrame spf = new SinglePanelFrame(args[0] + " Solver",jp);


    }


}

