import java.awt.Point;

public class Region {
    public static class Template {
        int idx;
        char id;
        int ccount;
        Point[] deltas = new Point[3];

        public Template(int idx,char id,int ccount,int x0,int y0,int x1,int y1,int x2,int y2) {
            this.idx = idx;
            this.id = id;
            this.ccount = ccount;
            deltas[0] = new Point(x0,y0);
            deltas[1] = new Point(x1,y1);
            deltas[2] = new Point(x2,y2);
        }

        public boolean isCenter() { return deltas[1].x == 0 && deltas[1].y == 0; }
        public int getCenterCount() { return ccount; }


    }

    // this should have an array of the 6 triominoes, with a point at 0,0 for each of the three cells for each.
    static Template[] templates = new Template[] {
            // ─ │ ┌ └ ┐ ┘
            new Template(0,'─',2, 0,0, 1,0, 2,0),
            new Template(1,'─', 2,-1,0, 0,0, 1,0),
            new Template(2,'─', 2,-2,0, -1,0, 0,0),

            new Template(3,'│',0,0,0, 0,1,0,2),
            new Template(4,'│', 0,0,-1,0,0,0,1),
            new Template(5,'│', 0,0,-2,0,-1,0,0),

            new Template(6,'┌', 1,0,0,0,-1,1,-1),
            new Template(7,'┌', 1,0,1,0,0,1,0),
            new Template(8,'┌',1,-1,1,-1,0,0,0),

            new Template(9,'└', 1,0,0,0,1,1,1),
            new Template(10,'└',1,0,-1,0,0,1,0),
            new Template(11,'└',1,-1,-1,-1,0,0,0),

            new Template(12,'┐',1, 0,0,1,0,1,1),
            new Template(13,'┐', 1,-1,0,0,0,0,1),
            new Template(14,'┐', 1,-1,-1,0,-1,0,0),

            new Template(15,'┘', 1,0,0,1,0,1,-1),
            new Template(16,'┘', 1,-1,0,0,0,0,-1),
            new Template(17,'┘', 1,-1,1,0,1,0,0)
    };

    // given a template id, and an index into one of the three cells for that template id,
    // return the template id that has its zero at that index.
    static int templateAtIndex(int tempid,int cellindex) {
        return (tempid/3) * 3 + cellindex;
    }



    static Board makeTestBoard() {
        Board b = new Board("test.txt");

        b.placeTemplate(2,2,Region.templates[0]);
        b.placeTemplate(2,6,Region.templates[1]);
        b.placeTemplate(2,10,Region.templates[2]);

        b.placeTemplate(6,2,Region.templates[3]);
        b.placeTemplate(6,6,Region.templates[4]);
        b.placeTemplate(6,10,Region.templates[5]);

        b.placeTemplate(10,2,Region.templates[6]);
        b.placeTemplate(10,6,Region.templates[7]);
        b.placeTemplate(10,10,Region.templates[8]);

        b.placeTemplate(14,2,Region.templates[9]);
        b.placeTemplate(14,6,Region.templates[10]);
        b.placeTemplate(14,10,Region.templates[11]);

        b.placeTemplate(18,2,Region.templates[12]);
        b.placeTemplate(18,6,Region.templates[13]);
        b.placeTemplate(18,10,Region.templates[14]);

        b.placeTemplate(22,2,Region.templates[15]);
        b.placeTemplate(22,6,Region.templates[16]);
        b.placeTemplate(22,10,Region.templates[17]);



        return b;
    }


}
