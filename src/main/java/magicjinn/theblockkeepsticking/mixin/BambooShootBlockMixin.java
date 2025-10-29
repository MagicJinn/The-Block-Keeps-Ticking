package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BambooShootBlock.class)
public class BambooShootBlockMixin implements TickingAccessor {

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world, 3);
        // Bamboo shoots are different from regular bamboo. Therefor, if we encounter one, we can
        // convert it to a bamboo block for free, then pass the new state on to the bamboo block
        // simulate method
        // (The conversion should be free, because the .growth method also adds a single bamboo to
        // the top of the shoot)
        if (randomTicks > 0) {
            world.setBlockState(pos, Blocks.BAMBOO.getDefaultState(), 3);
            BlockState newState = world.getBlockState(pos);
            // Pass the newly created block onto the next simulate method
            ((TickingAccessor) newState.getBlock()).Simulate(ticksToSimulate, world, newState,
                    pos);

            return true;
        }

        return false;
    }

}
