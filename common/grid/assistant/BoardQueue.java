package grid.assistant;

import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class BoardQueue<T extends AssistantBoard<T>> {
    private SolverAssistantConfig<T> config;
    private final FlattenLogicer<T> logicer;

    private FlattenLogicer.RecursionStatus applyLogic(T thing) {

        FlattenLogicer.RecursionStatus r1 = logicer.recursiveApplyLogic(thing);
        if (r1 != FlattenLogicer.RecursionStatus.GO) return r1;

        while(true) {
            LogicStatus lstat = logicer.applyTupleSuccessors(thing);
            if (lstat == LogicStatus.CONTRADICTION) return FlattenLogicer.RecursionStatus.DEAD;
            if (lstat == LogicStatus.STYMIED) return FlattenLogicer.RecursionStatus.GO;

            FlattenLogicer.RecursionStatus r2 = logicer.recursiveApplyLogic(thing);
            if (r2 != FlattenLogicer.RecursionStatus.GO) return r2;
        }
    }

    private enum ItemState {
        OPEN(false),   //  if we haven't tried to apply logic to this
        LOGIC(false),  // non-guess, logic successfully applied
        GUESS(false),  //  this is a guess, logic status unknown (very short lived state)
        GUESSED(true), // this is a guess, and we have successfully applied logic to it.
        CONTRADICTION(true), // this is a guess, and the immediate logic for it failed
        DEAD(false),   // non-guess, logic failed
        DONE(false); // current state is a solution
        private boolean guessResult;
        private ItemState(boolean gr) { guessResult = gr; }
        public boolean isGuessResult() { return guessResult; }
    }
    // OPEN, CONTRADICTION, DEAD, and DONE are the only allowed states for the end of the queue
    // LOGIC and GUESSED _must_ be followed up immediately by adding another state
    // GUESS _must_ be followed up immediately by a transition to either GUESSED or CONTRADICTION


    private class BoardItem {
        private ItemState state = ItemState.OPEN;
        T orig;
        T cur;

        List<MovePair<T>> movePairs = new ArrayList<>();

        public ItemState getState() {
            return state;
        }
        public void setState(ItemState state) { this.state = state; }

        public BoardItem(T b) { orig = b.clone(); cur = b.clone(); }

        public void addAntiMove(MovePair<T> pair) {
            movePairs.add(pair);
        }

        public void clear() {
            movePairs.clear();
            cur = orig.clone();
            state = ItemState.OPEN;
        }

        public void clearContradiction() {
            cur = orig.clone();
            state = ItemState.OPEN;

            // we do a contradiction only on a guess, which should only have one entry.
            if (movePairs.size() > 0) {
                MovePair<T> ment = movePairs.get(0);
                movePairs.clear();
                MovePair<T> newment = new MovePair<T>(ment.antimove,ment.move);
                movePairs.add(newment);
                ment.antimove.applyMove(cur);
            }
        }

        public void save(PrintWriter pw,boolean hasNext) {
            pw.println("------");
            for (MovePair<T> mp : movePairs) {
                pw.format("%s/%s%n",config.serialize(mp.move),config.serialize(mp.antimove));
            }
            pw.println(state);
        }

        public void load(String line) {
            String[] parts = line.split("/");
            AssistantMove<T> move = config.deserialize(parts[0]);
            AssistantMove<T> antimove = config.deserialize(parts[1]);
            MovePair<T> pair = new MovePair<T>(move,antimove);
            move.applyMove(cur);
            addAntiMove(pair);
            // state is loaded by higher-level process.
        }


        public int deltaCount() { return movePairs.size(); }
    }

    private List<BoardItem> queue = new ArrayList<>();
    private BoardItem getLast() { return queue.get(queue.size() - 1); }

    public T getCurOrig() { return getLast().orig; }
    public T getCurCur() { return getLast().cur; }
    public BoardHolder<T> getHolder() { return ()->getCurCur(); }


    public void addMovePair(MovePair<T> pair) {
        if (getLast().getState() != ItemState.OPEN) return;
        getLast().addAntiMove(pair);
        pair.move.applyMove(getCurCur());
    }

    public String clearCur() {
        getLast().clear();
        return "Reset to State";
    }

    public String popCur() {
        if (getLast().deltaCount() > 0) { return clearCur();  }
        if (queue.size() == 1) return "Don't delete the initial board!";
        queue.remove(queue.size() - 1);
        getLast().setState(ItemState.OPEN);
        return "State Removed";
    }

    public String doLogic() {
        T newboard = getLast().cur.clone();
        FlattenLogicer.RecursionStatus status = applyLogic(newboard);
        switch(status) {
            case DEAD:
                if (getLast().getState() == ItemState.OPEN) getLast().setState(ItemState.DEAD);
                else if (getLast().getState() == ItemState.GUESS) getLast().setState(ItemState.CONTRADICTION);
                else throw new RuntimeException("How did we get state " + getLast().getState() + " in doLogic?");
                break;
            case DONE:
            case GO:
                if (getLast().getState() == ItemState.OPEN) getLast().setState(ItemState.LOGIC);
                else if (getLast().getState() == ItemState.GUESS) getLast().setState(ItemState.GUESSED);
                else throw new RuntimeException("How did we get state " + getLast().getState() + " in doLogic?");

                queue.add(new BoardItem(newboard));
                if (newboard.isComplete()) {
                    getLast().setState(ItemState.DONE);
                    config.displaySolution(newboard);
                }
                break;
        }


        return  "Logic Status: " + status + (newboard.isComplete() ? "(ISCOMPLETE)" : "(NOTCOMPLETE)");
    }

    public String doGuess() {
        if (getLast().deltaCount() != 1) return "Guess requires exactly one delta";
        getLast().setState(ItemState.GUESS);
        return doLogic();
    }

    public String doContradiction() {
        if (queue.stream().noneMatch(qi->qi.getState().isGuessResult())) return "Must have guess to select contradiction";
        while(!getLast().getState().isGuessResult()) queue.remove(queue.size()-1);

        getLast().clearContradiction();
        return doLogic();
    }

    private static final String SAVEFILE="save.txt";
    public String save() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(SAVEFILE));
            for (int i = 0 ; i < queue.size() ; ++i) {
                queue.get(i).save(pw,i < queue.size() - 1);
            }
            pw.close();
        } catch(IOException ioe) {
            return "Can't write save file: " + ioe;
        }
        return "saved";
    }

    private ItemState parseState(String s) {
        try {
            return ItemState.valueOf(s);
        } catch(IllegalArgumentException iae) {
            return null;
        }
    }


    public void load() {
        try {
            Files.lines(Paths.get(SAVEFILE)).forEach(line->{
                System.out.println("Processing " + line);
                if (line.equals("------")) return;
                ItemState is = parseState(line);
                if (is == null) {
                    getLast().load(line);
                    return;
                }

                switch (is) {
                    case OPEN:
                    case CONTRADICTION:
                    case DEAD:
                    case DONE:
                        // these don't require any work...either logic has not been applied, or it has and it has failed.
                        getLast().setState(is);
                        break;
                    case GUESS:
                        // this should never exist long enough to be written to a file.
                        throw new RuntimeException("How did GUESS end up in the save file?");
                    case LOGIC:
                        doLogic();
                        break;
                    case GUESSED:
                        doGuess();
                        break;
                }
            });
        } catch(IOException ioe) {
           throw new RuntimeException("Can't load save file: " + ioe);
        }
    }




    public BoardQueue(SolverAssistantConfig<T> config) {
        queue.add(new BoardItem(config.getInitialBoard()));
        this.config = config;
        this.logicer = config.getLogicer();

        load();
    }

}
