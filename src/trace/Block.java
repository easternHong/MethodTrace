package trace;

public interface Block {
    public String getName();

    public MethodData getMethodData();

    public long getStartTime();

    public long getEndTime();

    public double addWeight(int x, int y, double weight);

    public void clearWeight();

    public long getExclusiveCpuTime();

    public long getInclusiveCpuTime();

    public long getExclusiveRealTime();

    public long getInclusiveRealTime();

    public boolean isContextSwitch();

    public boolean isIgnoredBlock();

    public Block getParentBlock();
}