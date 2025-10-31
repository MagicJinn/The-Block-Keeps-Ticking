package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BambooBlock.class)
public class BambooBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        BambooBlock bambooBlock = (BambooBlock) (Object) this;

        int stage = state.get(BambooBlock.STAGE);
        int bambooMaxHeight = BambooBlock.field_31000;
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world, 3);

        if (randomTicks <= 0)
            return false;

        int bambooBelowAmount = bambooBlock.countBambooBelow(world, pos);
        boolean changed = false;
        for (int i = 1; bambooBelowAmount < bambooMaxHeight && stage < 1 && i <= randomTicks; i++) {
            BlockPos blockInQuestion = pos.up(i - 1); // current top bamboo block
            BlockState currentBambooState = world.getBlockState(blockInQuestion);

            // Update stage from the actual bamboo block
            stage = currentBambooState.get(BambooBlock.STAGE);
            if (stage >= 1)
                break; // Stop if mature

            BlockPos blockAbove = pos.up(i); // block above current bamboo
            BlockState stateAbove = world.getBlockState(blockAbove);
            boolean blockAboveIsLit = world.getBaseLightLevel(blockAbove, 0) >= 9;

            if (stateAbove.isAir() && blockAboveIsLit) {
                // Pass the current bamboo block's state, not the air above
                bambooBlock.updateLeaves(currentBambooState, world, blockInQuestion, world.random,
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
