package magicjinn.theblockkeepsticking.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingCalculator {
    public static int RandomTickAmount(Long ticksToSimulate, World world) {
        return RandomTickAmount(ticksToSimulate, world, 1);
    }

    public static int RandomTickAmount(Long ticksToSimulate, World world, float divideByAmount) {
        // Determine the amount of random ticks that would have occurred
        int randomTickSpeed =
                ((ServerWorld) world).getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        int randomTicks = (int) (ticksToSimulate / (16 * 16 * 16) * randomTickSpeed);
        return (int) (randomTicks / divideByAmount);
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
