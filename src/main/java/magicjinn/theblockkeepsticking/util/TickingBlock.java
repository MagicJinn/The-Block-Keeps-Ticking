package magicjinn.theblockkeepsticking.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TickingBlock {
    /**
     * Gets the type of block this TickingBlock handles. Add public static final [HANDLING_TYPE]
     * INSTANCE = new [HANDLING_TYPE](); to the subclass to create implement this properly.
     * 
     * @return
     */
    public abstract Class<?> getType();

    // Custom simulation logic
    public abstract boolean Simulate(Object blockInstance, Long ticksToSimulate, World world,
            BlockState state, BlockPos pos);

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
