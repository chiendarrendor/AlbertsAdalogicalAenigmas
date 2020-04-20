import grid.file.GridFileReader;
import grid.letter.LetterRotate;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;
import grid.spring.GridPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    private static class MyListener implements GridPanel.GridListener {
        CellContainer<CellPair> cells;
        CellContainer<Boolean> inword;
        String solution;
        public MyListener(CellContainer<CellPair> cells, CellContainer<Boolean> inword, String s) {
            this.cells = cells;
            this.inword = inword;
            solution = s;
        }
        @Override public int getNumXCells() { return 10; }
        @Override public int getNumYCells() { return 10; }
        @Override public boolean drawGridNumbers() { return true; }
        @Override public boolean drawGridLines() { return true; }
        @Override public boolean drawBoundary() { return true; }
        @Override public String[] getAnswerLines() { return new String[] { "X-Games","Decathlon!",solution}; }

        @Override public boolean drawCellContents(int cx, int cy, BufferedImage bi) {
            Graphics2D g = (Graphics2D)bi.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0,0,bi.getWidth(),bi.getHeight()/2);
            CellPair cp = cells.getCell(cx,cy);

            if (cp.greyStrip != null) {
                GridPanel.DrawStringInCorner(bi,cp.greyStrip.getColor(cx,cy),
                        "" + cp.greyStrip.getChar(cx,cy),Direction.SOUTH);
            }

            if (cp.whiteStrip != null) {
                g.setColor(cp.whiteStrip.getColor(cx,cy));
                if (!inword.getCell(cx,cy)) {
                    g.setFont(g.getFont().deriveFont(5.0f * g.getFont().getSize()));
                }
                GridPanel.DrawStringInCorner(g,0,0,bi.getWidth(),bi.getHeight(),
                        "" + cp.whiteStrip.getChar(cx,cy),Direction.NORTH);
            }



            return true;
        }


    }


    private static void showStripofStrips(CellContainer<CellPair> cells,Direction opdir,Direction stripdir) {
        for (int i = 0 ; i < 10 ; ++i) {
            Point curp = opdir.delta(0,0,i);
            CellPair cpair = cells.getCell(curp.x,curp.y);
            StripPtr cpointer = cpair.whiteStrip.stripdir == stripdir ? cpair.whiteStrip : cpair.greyStrip;
            System.out.println("  " + cpointer.s + (cpointer.reversed ? "(reversed)" : ""));
        }
    }



    private static void showSolution(CellContainer<CellPair> cells) {
        System.out.println("Horizontal Strips:");
        showStripofStrips(cells,Direction.SOUTH,Direction.EAST);
        System.out.println("Vertical Strips:");
        showStripofStrips(cells,Direction.EAST,Direction.SOUTH);

    }




    // given the strip location (stripidx,stripdir)
    // tries every strip in strips (both forwards and backwards) in that strip location,
    // where a strip will fit if, for every cell, either the cell is empty, or it contains the opposite white state
    //   to the corresponding location on the strip.
    // for every fitting strip (added to cells, removed from strips) it will then recurse to the next strip location
    // (for (idx,EAST), next location is (idx,SOUTH), and for (idx,SOUTH), next is (idx+1,EAST)
    //
    // if strips is empty when we start, we've put all strips on the board, and this is a solution!  copy it to solutions.
    //
    // after trying and recursing each strip, it should be removed from cells and added again to strips


    private static boolean[] bar = new boolean[] { true, false };
    private static int maxdepth = 0;


    public static void processOneStrip(CellContainer<CellPair> cells, List<CellContainer<CellPair>> solutions,
                                       Set<Strip> strips,int stripidx,Direction stripdir) {

        if (strips.size() == 0) {
            solutions.add(new CellContainer<CellPair>(cells));
            System.out.println("Solution Achieved");
            showSolution(cells);
            return;
        }

        int depthcount = 2 * stripidx + (stripdir == Direction.SOUTH ? 1 : 0);
        if (depthcount > maxdepth) { maxdepth = depthcount; System.out.println("Max depth achieved: " + maxdepth); }


        Set<Strip> iterset = new HashSet<>();
        iterset.addAll(strips);

        for (Strip strip : iterset) {
            strips.remove(strip);
            for (boolean reversal : bar) {
                StripPtr curptr = new StripPtr(strip,reversal,stripdir);

                Point startp;
                Direction godir;

                if (stripdir == Direction.EAST) {
                    godir = reversal ? Direction.WEST : Direction.EAST;
                    startp = new Point(reversal ? cells.getWidth() - 1 : 0,stripidx);
                } else {
                    godir = reversal ? Direction.NORTH : Direction.SOUTH;
                    startp = new Point(stripidx,reversal ? cells.getHeight() - 1 : 0);
                }

                // check to see if current strip fits on cells starting at startp and going in godir
                boolean broken = false;
                for (int i = 0 ; i < 10 ; ++i) {
                    Point curp = godir.delta(startp,i);
                    CellPair cp = cells.getCell(curp.x,curp.y);
                    boolean stripIsWhite = strip.isWhiteList.get(i);
                    if (cp.greyStrip == null && cp.whiteStrip == null) continue;
                    if (stripIsWhite && cp.whiteStrip == null) continue;
                    if (!stripIsWhite && cp.greyStrip == null) continue;

                    if (stripIsWhite && cp.greyStrip != null) {
                        if (cp.greyStrip.getChar(curp.x,curp.y) != '.' &&
                                cp.greyStrip.getChar(curp.x,curp.y) != strip.characterList.get(i)) continue;
                    }

                    if (!stripIsWhite && cp.whiteStrip != null) {
                        if (cp.whiteStrip.getChar(curp.x,curp.y) != '.' &&
                                cp.whiteStrip.getChar(curp.x,curp.y) != strip.characterList.get(i)) continue;
                    }



                    broken = true;
                    break;
                }
                if (broken) continue;

                //    add strip to cells
                for (int i = 0 ; i < 10 ; ++i) {
                    Point curp = godir.delta(startp,i);
                    CellPair cp = cells.getCell(curp.x,curp.y);
                    boolean stripIsWhite = strip.isWhiteList.get(i);

                    if (stripIsWhite) cp.whiteStrip = curptr;
                    else cp.greyStrip = curptr;
                }

                //    recurse
                Direction nextd = stripdir == Direction.EAST ? Direction.SOUTH : Direction.EAST;
                int nextid = stripdir == Direction.EAST ? stripidx : stripidx+1;
                processOneStrip(cells,solutions,strips,nextid,nextd);

                //    remove strip from cells
                for (int i = 0 ; i < 10 ; ++i) {
                    Point curp = godir.delta(startp,i);
                    CellPair cp = cells.getCell(curp.x,curp.y);
                    boolean stripIsWhite = strip.isWhiteList.get(i);

                    if (stripIsWhite) cp.whiteStrip = null;
                    else cp.greyStrip = null;
                }
            }

            strips.add(strip);
        }
    }

    public static void findWord(CellContainer<CellPair> cells,String sstring,
                                CellContainer<Boolean> inword) {
        System.out.println("Searching for " + sstring);
        for(int y = 0 ; y < cells.getHeight(); ++y) {
            for (int x = 0; x < cells.getWidth(); ++x) {
                for (Direction d : Direction.values()) {
                    boolean missed = false;
                    for (int len = 0 ; len < sstring.length() ; ++len) {
                        Point p = d.delta(x,y,len);
                        if (!cells.onBoard(p)) { missed = true; break; }
                        if (cells.getCell(p.x,p.y).whiteStrip.getChar(p.x,p.y) != sstring.charAt(len)) { missed = true; break; }
                    }

                    if (missed) continue;
                    System.out.println("Found " + sstring);
                    for (int len = 0 ; len < sstring.length() ; ++len) {
                        Point p = d.delta(x,y,len);
                        inword.setCell(p.x,p.y,true);
                    }

                    return;
                }
            }
        }
    }



    public static void wordSearch(GridFileReader gfr,CellContainer<CellPair> cells,
                                  CellContainer<Boolean> inword) {
        int wordcount = Integer.parseInt(gfr.getVar("SEARCHCOUNT"));
        for (int i = 0 ; i < wordcount ; ++i) {
            String sstring = gfr.getVar("SEARCH" + i);

            findWord(cells,sstring,inword);


        }
    }




    public static void main(String[] args) {
        GridFileReader gfr = new GridFileReader("xgames.txt");

        Set<Strip> strips = new HashSet<>();
        for (int y = 0 ; y < gfr.getHeight() ; ++y) {
            Strip s = new Strip();
            for (int x = 0 ; x < gfr.getWidth() ; ++x) {
                s.add(gfr.getBlock("STRIPS")[x][y]);
            }
            strips.add(s);
        }

        CellContainer<CellPair> cells = new CellContainer<CellPair>(10,10,
                (x,y)->new CellPair(),
                (x,y,r)->new CellPair(r));

        Strip found = null;
        for (Strip s : strips) {
            if (s.toString().equals(gfr.getVar("TOPSTRIP"))) {
                found = s;
                break;
            }
        }

        boolean isreversed = false;
        Point startp = new Point(0,0);
        if (gfr.getVar("TOPREVERSED").equals("true")) {
            isreversed = true;
            startp = new Point(9,0);
        }



        strips.remove(found);
        StripPtr firstptr = new StripPtr(found,isreversed,Direction.EAST);
        for (int i = 0 ; i < 10 ; ++i) {
            Point p = Direction.EAST.delta(startp,i);
            CellPair cp = cells.getCell(p.x,p.y);
            if (found.isWhiteList.get(i)) cp.whiteStrip = firstptr;
            else cp.greyStrip = firstptr;
        }





        List<CellContainer<CellPair>> solutions = new ArrayList<>();

        processOneStrip(cells,solutions,strips,0, Direction.SOUTH);

        System.out.println("Solution Size: " + solutions.size());

        CellContainer<CellPair> solution = solutions.get(0);

        CellContainer<Boolean> inWord = new CellContainer<Boolean>(solution.getWidth(),solution.getHeight(),(x,y)->false);

        wordSearch(gfr,solution,inWord);

        StringBuffer sb = new StringBuffer();
        
        int wcount = 0;
        for (int y = 0 ; y < inWord.getHeight() ; ++y) {
            for (int x = 0 ; x < inWord.getWidth(); ++x) {
                if (inWord.getCell(x,y)) continue;
                ++wcount;

                if (wcount <= 14) {
                    sb.append(solution.getCell(x,y).whiteStrip.getChar(x,y));
                    if (wcount == 14) sb.append(": ");
                } else {
                    Color c = solution.getCell(x,y).whiteStrip.getColor(x,y);
                    int col = c.getRGB() & 0xffffff;
                    sb.append("<font color=\"#" + Integer.toHexString(col) + "\">");
                    System.out.println("Color: " + c);
                    System.out.println(Integer.toHexString(col));
                    sb.append(LetterRotate.Rotate(solution.getCell(x, y).whiteStrip.getChar(x, y), 10));
                    sb.append("</font>");
                }
            }
        }




        GridFrame gf = new GridFrame("X-Games Solver/Visualizer",900,900, new MyListener(solution,inWord,sb.toString()));
    }


}
