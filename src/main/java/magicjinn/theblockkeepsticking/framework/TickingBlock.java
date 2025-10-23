package magicjinn.theblockkeepsticking.framework;

import magicjinn.theblockkeepsticking.util.TickingResult;

public abstract class TickingBlock {
    /**
     * Gets the type of block this TickingBlock handles. Add public static final [HANDLING_TYPE]
     * INSTANCE = new [HANDLING_TYPE](); to the subclass to create implement this properly.
     * 
     * @return
     */
    public abstract Class<?> getType();

    // Custom simulation logic
    public abstract void Simulate(net.minecraft.block.entity.BlockEntity blockInstance, Long ticksToSimulate);

    public static TickingResult CalculateOperations(long ticks, int operationTime) {
        return CalculateOperations(ticks, operationTime, 0);
    }

    public static TickingResult CalculateOperations(long ticks, int operationTime, int offset) {
        operationTime += offset;
        int cycles = (int) (ticks / operationTime);
        int remainder = (int) (ticks % operationTime);
        return new TickingResult(cycles, remainder);
    }
}
