package magicjinn.theblockkeepsticking.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
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

    public static class TickingResult {
        public TickingResult(int cycles, int remainder) {
            this.cycles = cycles;
            this.remainder = remainder;
        }

        public int cycles;
        public int remainder;
    }

    public static int RandomTickAmount(Long ticksToSimulate, World world) {
        // Determine the amount of random ticks that would have occurred
        int randomTickSpeed = ((ServerWorld) world).getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        int randomTicks = (int) (ticksToSimulate / (16 * 16 * 16) * randomTickSpeed);
        return randomTicks;
    }

    public static int CropGrowthAmount(Long ticksToSimulate, Block block, World world,
            BlockState state, BlockPos pos) {
        return CropGrowthAmount(ticksToSimulate, block, world, state, pos, 25f);
    }

    // Base method to be used by both crops and stems
    public static int CropGrowthAmount(Long ticksToSimulate, Block block, World world,
            BlockState state, BlockPos pos, float growthChance) {
        // Too dark to grow
        if (world.getBaseLightLevel(pos, 0) < 9)
            return 0;

        // Determine the amount of random ticks that would have occurred
        int randomTicks = RandomTickAmount(ticksToSimulate, world);

        // Simplified growth formula, fakes randomness
        float availableMoisture = CropBlock.getAvailableMoisture(block, world, pos);
        int growth = (int) Math.floor(growthChance / availableMoisture + 1) * randomTicks;
        return growth;
    }
}
