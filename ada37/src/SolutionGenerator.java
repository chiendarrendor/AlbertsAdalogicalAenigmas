import grid.letter.LetterRotate;

public class SolutionGenerator {
    public static String generateAda37Solution(Board b) {
        StringBuffer sb = new StringBuffer();
        for (int x = 0 ; x < b.getWidth() ; ++x) {
            int shadecount = 0;
            for (int y = 0 ; y < b.getHeight() ; ++y) {
                CellData cd = b.getCellData(x,y);
                if (cd.isWall()) continue;
                if (!b.isShaded(x,y)) continue;
                shadecount += cd.getValue();
            }
            sb.append(LetterRotate.Rotate('A',shadecount-1));
        }
        return sb.toString();
    }

    public static String generateAddendum32Solution(Board b) {
        int numfours = 0;
        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0 ; x < b.getWidth() ; ++x) {
                CellData cd = b.getCellData(x,y);
                numfours += cd.getValue() == 4 ? 1 : 0;
            }
        }

        StringBuffer sb = new StringBuffer();
        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0; x < b.getWidth(); ++x) {
                CellData cd = b.getCellData(x, y);
                if (cd.getValue() != 4) continue;
                if (!b.hasLetter(x, y)) continue;
                sb.append(LetterRotate.Rotate(b.getLetter(x, y), numfours));
            }
        }
        return sb.toString();
    }
}
