package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.KelpBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(KelpBlock.class)
public class KelpBlockMixin implements TickingBlockAccessor {
    @Shadow @Final private static double GROWTH_CHANCE;

    private static final Direction growthDirection = Direction.UP;


    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world,
                (float) (1.0 / GROWTH_CHANCE));

        if (randomTicks <= 0)
            return false;

        int age = state.get(AbstractPlantStemBlock.AGE);
        int maxAge = AbstractPlantStemBlock.MAX_AGE;

        int ageDiff = maxAge - age;

        boolean changed = false;

        for (int i = 1; i < ageDiff && i <= randomTicks && age < maxAge; i++) {
            BlockPos blockAbove = pos.offset(growthDirection, i);
            BlockState blockAboveState = world.getBlockState(blockAbove);
            if (blockAboveState.isOf(Blocks.WATER)) {
                age++;
                world.setBlockState(blockAbove, state.with(AbstractPlantStemBlock.AGE, age), 3);
                changed = true;
            } else
                break;
        }

        return changed;
    }
}
