package grid.spring;

public interface GridPanelFactory {
    public GridPanel getGridPanel(int width, int height, GridPanel.GridListener listener,GridPanel.EdgeListener edgeListener);
}
