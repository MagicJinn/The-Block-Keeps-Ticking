package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnifferEggBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(SnifferEggBlock.class)
public class SnifferEggBlockMixin implements TickingAccessor {

    @Shadow @Final private static int HATCHING_TIME;
    @Shadow @Final private static int BOOSTED_HATCHING_TIME;
    @Shadow @Final private static int MAX_RANDOM_CRACK_TIME_OFFSET;

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        SnifferEggBlock snifferEgg = (SnifferEggBlock) (Object) this;

        // if (!(world instanceof ServerWorld serverWorld))
        // return false;

        long ticksRemaining = ticksToSimulate;

        int hatchState = snifferEgg.getHatchStage(state);

        boolean changed = false;

        // Whether to boost the hatching time
        boolean shouldBoost = SnifferEggBlock.isAboveHatchBooster(world, pos);
        int hatchingTime = (shouldBoost ? BOOSTED_HATCHING_TIME : HATCHING_TIME) / 3
                + MAX_RANDOM_CRACK_TIME_OFFSET / 2;

        while (hatchState < SnifferEggBlock.FINAL_HATCH_STAGE) {
            long ticksNeeded = hatchingTime;
            if (ticksRemaining < ticksNeeded)
                break;

            ticksRemaining -= ticksNeeded;
            hatchState++;
            changed = true;
        }

        if (changed) {
            world.setBlockState(pos, (BlockState) state.with(SnifferEggBlock.HATCH, hatchState), 2);
            world.scheduleBlockTick(pos, snifferEgg,
                    Integer.max(1, (int) (hatchingTime - Math.max(0, ticksRemaining))));
        }

        return changed;
    }

}
