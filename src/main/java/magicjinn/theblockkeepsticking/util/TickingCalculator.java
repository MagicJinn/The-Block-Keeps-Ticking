package magicjinn.theblockkeepsticking.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingCalculator {
    public static int RandomTickAmount(long ticksToSimulate, World world) {
        return RandomTickAmount(ticksToSimulate, world, 1);
    }

    public static int RandomTickAmount(long ticksToSimulate, World world, float divideByAmount) {
        // Determine the amount of random ticks that would have occurred
        int randomTickSpeed =
                ((ServerWorld) world).getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        int randomTicks = (int) (ticksToSimulate * randomTickSpeed) / (16 * 16 * 16);
        return (int) (randomTicks / divideByAmount);
    }

    public static int CropGrowthAmount(long ticksToSimulate, Block block, World world,
            BlockState state, BlockPos pos) {
        return CropGrowthAmount(ticksToSimulate, block, world, state, pos, 25f);
    }

    public static int CropGrowthAmount(long ticksToSimulate, Block block, World world,
            BlockState state, BlockPos pos, float growthChance) {

        // Too dark to grow
        if (world.getBaseLightLevel(pos, 0) < 9)
            return 0;

        // Determine the amount of random ticks that would have occurred
        int randomTicks = RandomTickAmount(ticksToSimulate, world);

        // Available moisture at the crop position
        float availableMoisture = CropBlock.getAvailableMoisture(block, world, pos);

        // Simplified growth formula, fakes randomness
        float chancePerTick = 1f / ((25f / availableMoisture) + 1f);
        int growth = (int) (randomTicks * chancePerTick);

        return growth;
    }
}
