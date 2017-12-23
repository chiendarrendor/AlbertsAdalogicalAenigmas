import grid.file.GridFileReader;
import grid.letter.LetterRotate;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chien on 9/23/2017.
 */
public class Ada03Solution
{
    public static Set<Integer> primes = new HashSet<>();
    static {
        primes.add(2);
        primes.add(3);
        primes.add(5);
        primes.add(7);
        primes.add(11);
        primes.add(13);
        primes.add(17);
        primes.add(19);
        primes.add(23);
        primes.add(29);
    }

    public static boolean isPrime(int p) { return primes.contains(p); }



    public static void main(String[] args)
    {
        GridFileReader gfr = new GridFileReader("addenda-7.txt");

        for (int y = 0 ; y < gfr.getHeight() ; ++y)
        {
            int wcount = 0;
            for (int x = 0 ; x < gfr.getWidth() ; ++x)
            {
                if (gfr.getBlock("SOLUTION")[x][y].charAt(0) != '.') ++wcount;
            }
            if (!isPrime(wcount)) continue;

            for (int x = 0 ; x < gfr.getWidth() ; ++x)
            {
                if (gfr.getBlock("SOLUTION")[x][y].charAt(0) == '.') continue;
                int blocksize = Integer.parseInt(gfr.getBlock("SOLUTION")[x][y]);
                if (gfr.getBlock("LETTERS")[x][y].charAt(0) == '.') continue;
                char ch = gfr.getBlock("LETTERS")[x][y].charAt(0);
                System.out.print(LetterRotate.Rotate(ch,blocksize));
            }

        }




    }

}
