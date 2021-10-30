package grid.spring;

public class GridFrame extends SinglePanelFrame
{
	public GridFrame(String title, int width, int height, GridPanel.GridListener listener) {
		super(title,new GridPanelContainer(width,height,listener) );
	}
	
	public GridFrame(String title,int width,int height,GridPanel.GridListener listener, GridPanel.EdgeListener edgeListener) {
		super(title,new GridPanelContainer(width,height,listener,edgeListener));
	}

	public GridFrame(String title, int width, int height, GridPanel.GridListener listener,GridPanelFactory gpf) {
		super(title,new GridPanelContainer(gpf,width,height,listener) );
	}

	public GridFrame (String title,int width,int height,GridPanel.GridListener listener, GridPanel.EdgeListener edgeListener, GridPanelFactory gpf) {
		super(title,new GridPanelContainer(gpf,width,height,listener,edgeListener));
	}


	
}

