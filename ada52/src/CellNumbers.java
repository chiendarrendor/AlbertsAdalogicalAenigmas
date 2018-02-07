public class CellNumbers
{
    boolean[] hasNumber;
    int min = 1;
    int max;

    public CellNumbers(int max)
    {
        this.max = max;
        hasNumber = new boolean[max+1];
        for (int i = min ; i <= max ; ++i) { hasNumber[i] = true; }
    }

    public CellNumbers(CellNumbers right)
    {
        this.max = right.max;
        hasNumber = new boolean[max+1];
        for (int i = min ; i <= max ; ++i) { hasNumber[i] = right.hasNumber[i]; }
    }

    private int onCount()
    {
        int total = 0;
        for (int i = min ; i <= max ; ++i) if (hasNumber[i]) ++total;
        return total;
    }

    public boolean isValid() { return onCount() > 0; }
    public boolean isDone() { return onCount() == 1; }
    public int doneNumber() { for (int i = 1 ; i <= max ; ++i) if (isOn(i)) return i; throw new RuntimeException("calling doneNumber on non-done cell!"); }
    public boolean isOn(int number) { return hasNumber[number]; }
    public void removeNumber(int number) { hasNumber[number] = false; }
    public void setNumber(int number) { for (int i = 1 ; i <= max ; ++i) if (i != number) removeNumber(i); }
    public int getMin() { return min; }
    public int getMax() { return max; }


    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        boolean empty = true;
        for (int i = min ; i <= max ; ++i) { if (hasNumber[i]) { empty = false; sb.append(i); } }
        if (empty) return "---";
        return sb.toString();
    }

    public int maxVal()
    {
        for (int i = max ; i >= 1 ; --i) if (isOn(i)) return i;
        throw new RuntimeException("maxVal called on invalid");
    }

    public int minVal()
    {
        for (int i = 1 ; i <= max ; ++i) if (isOn(i)) return i;
        throw new RuntimeException("minVal called on invalid");
    }
}
