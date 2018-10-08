package grid.solverrecipes.singleloopflatten;

// this package is an implementation of a partially abstract FlattenSolvable (aka 'board')
// and concomitant other classes in order to provide logical processing for the following common subproblem:
//
// given paths that go horizontally and vertically between cells (and therefore do not cross)
// the path part of the solution will consist, ultimately, of a single path that loops back onto itself.

// to use:
// make your Board object subclass of SingleLoopBoard
// implement required getWidth() and getHeight()
// implement methods of FlattenSolvable, or have your class implement Simple/MultiFlattenSolvable
// SingleLoopBoard will automatically keep track of edges still unused in getUnusedCount()
// add to your Flatten Logicer Logic Step addition logic:
//        CellPathLogicStep.generateLogicSteps(this,b,true);
//        // the boolean argument to this call is whether or not the problem demands that every cell be part of the path
//        addLogicStep(new SinglePathLogicStep<Board>());
