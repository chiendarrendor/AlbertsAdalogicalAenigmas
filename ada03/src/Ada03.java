import grid.file.GridFileReader;

public class Ada03
{
    public static void main(String[] args) {
	    // I will be using the Alan Rodriguez Belmonte solving assistant for solving this:
        // this program will output the Nurikabe solving assistant XML save format.

        GridFileReader gfr = new GridFileReader("addenda-7.txt");

        System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        System.out.println("<Nurikabe>");
        System.out.println("<Dimension x=\"" + gfr.getWidth() + "\" y=\"" + gfr.getHeight() + "\"/>");
        System.out.println("<Board>");

        System.out.println("<Streets>");

        for (int y = 0 ; y < gfr.getHeight() ; ++y)
        {
            for (int x = 0 ; x < gfr.getWidth() ; ++x)
            {
                if (gfr.getBlock("WALLS")[x][y].charAt(0) == '.') continue;
                System.out.println("<Street cs=\"(" + x + "," + y + ")\"/>");
            }
        }

        System.out.println("</Streets>");
        for (int y = 0 ; y < gfr.getHeight() ; ++y)
        {
            for (int x = 0; x < gfr.getWidth(); ++x)
            {
                char schr = gfr.getBlock("NUMBERS")[x][y].charAt(0);
                if (schr == '.') continue;
                String dim = "\"(" + x + "," + y + ")\"";

                System.out.println("<Building cs=" + dim + " number=\"" + schr + "\" loc=" + dim + "/>");
            }
        }

        System.out.println("<Buildings>");




        System.out.println("</Buildings>");




        System.out.println("</Board>");
        System.out.println("</Nurikabe>");

    }
}
