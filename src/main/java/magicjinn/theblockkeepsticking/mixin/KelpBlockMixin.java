package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

@Mixin(KelpBlock.class)
public class KelpBlockMixin implements TickingAccessor {
    @Shadow
    @Final
    private static double GROW_PER_TICK_PROBABILITY;

    private static final Direction growthDirection = Direction.UP;


    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level,
                (float) (1.0 / GROW_PER_TICK_PROBABILITY));

        if (randomTicks <= 0)
            return false;

        int age = state.getValue(GrowingPlantHeadBlock.AGE);
        int maxAge = GrowingPlantHeadBlock.MAX_AGE;

        int ageDiff = maxAge - age;

        boolean changed = false;

        for (int i = 1; i < ageDiff && i <= randomTicks && age < maxAge; i++) {
            BlockPos blockAbove = pos.relative(growthDirection, i);
            BlockState blockAboveState = level.getBlockState(blockAbove);
            if (blockAboveState.is(Blocks.WATER)) {
                age++;
                level.setBlock(blockAbove, state.setValue(GrowingPlantHeadBlock.AGE, age), 3);
                changed = true;
            } else
                break;
        }

        return changed;
    }
}
