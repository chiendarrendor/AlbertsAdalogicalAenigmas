package grid.assistant;

import grid.spring.SinglePanelFrame;

public class SolverAssistantFrame<T extends AssistantBoard<T> > extends SinglePanelFrame {

    public SolverAssistantFrame(String title,int width,int height,SolverAssistantConfig<T> config) {
        super(title,new SolverAssistantPanel<T>(width,height,config));

    }


}
