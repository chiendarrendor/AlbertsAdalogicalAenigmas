package grid.spring.image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

public class ArrowLine {
    // gotten from stackoverflow question 2027613
    public static void drawArrowLine(Graphics g, Point p1, Point p2, int awidth, int aheight) {
        AffineTransform tx = new AffineTransform();
        int halfw = awidth/2;
        int halfh = aheight/2;

        // experiment here:
        Polygon arrowhead = new Polygon();
        arrowhead.addPoint(0,halfh);
        arrowhead.addPoint(-halfw,-halfh);
        arrowhead.addPoint(halfw,-halfh);


        tx.setToIdentity();
        double angle = Math.atan2(p2.y-p1.y,p2.x-p1.x);
        tx.translate(p2.x,p2.y);
        tx.rotate((angle-Math.PI/2d));
        Graphics2D subg = (Graphics2D)g.create();
        subg.setTransform(tx);
        subg.fill(arrowhead);
        subg.dispose();
        g.drawLine(p1.x,p1.y,p2.x,p2.y);

    }
}
