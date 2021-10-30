package grid.spring;

public class GridPanelFactoryImpl implements GridPanelFactory {
    public GridPanel getGridPanel(int width, int height, GridPanel.GridListener listener,GridPanel.EdgeListener edgeListener) { return new GridPanel(width,height,listener,edgeListener); }
}
