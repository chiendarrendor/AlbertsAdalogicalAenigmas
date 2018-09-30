package grid.assistant;

// This package combines aspects of the grid.spring libraries and the logic.flatten libraries
// to create a GUI entity that will allow the user to operate through the game logic themselves.

// to use:
//
// your Board object must implement AssistantBoard<Board>
// (which is FlattenSolvable<Board> plus a clone() method)
//
// you must have a FlattenLogicer<Board> object capable of detecting if a given board is
// Contradiction or not
// (if your FlattenLogicer is capable of actual solution, then why are you using assistant?)
//
// you must have a class implementing SolverAssistantConfig<Board> (see details below)
//
// instanciate SolverAssistantFrame<Board>(<title>,<window width>,<window height>,<SolverAssistantConfig>)
//
// EdgeClicker
// CellClicker
// -- These objects need to implement a function click(), taking 3 objects
//    T orig -- a board object prior to any clicking on it
//    T cur  -- a board object that changes dynamically when you click on it
//    ClickInfo -- details about the click that occured:
//      cellx,celly,d  (which cell (or edge)) was clicked on?
//      cellwidth, cellheight (how many pixels wide/tall is a cell?)
//      dxloc,dyloc (how far from the cell upper left was the click) (not sure if this works for edges)
//      MouseEvent (what was the actual mouse event that fired?)  (could use for left/right click, etc)
//
//    These objects should do one of a few things:
//      if click would represent a modification to the orig board, do nothing and return null
//      if click would represent a modification to the cur board, return a
//        MovePair<> object containing the move/antimove pair associated with the move.
//        the move and antimove should be objects of type AssistantMove
// SolverAssistantConfig:
// getLogicer() must return an object implementing FlattenLogicer, as described above
// getInitialBoard() must return  your initial board
// getGridListener() must return a GridListener (standard Grid drawer)
// getEdgeListener() must return an EdgeListener (standard grid edge drawer)
// -- note, if there are any ui elements needed for clicking, this should draw them
// -- Both of these Listeners must take an object of type BoardHolder (layer of indirection for Board objects)
// getCellClicker()
// getEdgeClicker()
//   should return a CellClicker/EdgeClicker object as described above

