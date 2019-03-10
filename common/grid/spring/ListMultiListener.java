package grid.spring;

import java.util.List;

public abstract class ListMultiListener<T> implements GridPanel.MultiGridListener {
    private List<T> thingar;
    int curidx = 0;

    public ListMultiListener(List<T> things) { thingar = things; }
    protected T b() { return thingar.get(curidx); }

    @Override public boolean hasNext() { return curidx < thingar.size() - 1; }
    @Override public boolean hasPrev() { return curidx > 0; }
    @Override public void moveToNext() { ++curidx; }
    @Override public void moveToPrev() { --curidx; }
}
