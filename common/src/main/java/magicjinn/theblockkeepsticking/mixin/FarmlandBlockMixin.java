package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        long randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level);
        if (randomTicks <= 0)
            return false;

        // We could just for(int i = 0; i < Math.max(8, randomTicks); i++),
        // but that's no fun.

        FarmlandBlock farmland = (FarmlandBlock) (Object) this;
        int moisture = state.getValue(FarmlandBlock.MOISTURE);

        // Exact copy from FarmlandBlock.isNearWater, plus rain (vanilla wet condition)
        boolean hydrated = level.findBlocksIn(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))
                .filterState(foundState -> foundState.getFluidState().is(FluidTags.WATER)).anyMatched()
                || level.isRainingAt(pos.above());

        if (hydrated) {
            if (moisture < FarmlandBlock.MAX_MOISTURE) {
                level.setBlock(pos, state.setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE), 2);
                return true;
            }
            return false;
        }

        // Exact copy from FarmlandBlock.shouldMaintainFarmland
        boolean hasCrop = level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND);

        if (moisture > 0) {
            // Leftover ticks after drying to 0 can convert to base block
            if (randomTicks > moisture && !hasCrop) {
                farmland.turnToBaseBlock(null, state, level, pos);
            } else {
                level.setBlock(pos,
                        state.setValue(FarmlandBlock.MOISTURE, Math.max(0, moisture - (int) randomTicks)), 2);
            }
            return true;
        }

        if (!hasCrop) {
            farmland.turnToBaseBlock(null, state, level, pos);
            return true;
        }

        return false;
    }
}
