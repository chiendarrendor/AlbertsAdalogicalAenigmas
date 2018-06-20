package grid.assistant;
import grid.spring.ClickableGridPanel;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

public class SolverAssistantPanel<T extends AssistantBoard<T> > extends JPanel {
    BoardQueue<T> queue;

    ClickableGridPanel cgp = null;
    JLabel status;

    public void update() {
        cgp.getParams().repaint();
    }

    public void status(String s) {
        status.setText(s);
    }


    public SolverAssistantPanel(int width, int height, SolverAssistantConfig<T> config) {
        queue = new BoardQueue<T>(config);

        setLayout(new BorderLayout());
        cgp = new ClickableGridPanel(width,height,
                config.getGridListener(queue.getHolder()),
                config.getEdgeListener(queue.getHolder()));
        add(cgp,BorderLayout.NORTH);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel,BoxLayout.Y_AXIS));

        status = new JLabel("Test");
        status.setFont(new Font("SansSerif",Font.BOLD,16));
        statusPanel.add(status);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        statusPanel.add(buttonPanel);
        add(statusPanel,BorderLayout.SOUTH);

        buttonPanel.add(new LambdaButton("Clear" , () -> { status(queue.clearCur()); update(); } ));
        buttonPanel.add(new LambdaButton("Backup",() -> { status(queue.popCur()); update(); } ));

        buttonPanel.add(new LambdaButton("Logic", () -> { status(queue.doLogic()); update(); } ));
        buttonPanel.add(new LambdaButton("Guess",()-> { status(queue.doGuess()); update(); } ));
        buttonPanel.add(new LambdaButton("Contradiction",() -> { status(queue.doContradiction()); update(); } ));
        buttonPanel.add(new LambdaButton("Save", ()-> status(queue.save()) ));

        if (config.getCellClicker() != null) {
            cgp.addCellClicker((x,y)-> {
                MovePair<T> movepair = config.getCellClicker().click(queue.getCurOrig(),queue.getCurCur(),x,y);
                if (movepair == null) return;
                queue.addCellMovePair(x,y,movepair);
                update();
            });
        }

        if (config.getEdgeClicker() != null) {
            cgp.addEdgeClicker((x,y,d)->{
                MovePair<T> movepair = config.getEdgeClicker().click(queue.getCurOrig(),queue.getCurCur(),x,y,d);
                if (movepair == null) return;
                queue.addEdgeMovePair(x,y,d,movepair);
                update();
            });
        }
    }
}
