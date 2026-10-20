package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BambooSaplingBlock.class)
public class BambooSaplingBlockMixin implements TickingAccessor {

    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level, 3);
        // Bamboo saplings are different from regular bamboo. Therefor, if we encounter
        // one, we can convert it to a bamboo block for free, then pass the new state on
        // to the bamboo block simulate method (The conversion should be free, because
        // the .growth method also adds a single bamboo to the top of the sapling)
        if (randomTicks > 0) {
            level.setBlock(pos, Blocks.BAMBOO.defaultBlockState(), 3);
            BlockState newState = level.getBlockState(pos);
            // Pass the newly created block onto the next simulate method
            ((TickingAccessor) newState.getBlock()).Simulate(ticksToSimulate, level, newState,
                    pos);

            return true;
        }

        return false;
    }

}
