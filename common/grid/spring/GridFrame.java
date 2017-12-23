package grid.spring;

public class GridFrame extends SinglePanelFrame
{
	public GridFrame(String title, int width, int height, GridPanel.GridListener listener)
	{
		this(title,width,height,listener,null);
	}
	
	public GridFrame(String title,int width,int height,GridPanel.GridListener listener, GridPanel.EdgeListener edgeListener)
	{
		super(title,new GridPanelContainer(width,height,listener,edgeListener));
	}	
	
}

