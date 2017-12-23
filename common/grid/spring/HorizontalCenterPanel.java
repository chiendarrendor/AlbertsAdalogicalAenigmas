package grid.spring; /**
 * Created by chien on 3/1/2017.
 */
import javax.swing.*;
import java.awt.*;

public class HorizontalCenterPanel extends Box
{
    private static int SEPARATION = 5;
    public HorizontalCenterPanel(Component... comps)
    {
        super(BoxLayout.X_AXIS);
        add(Box.createHorizontalGlue());

        for (int i = 0 ; i < comps.length ; ++ i)
        {
            if (i > 0) add(Box.createRigidArea(new Dimension(SEPARATION,0)));
            add(comps[i]);
        }

        add(Box.createHorizontalGlue());
    }
}
