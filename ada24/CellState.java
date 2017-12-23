enum CellState 
{ 
	UNKNOWN(false,false), 
	EMPTY(false,true), 
	SLASH(true,true), 
	BACKSLASH(true,true), 
	GENERICMIRROR(true,false);
	
	private final boolean isMirror;
	private final boolean noChoice;
	CellState(boolean isMirror,boolean noChoice) { this.isMirror = isMirror; this.noChoice = noChoice; }
	public boolean isMirror() { return isMirror; }
	public boolean noChoice() { return noChoice; }
}