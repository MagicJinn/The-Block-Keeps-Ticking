package magicjinn.theblockkeepsticking.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.CropBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class TickingCalculator {
    public static int MoveTowardsZero(int input, long ticks) {
        int sign = Integer.signum(input);
        return sign * Integer.max(0, (int) (Math.abs(input) - ticks));
    }

    public static int RandomTickAmount(long ticksToSimulate, Level level) {
        return RandomTickAmount(ticksToSimulate, level, 1);
    }

    public static int RandomTickAmount(long ticksToSimulate, Level level, float divideByAmount) {
        // Determine the amount of random ticks that would have occurred
        int randomTickRatio = RandomTickRatio(level);
        if (randomTickRatio <= 0 || divideByAmount <= 0)
            return 0;
        int randomTicks = (int) (ticksToSimulate / randomTickRatio);
        return (int) (randomTicks / divideByAmount);
    }

    /**
     * Expected random-tick count as a double, then one Bernoulli trial on the fractional part so
     * short skips (where the integer estimate is 0) still sometimes produce attempts.
     */
    public static int RandomTickAmountStochastic(long ticksToSimulate, Level level,
            float divideByAmount, RandomSource random) {
        int randomTickRatio = RandomTickRatio(level);
        if (randomTickRatio <= 0 || divideByAmount <= 0)
            return 0;
        double expected =
                (double) ticksToSimulate / (double) randomTickRatio / (double) divideByAmount;
        int base = (int) Math.floor(expected);
        double frac = expected - base;
        if (frac > 0.0 && random.nextDouble() < frac) {
            return base + 1;
        }
        return base;
    }

    public static int RandomTickRatio(Level level) {
        int randomTickSpeed =
                ((ServerLevel) level).getGameRules().get(GameRules.RANDOM_TICK_SPEED);

        if (randomTickSpeed <= 0) // Because its possible
            return Integer.MAX_VALUE;

        float fraction = (float) randomTickSpeed / (float) (16 * 16 * 16);
        if (fraction <= 0)
            return Integer.MAX_VALUE;

        int ratio = (int) (1.0 / fraction);
        return ratio;
    }

    public static int CropGrowthAmount(long ticksToSimulate, Block block, Level level,
            BlockState state, BlockPos pos) {
        return CropGrowthAmount(ticksToSimulate, block, level, state, pos, 25f);
    }

    public static int CropGrowthAmount(long ticksToSimulate, Block block, Level level,
            BlockState state, BlockPos pos, float growthChance) {

        // Too dark to grow
        if (level.getRawBrightness(pos, 0) < 9)
            return 0;

        // Determine the amount of random ticks that would have occurred
        int randomTicks = RandomTickAmount(ticksToSimulate, level);

        // Available moisture at the crop position
        float growthSpeed = CropBlock.getGrowthSpeed(block, level, pos);

        // Simplified growth formula, fakes randomness
        float chancePerTick = 1f / ((25f / growthSpeed) + 1f);
        int growth = (int) (randomTicks * chancePerTick);

        return growth;
    }
}
