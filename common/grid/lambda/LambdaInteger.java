package grid.lambda;

public class LambdaInteger {
    private int i;

    public LambdaInteger(int i) { this.i = i; }
    public LambdaInteger() { this.i = 0; }
    public void inc() { ++i; }
    public void dec() { --i; }
    public void set(int i) { this.i = i; }
    public int get() { return i; }
}
