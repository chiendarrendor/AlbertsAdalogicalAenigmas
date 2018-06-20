package grid.assistant;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LambdaButton extends JButton{
    private class MyActionListener implements ActionListener {
        private Runnable r;
        public MyActionListener(Runnable r) { this.r = r; }
        public void actionPerformed(ActionEvent e) { r.run(); }
    }
    public LambdaButton(String title,Runnable action) { super(title); addActionListener(new MyActionListener(action));}
}
