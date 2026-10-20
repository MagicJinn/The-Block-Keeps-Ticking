package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin implements TickingAccessor {
    // randomTick is protected in LeavesBlock, so it can't be called directly from
    // this package. Shadow it to invoke the real (virtually dispatched) method.
    @Shadow
    protected abstract void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);

    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level);
        if (randomTicks <= 0)
            return false;

        // Leaves only decay (or do nothing) on a random tick, and once decayed the
        // block is gone, so a single tick reproduces vanilla regardless of how many
        // random ticks were missed.
        this.randomTick(state, (ServerLevel) level, pos, level.getRandom());

        // Report a change only if the leaf actually decayed away.
        if (pos != null)
            return level.getBlockState(pos).isAir();
        return false;
    }
}
