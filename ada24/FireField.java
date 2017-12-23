

// this class will calculate out the set of fires from a given Path and Point.
// A fire is successful if it meets its other end with exactly the given
// number of mirrors.   if it exits anywhere else, or runs out of 
// mirrors, the fire is unsuccessful.
// if a fire hits an unknown space, it could be SLASH, BACKSLASH or EMPTY
// if a fire hits a GENERICMIRROR space it could be SLASH or BACKSLASH
// if a fire has no mirrors left, all unknown must be empty, and all GENERICMIRROR spaces are failures

import java.awt.Point;
import java.util.*;


public class FireField
{
    Board theBoard;


    private Point GetInitialMomentum(Point startingPoint)
    {
        Point result = new Point();
        if (startingPoint.y == -1) result.y = 1;
        else if (startingPoint.y == theBoard.height) result.y = -1;
        else result.y = 0;

        if (startingPoint.x == -1) result.x = 1;
        else if (startingPoint.x == theBoard.width) result.x = -1;
        else result.x = 0;

        return result;
    }

    private void ChangeMomentum(CellState cs,Point momentum)
    {
        switch(cs)
        {
        case SLASH:
            if (momentum.x == 0 && momentum.y == -1) { momentum.x = 1 ; momentum.y = 0; }
            else if (momentum.x == 0 && momentum.y == 1) { momentum.x = -1 ; momentum.y = 0; }
            else if (momentum.x == 1 && momentum.y == 0) { momentum.x = 0; momentum.y = -1; }
            else if (momentum.x == -1 && momentum.y == 0) { momentum.x = 0 ; momentum.y = 1; }
            break;
        case BACKSLASH:
            if (momentum.x == 0 && momentum.y == -1) { momentum.x = -1 ; momentum.y = 0; }
            else if (momentum.x == 0 && momentum.y == 1) { momentum.x = 1 ; momentum.y = 0; }
            else if (momentum.x == 1 && momentum.y == 0) { momentum.x = 0; momentum.y = 1; }
            else if (momentum.x == -1 && momentum.y == 0) { momentum.x = 0 ; momentum.y = -1; }
            break;	
        }
    }





    public Vector<FirePath> paths = new Vector<FirePath>();

    public class QueueElement
    {
        FirePath thePath;
        Point curLocation;
        Point momentum;
        int mirrorcount = 0;

        public QueueElement(FirePath fp,Point loc, Point delta) { thePath = fp; curLocation = new Point(loc) ; momentum = new Point(delta); }
        public QueueElement(QueueElement right)
        {
            thePath = new FirePath(right.thePath);
            curLocation = new Point(right.curLocation);
            momentum = new Point(right.momentum);
            mirrorcount = right.mirrorcount;
        }
        public void Move() { curLocation.x += momentum.x; curLocation.y += momentum.y; }
        public boolean onBoard() { return theBoard.onBoard(curLocation.x,curLocation.y); }
    }

    public FireField(State state,Board.Path path, Point startP)
    {
        this(state,path,startP,true);
    }
    
    
    public FireField(State state, Board.Path path,Point startP,boolean allowGuessing)
    {
            Vector<QueueElement> queue = new Vector<>();
            theBoard = state.myBoard;

            if (!startP.equals(path.p1) && !startP.equals(path.p2)) throw new RuntimeException("Given Point not terminal of path!");
            Point terminal = startP.equals(path.p1) ? path.p2 : path.p1;
            QueueElement qe = new QueueElement(new FirePath(),startP,GetInitialMomentum(startP));
            queue.add(qe);

            // elements in queue we know nothing about, so everything gets checked.
            while(queue.size() > 0)
            {
                    QueueElement curel = queue.remove(0);
                    Point curp = curel.curLocation;
//                    System.out.println("current Loc: " + curp.x + "," + curp.y + " queue.size: " + queue.size() + " solution size: " + paths.size());

                    if (!curel.onBoard() && curel.thePath.isEmpty())  // this means that we just started.
                    {
                            curel.Move();
                            queue.add(curel);
                            continue;
                    }

                    if (!curel.onBoard())
                    {
                            if (!terminal.equals(curel.curLocation)) continue;
                            if (curel.mirrorcount != path.numMirrors) continue;
                            paths.add(curel.thePath);
                            continue;
                    }

                    // if we get here, we're on the board.
                    // let's start with choiceless things.
                    CellState curcs = state.state[curp.x][curp.y];



                    if (curcs.noChoice())
                    {
                            if (curcs.isMirror()) 
                            {
                                    ++curel.mirrorcount;
                                    if (curel.mirrorcount > path.numMirrors) continue;
                            }
                            ChangeMomentum(curcs,curel.momentum);
                            curel.thePath.add(new PathElement(curp,curcs,false));
                            curel.Move();
                            queue.add(curel);
                            continue;
                    }

                    // if we get here, we have a choice to make.
                    boolean doStraight = false;
                    boolean doBackslash = false;
                    boolean doSlash = false;
                    boolean canBounce = curel.mirrorcount < path.numMirrors;

                    if (curcs == CellState.UNKNOWN)
                    {
                            doStraight = true;
                            if (canBounce && allowGuessing) { doBackslash = true ; doSlash = true; }
                    }
                    else if (curcs == CellState.GENERICMIRROR)
                    {
                        if (canBounce && allowGuessing) { doBackslash = true ; doSlash = true; }
                    }

                    if (doBackslash)
                    {
                            QueueElement bsqe = new QueueElement(curel);
                            ++bsqe.mirrorcount;
                            bsqe.thePath.add(new PathElement(curp,CellState.BACKSLASH,true));
                            ChangeMomentum(CellState.BACKSLASH,bsqe.momentum);
                            bsqe.Move();
                            queue.add(bsqe);
                    }

                    if (doSlash)
                    {
                            QueueElement bsqe = new QueueElement(curel);
                            ++bsqe.mirrorcount;
                            bsqe.thePath.add(new PathElement(curp,CellState.SLASH,true));
                            ChangeMomentum(CellState.SLASH,bsqe.momentum);
                            bsqe.Move();
                            queue.add(bsqe);
                    }			

                    if (doStraight)
                    {
                            curel.thePath.add(new PathElement(curp,CellState.EMPTY,true));
                            curel.Move();
                            queue.add(curel);
                    }
            }
    }
}