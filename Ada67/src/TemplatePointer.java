import java.awt.Point;

public class TemplatePointer {
    private Point basePoint;
    private int regionid;
    private Region.Template template;
    public TemplatePointer(int rid, Region.Template temp,Point basePoint) {
        regionid = rid;
        template = temp;
        this.basePoint = basePoint;
    }

    public int getRegionId() { return regionid; }
    public Region.Template getTemplate() { return template; }
    public Point getBasePoint() { return basePoint; }
    public Point getIndexPoint(int i) { return new Point(basePoint.x + template.deltas[i].x,basePoint.y + template.deltas[i].y); }

    public boolean equals(Object right) {
        TemplatePointer otp = (TemplatePointer)right;
        if (otp == null) return false;

        for (int i = 0 ; i < 3; ++i ) {
            if (getIndexPoint(i).equals(otp.getIndexPoint(i))) return false;
        }
        return true;
    }

}
