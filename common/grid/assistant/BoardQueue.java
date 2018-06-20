package grid.assistant;

import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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



    private class BoardItem {
        private boolean isGuess = false;
        T orig;
        T cur;

        Map<Point,MovePair<T>> cellMovePairs = new HashMap<>();
        Map<EdgeKey,MovePair<T>> edgeMovePairs = new HashMap<>();

        public BoardItem(T b) { orig = b.clone(); cur = b.clone(); }

        public void addCellAntiMove(int x, int y, MovePair<T> pair) {
            Point k = new Point(x,y);
            if (pair.isNoOp()) cellMovePairs.remove(k);
            else cellMovePairs.put(k,pair);
        }

        public void addEdgeAntiMove(int x, int y, Direction d, MovePair<T> pair) {
            EdgeKey ek = new EdgeKey(x,y,d);
            if (pair.isNoOp()) edgeMovePairs.remove(ek);
            else edgeMovePairs.put(ek,pair);
        }

        public void clear() {
            cellMovePairs.clear();
            edgeMovePairs.clear();
            cur = orig.clone();
        }

        public void clearContradiction() {
            cur = orig.clone();
            isGuess = false;

            if (cellMovePairs.size() > 0) {
                Map.Entry<Point,MovePair<T>> ment = cellMovePairs.entrySet().iterator().next();
                cellMovePairs.remove(ment.getKey());
                MovePair<T> newpair = new MovePair<T>(ment.getValue().antimove,ment.getValue().move);
                cellMovePairs.put(ment.getKey(),newpair);
                ment.getValue().antimove.applyMove(cur);
            }

            if (edgeMovePairs.size() > 0) {
                Map.Entry<EdgeKey,MovePair<T>> ment = edgeMovePairs.entrySet().iterator().next();
                edgeMovePairs.remove(ment.getKey());
                MovePair<T> newpair = new MovePair<T>(ment.getValue().antimove,ment.getValue().move);
                edgeMovePairs.put(ment.getKey(),newpair);
                ment.getValue().antimove.applyMove(cur);
            }
        }

        public void save(PrintWriter pw,boolean hasNext) {
            pw.println("------");
            for (Map.Entry<EdgeKey,MovePair<T>> ent : edgeMovePairs.entrySet()) {
                pw.format("%d,%d,%s/%s/%s%n",
                        ent.getKey().getX(),ent.getKey().getY(),ent.getKey().getD(),
                        config.serialize(ent.getValue().move),
                        config.serialize(ent.getValue().antimove));
            }
            for (Map.Entry<Point,MovePair<T>> ent : cellMovePairs.entrySet()) {
                pw.format("%d,%d/%s/%s%n",
                        ent.getKey().x,ent.getKey().y,
                        config.serialize(ent.getValue().move),
                        config.serialize(ent.getValue().antimove));
            }


            if (hasNext) {
                pw.println(isGuess() ? "GUESS" : "LOGIC");
            }
        }

        public void load(String line) {
            String[] parts = line.split("/");
            String[] keyparts = parts[0].split(",");
            AssistantMove<T> move = config.deserialize(parts[1]);
            AssistantMove<T> antimove = config.deserialize(parts[2]);
            MovePair<T> pair = new MovePair<T>(move,antimove);
            move.applyMove(cur);
            if (keyparts.length == 2) {
                Point key = new Point(Integer.parseInt(keyparts[0]),Integer.parseInt(keyparts[1]));
                cellMovePairs.put(key,pair);
            } else if (keyparts.length == 3) {
                EdgeKey key = new EdgeKey(Integer.parseInt(keyparts[0]),Integer.parseInt(keyparts[1]),
                        Enum.valueOf(Direction.class,parts[2]));
                edgeMovePairs.put(key,pair);
            }
        }


        public int deltaCount() { return cellMovePairs.size() + edgeMovePairs.size(); }
        public boolean isGuess() { return isGuess; }
        public void setGuess() { isGuess = true; }



    }

    private List<BoardItem> queue = new ArrayList<>();
    private BoardItem getLast() { return queue.get(queue.size() - 1); }

    public T getCurOrig() { return getLast().orig; }
    public T getCurCur() { return getLast().cur; }
    public BoardHolder<T> getHolder() { return ()->getCurCur(); }

    public void addCellMovePair(int x, int y, MovePair<T> pair) {
        getLast().addCellAntiMove(x,y,pair);
    }
    public void addEdgeMovePair(int x, int y, Direction d, MovePair<T> pair) { getLast().addEdgeAntiMove(x,y,d,pair); }

    public String clearCur() {
        getLast().clear();
        return "Reset to State";
    }

    public String popCur() {
        if (getLast().deltaCount() > 0) { return clearCur();  }
        if (queue.size() == 1) return "Don't delete the initial board!";
        queue.remove(queue.size() - 1);
        return "State Removed";
    }

    public String doLogic() {
        T newboard = getLast().cur.clone();
        FlattenLogicer.RecursionStatus status = applyLogic(newboard);
        if (status != FlattenLogicer.RecursionStatus.DEAD) queue.add(new BoardItem(newboard));

        if (newboard.isComplete()) config.displaySolution(newboard);

        return  "Logic Status: " + status + (newboard.isComplete() ? "(ISCOMPLETE)" : "(NOTCOMPLETE)");
    }

    public String doGuess() {
        if (getLast().deltaCount() != 1) return "Guess requires exactly one delta";
        getLast().setGuess();
        return doLogic();
    }

    public String doContradiction() {
        if (queue.stream().noneMatch(qi->qi.isGuess())) return "Must have guess to select contradiction";
        while(!getLast().isGuess()) queue.remove(queue.size()-1);

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

    public void load() {
        try {
            Files.lines(Paths.get(SAVEFILE)).forEach(line->{
                System.out.println("Processing " + line);
                if (line.equals("GUESS")) doGuess();
                else if (line.equals("LOGIC")) doLogic();
                else if (line.equals("------")) return;
                else getLast().load(line);
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
