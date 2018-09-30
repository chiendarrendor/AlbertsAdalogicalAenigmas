import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BetterClueLogicStep implements LogicStep<Board>  {
    List<Integer> clues;
    List<Point> cells;
    int maxnum;
    List<List<Integer>> groups = new ArrayList<>();
    
    
    public BetterClueLogicStep(List<Integer> clues, List<Point> cells, int maxnum, List<Rectangle>boxes) {
        this.clues = clues;
        this.cells = cells;
        this.maxnum = maxnum;
        
        for (Rectangle r : boxes) {
            List<Integer> group = new ArrayList<>();
            for (int i = 0 ; i < cells.size() ; ++i) {
                Point p = cells.get(i);
                if (r.contains(p)) group.add(i);
            }
            if (group.size() > 0) groups.add(group);
        }
        
        
    }

    // characters: W = wall, X = null, 1-6 = numbers, . = not processed yet
    private class Option {
        char[] celloptions;
        public Option() { celloptions = new char[0]; }
        public Option(Option o,char c) {
            celloptions = new char[o.celloptions.length + 1];
            System.arraycopy(o.celloptions,0,celloptions,0,o.celloptions.length);
            celloptions[o.celloptions.length] = c;
        }

    }



    @Override public LogicStatus apply(Board thing) {
        List<Option> options = new ArrayList<>();
        options.add(new Option());

        for (int i = 0 ; i < cells.size() ; ++i) {
            Point p = cells.get(i);
            boolean terminal = i == cells.size() - 1;
            List<Option> newlist = new ArrayList<>();
            Cell c = thing.getCell(p.x,p.y);
            List<Character> addlist = new ArrayList<>();
            if (c == null) {
                addlist.add('X');
            } else {
                if (c.isBroken()) return LogicStatus.CONTRADICTION;
                if (c.contains(Cell.WALLID)) addlist.add('W');
                for (int j = 1 ; j <= maxnum ; ++j) {
                    if (c.contains(j)) addlist.add((char)('1'+j-1));
                }
            }
            for (Option o : options) {
                for (char newc : addlist) {
                    Option no = new Option(o,newc);
                    boolean fail = false;
                    for (List<Integer> group : groups) {
                        if (!filterDuplicates(no,group)) {
                            fail = true;
                            break;
                        }
                    }
                    if (fail) continue;
                    if (!filterClues(no,terminal)) continue;
                    newlist.add(no);
                }
            }
            options = newlist;
            if (options.size() > 40000) break;
        }

//        int ocount = 0;
//        for (Option o : options) {
//            System.out.println(o.celloptions);
//            ++ocount;
//        }
//        System.out.println("total: "+ ocount);
//
//        if (ocount > -1) return  LogicStatus.STYMIED;

        List<Set<Character>> unionSet = new ArrayList<>();
        for (Option o : options) {
            for (int i = 0 ; i < o.celloptions.length ; ++i) {
                if (unionSet.size() < i+1) unionSet.add(new HashSet<Character>());
                unionSet.get(i).add(o.celloptions[i]);
            }
        }

        if (options.size() == 0) return LogicStatus.CONTRADICTION;
        LogicStatus result = LogicStatus.STYMIED;

        for (int i = 0 ; i < unionSet.size() ; ++i) {
            Point p = cells.get(i);
            Cell c = thing.getCell(p.x,p.y);
            Set<Character> cset = unionSet.get(i);
            if (c == null) continue;

            if (cset.size() == 0) return LogicStatus.CONTRADICTION;
            if (c.isBroken()) return LogicStatus.CONTRADICTION;

            if (c.canBeWall() && !cset.contains('W')) {
                c.makeNotWall();
                result = LogicStatus.LOGICED;
            }

            for (int ti = 1 ; ti <= maxnum ; ++ti) {
                char tich = (char)('1' + ti - 1);
                if (c.contains(ti) && !cset.contains(tich)) {
                    c.remove(ti);
                    result = LogicStatus.LOGICED;
                }
            }
        }

        return result;
    }



    private boolean filterClues(Option no,boolean isterminal) {
        int clueidx = -1;
        boolean inClue = false;
        int cluesum = 0;
        for (char c : no.celloptions) {
            switch(c) {
                case 'W':
                case 'X':
                    if (inClue == false) continue;
                    inClue = false;
                    if (clues.get(clueidx) == -1) continue;
                    if (cluesum != clues.get(clueidx)) return false;
                    break;
                default:
                    int num = c - '1' + 1;
                    if (inClue == false) {
                        inClue = true;
                        ++clueidx;
                        cluesum = num;
                        if (clueidx >= clues.size()) return false;
                    } else {
                        cluesum += num;
                    }
                    if (clues.get(clueidx) > 0 && cluesum > clues.get(clueidx)) return false;
            }
        }

        if (!isterminal) return true;
        if (clueidx == -1 && clues.size() > 0) return false;
        if (clues.get(clueidx) != -1 && cluesum != clues.get(clueidx)) return false;
        return clueidx+1 == clues.size();

    }

    private boolean filterDuplicates(Option no, List<Integer> group) {
        Set<Character> duplicates = new HashSet<>();
        for (int i : group) {
            if (i >= no.celloptions.length) continue;
            switch(no.celloptions[i]) {
                case 'W':
                case 'X':
                    // do nothing
                    break;
                default:
                    if (duplicates.contains(no.celloptions[i])) return false;
                    duplicates.add(no.celloptions[i]);
                    break;
            }
        }

        return true;
    }


    public void test() {
        Option no = new Option();
        no.celloptions = "632W451".toCharArray();
        System.out.println("non-terminal for 632W451: " + filterClues(no,false));
        System.out.println("terminal for 632W451: " + filterClues(no,true));

    }

}
