import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {

    public Solver(Board b) {
        b.holes.keySet().stream().forEach((p)->{
            addLogicStep(new HoleLogicStep(p));
            Hole h = b.holes.get(p);
            if (!h.isFixedLength()) {
                addLogicStep(new CipherLogicStep(p));
            }
        });

        if (b.getCharMap().getCiphers().size() > 1) {
            addLogicStep(new CipherUniquenessLogicStep());
        }
    }


}
