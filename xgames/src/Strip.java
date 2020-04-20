import com.sun.org.apache.regexp.internal.RE;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strip {
    List<Color> colorList = new ArrayList<>();
    List<Boolean> isWhiteList = new ArrayList<>();
    List<Character> characterList = new ArrayList<>();
    StringBuffer name = new StringBuffer();


    private static String mgroup(Matcher m,int pos) {
        if (m.group(pos) == null) return "";
        return m.group(pos);
    }

    // string format is (.\)?([A-Z0-9])([rb])?
    Pattern spattern = Pattern.compile("(\\.)?([A-Z0-9])?([rb])?");
    public void add(String s) {
        Matcher m = spattern.matcher(s);
        if (!m.matches()) throw new RuntimeException("Invalid pattern string: " + s);

        boolean isWhite =  !mgroup(m,1).equals(".");
        isWhiteList.add(isWhite);

        Color c = Color.BLACK;
        if (mgroup(m,3).equals("r")) c = Color.RED;
        if (mgroup(m,3).equals("b")) c = Color.BLUE;
        colorList.add(c);

        char ch = '.';
        if (mgroup(m,2).length() > 0) ch = m.group(2).charAt(0);
        characterList.add(ch);
        name.append(ch);
    }

    public String toString() { return name.toString(); }

}
