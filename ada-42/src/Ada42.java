// for next time:
// 1) file -> New Module From Exisiting Sources, use the 'common' directory
// 2) Project Structure -> Add Dependency to project on module 'common'

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Ada42
{
    private static class DistrictInfo
    {
        char districtId;
        Vector<Point> spaces = new Vector<Point>();
        int minx;
        int maxx;
        int miny;
        int maxy;
        int numNums = 0;

        int getXExtent() { return maxx - minx + 1; }
        int getYExtent() { return maxy - miny + 1; }

        public DistrictInfo(char districtId, int x,int y,boolean hasNum)
        {
            this.districtId = districtId;
            minx = x;
            maxx = x;
            miny = y;
            maxy = y;
            spaces.add(new Point(x,y));
            if (hasNum) ++numNums;
        }

        public void AddCell(int x,int y,boolean hasNum)
        {
            if (x < minx) minx = x;
            if (x > maxx) maxx = x;
            if (y < miny) miny = y;
            if (y > maxy) maxy = y;
            spaces.add(new Point(x,y));
            if (hasNum) ++numNums;
        }
    }

    private static class BoardState
    {
        Map<Character,DistrictInfo> districts = new HashMap<>();
        GridFileReader gfr;

        public BoardState(String filename)
        {
            gfr = new GridFileReader(filename);
            for (int x = 0 ; x < gfr.getWidth() ; ++x)
            {
                for (int y = 0 ; y < gfr.getHeight() ; ++y)
                {
                    if (!HasDistrict(x,y)) continue;
                    char district = GetDistrict(x,y);
                    if (districts.containsKey(district))
                    {
                        districts.get(district).AddCell(x,y,HasNumber(x,y));
                    }
                    else
                    {
                        DistrictInfo di = new DistrictInfo(district,x,y,HasNumber(x,y));
                        districts.put(district,di);
                    }
                }
            }
        }

        public char GetLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
        public boolean HasLetter(int x,int y) { return Character.isAlphabetic(GetLetter(x,y)); }
        public char GetNumber(int x,int y) { return gfr.getBlock("NUMBERS")[x][y].charAt(0); }
        public boolean HasNumber(int x,int y) { return Character.isDigit(GetNumber(x,y)); }
        public char GetDistrict(int x,int y) { return gfr.getBlock("AREAS")[x][y].charAt(0);}
        public boolean HasDistrict(int x,int y) { return Character.isAlphabetic(GetDistrict(x,y)); }

        public int getWidth() { return gfr.getWidth(); }
        public int getHeight() { return gfr.getHeight(); }

    }




    public static class MyListener implements GridPanel.GridListener
    {
        private BoardState bs;
        public MyListener(BoardState bs) { this.bs = bs;}

        public int getNumXCells() { return bs.getWidth(); }
        public int getNumYCells() { return bs.getHeight(); }
        public boolean drawGridNumbers() { return true; }
        public boolean drawGridLines() { return true; }
        public boolean drawBoundary() { return true; }

        public boolean drawCellContents(int cx,int cy, BufferedImage bi)
        {
            Graphics2D g = (Graphics2D) bi.getGraphics();

            if (bs.HasNumber(cx,cy)) GridPanel.DrawStringInCell(bi,Color.black,"" + bs.GetNumber(cx,cy));
            if (bs.HasLetter(cx,cy))
            {
                GridPanel.DrawStringUpperLeftCell(bi, Color.black, ""+bs.GetLetter(cx,cy));

                char district = bs.GetDistrict(cx,cy);

                DistrictInfo di = bs.districts.get(district);
                boolean isBase = (di.getXExtent() == 2 && di.getYExtent() == 3) ||
                        (di.getXExtent() == 3 && di.getYExtent() == 2);

                GridPanel.DrawStringInCell(bi,
                        isBase ? Color.red : Color.blue,
                        "" + LetterRotate.Rotate(bs.GetLetter(cx, cy), di.numNums));

            }

            return true;
        }

    }

    public static class MyEdgeListener implements GridPanel.EdgeListener
    {
        BoardState bs;
        public MyEdgeListener(BoardState bs) { this.bs = bs; }

        public EdgeDescriptor onBoundary() { return new GridPanel.EdgeListener.EdgeDescriptor(Color.black,5);}

        public EdgeDescriptor toEast(int x,int y)
        {
            return new EdgeDescriptor(Color.black, bs.GetDistrict(x,y) == bs.GetDistrict(x+1,y) ? 1 : 5);
        }

        public EdgeDescriptor toSouth(int x,int y)
        {
            return new EdgeDescriptor(Color.black, bs.GetDistrict(x,y) == bs.GetDistrict(x,y+1) ? 1 : 5);
        }
    }



    public static void main(String[] args)
    {
        BoardState bs = new BoardState("ada42.txt");

        for (char district : bs.districts.keySet())
        {
            DistrictInfo di = bs.districts.get(district);
            System.out.println("ID: " + district + " num count: " + di.numNums + " x extent: " + di.getXExtent() + " y extent: " + di.getYExtent());
            for (Point p : di.spaces)
            {
                System.out.print("\t" + p.toString());
            }
            System.out.println("");
        }




        for (int y = 0 ; y < bs.getHeight() ; ++y)
        {
            for (int x = 0 ; x < bs.getWidth() ; ++x)
            {
                if (!bs.HasLetter(x,y)) continue;
                if (!bs.HasDistrict(x,y)) continue;
                char district = bs.GetDistrict(x,y);

                DistrictInfo di = bs.districts.get(district);

                if ((di.getXExtent() == 2 && di.getYExtent() == 3) ||
                        (di.getXExtent() == 3 && di.getYExtent() == 2))
                {
                    System.out.print(LetterRotate.Rotate(bs.GetLetter(x,y),di.numNums));
                }
            }
        }
        System.out.println("");

        for (int y = 0 ; y < bs.getHeight() ; ++y)
        {
            for (int x = 0 ; x < bs.getWidth() ; ++x)
            {
                if (!bs.HasLetter(x,y)) continue;
                if (!bs.HasDistrict(x,y)) continue;
                char district = bs.GetDistrict(x,y);

                DistrictInfo di = bs.districts.get(district);

                if (!((di.getXExtent() == 2 && di.getYExtent() == 3) ||
                        (di.getXExtent() == 3 && di.getYExtent() == 2)))
                {
                    System.out.print(LetterRotate.Rotate(bs.GetLetter(x,y),di.numNums));
                }
            }
        }
        System.out.println("");

        Map<Character,StringBuffer> strings = new HashMap<>();

        for (int x = 0 ; x < bs.getWidth() ; ++x)
        {
            for (int y = 0 ; y < bs.getHeight() ; ++y)
            {
                if (!bs.HasDistrict(x,y)) continue;
                char distid = bs.GetDistrict(x,y);
                char pentype = bs.gfr.getVar(""+distid).charAt(0);
                if (!strings.containsKey(pentype))
                {
                    strings.put(pentype,new StringBuffer());
                }
            }
        }

        for (int y = 0 ; y < bs.getHeight() ; ++y)
        {
            for (int x = 0 ; x < bs.getWidth() ; ++x)
            {
                if (!bs.HasDistrict(x,y)) continue;
                if (!bs.HasLetter(x,y)) continue;

                char district = bs.GetDistrict(x,y);
                char pentype = bs.gfr.getVar("" + district).charAt(0);
                int numcount = bs.districts.get(district).numNums;

                for(Map.Entry<Character,StringBuffer> ent: strings.entrySet())
                {
                    ent.getValue().append(ent.getKey().charValue() == pentype ?
                            LetterRotate.Rotate(bs.GetLetter(x,y),numcount) : ' ');
                }
            }
        }

        for (Map.Entry<Character,StringBuffer> ent: strings.entrySet())
        {
            System.out.println(ent.getKey() + ": " + ent.getValue());
        }






        GridFrame gridFrame = new GridFrame("Adalogical Aenigma #42", 1300, 768,
                new MyListener(bs),new MyEdgeListener(bs));
    }
}
