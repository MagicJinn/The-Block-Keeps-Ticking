package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(BambooStalkBlock.class)
public class BambooStalkBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        BambooStalkBlock stalkBlock = (BambooStalkBlock) (Object) this;

        int stage = state.getValue(BambooStalkBlock.STAGE);
        int bambooMaxHeight = BambooStalkBlock.MAX_HEIGHT;
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level, 3);

        if (randomTicks <= 0)
            return false;

        int bambooBelowAmount = stalkBlock.getHeightBelowUpToMax(level, pos);
        boolean changed = false;
        for (int i = 1; bambooBelowAmount < bambooMaxHeight && stage < 1 && i <= randomTicks; i++) {
            BlockPos blockInQuestion = pos.above(i - 1); // current top bamboo block
            BlockState currentBambooState = level.getBlockState(blockInQuestion);

            // Update stage from the actual bamboo block
            stage = currentBambooState.getValue(BambooStalkBlock.STAGE);
            if (stage >= 1)
                break; // Stop if mature

            BlockPos blockAbove = pos.above(i); // block above current bamboo
            BlockState stateAbove = level.getBlockState(blockAbove);
            boolean blockAboveIsLit = level.getRawBrightness(blockAbove, 0) >= 9;

            if (stateAbove.isAir() && blockAboveIsLit) {
                // Pass the current bamboo block's state, not the air above
                stalkBlock.growBamboo(currentBambooState, level, blockInQuestion, level.getRandom(),
                        bambooBelowAmount + i);
                bambooBelowAmount++;
                changed = true;
            } else {
                break;
            }
        }

        return changed;
    }

}
