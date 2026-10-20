package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(SnifferEggBlock.class)
public class SnifferEggBlockMixin implements TickingAccessor {

    @Shadow
    @Final
    private static final int REGULAR_HATCH_TIME_TICKS = 24000;

    @Shadow
    @Final
    private static final int BOOSTED_HATCH_TIME_TICKS = 12000;

    @Shadow
    @Final
    private static final int RANDOM_HATCH_OFFSET_TICKS = 300;

    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        SnifferEggBlock snifferEgg = (SnifferEggBlock) (Object) this;

        // if (!(world instanceof ServerLevel serverLevel))
        // return false;

        long ticksRemaining = ticksToSimulate;

        int hatchState = snifferEgg.getHatchLevel(state);

        boolean changed = false;

        // Whether to boost the hatching time
        boolean shouldBoost = SnifferEggBlock.hatchBoost(level, pos);
        int hatchingTime = (shouldBoost ? BOOSTED_HATCH_TIME_TICKS : REGULAR_HATCH_TIME_TICKS) / 3
                + RANDOM_HATCH_OFFSET_TICKS / 2;

        while (hatchState < SnifferEggBlock.MAX_HATCH_LEVEL) {
            long ticksNeeded = hatchingTime;
            if (ticksRemaining < ticksNeeded)
                break;

            ticksRemaining -= ticksNeeded;
            hatchState++;
            changed = true;
        }

        if (changed) {
            level.setBlock(pos, (BlockState) state.setValue(SnifferEggBlock.HATCH, hatchState), 2);
            level.scheduleTick(pos, snifferEgg,
                    Integer.max(1, (int) (hatchingTime - Math.max(0, ticksRemaining))));
        }

        return changed;
    }

}
