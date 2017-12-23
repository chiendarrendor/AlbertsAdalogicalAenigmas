/**
 * Created by chien on 9/23/2017.
 */
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Ada03In
{


    public static void main(String[] args)
    {
        Document doc = null;

        try
        {
            File inputFile = new File("addenda-7-solved.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(inputFile);
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (SAXException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        Element dim = (Element)doc.getElementsByTagName("Dimension").item(0);
        System.out.println("X: " + dim.getAttribute("x") + " Y: " + dim.getAttribute("y"));
        int width = Integer.parseInt(dim.getAttribute("x"));
        int height = Integer.parseInt(dim.getAttribute("y"));
        char[][] solution = new char[width][height];

        ProcessCells(solution,'.',doc.getElementsByTagName("Street"));
        ProcessCells(solution,'b',doc.getElementsByTagName("Building"));



        for (int y = 0 ; y < height ; ++y)
        {
            for (int x = 0 ; x < width ; ++x)
            {
                System.out.print(solution[x][y]);
            }
            System.out.println("");
        }

    }

    private static void ProcessCells(char[][] solution, char type, NodeList nodes)
    {
        char itype = type;

        for (int i = 0 ; i < nodes.getLength() ; ++i)
        {
            Element el = (Element)nodes.item(i);
            String cs = el.getAttribute("cs");

            if (type == 'b')
            {
                itype = el.getAttribute("number").charAt(0);
            }




            Pattern p = Pattern.compile("\\(([0-9]+),([0-9]+)\\)");
            Matcher m = p.matcher(cs);

            while(m.find())
            {
                int x = Integer.parseInt(m.group(1));
                int y = Integer.parseInt(m.group(2));
                solution[x][y] = itype;
            }
        }
    }
}
