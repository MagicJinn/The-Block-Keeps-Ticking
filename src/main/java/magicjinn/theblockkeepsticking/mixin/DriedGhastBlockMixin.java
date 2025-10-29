package magicjinn.theblockkeepsticking.mixin;

import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.block.DriedGhastBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DriedGhastBlock.class)
public class DriedGhastBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        DriedGhastBlock driedGhast = (DriedGhastBlock) (Object) this;
        if (!(world instanceof ServerWorld serverWorld))
            return false;

        boolean waterlogged = state.get(Properties.WATERLOGGED);
        if (!waterlogged)
            return false;

        if (serverWorld.getBlockTickScheduler().isQueued(pos, driedGhast))
            return false;

        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, serverWorld);
        if (randomTicks == 0)
            return false;

        // How many ticks happen between every random tick
        int randomTickFraction = (int) (ticksToSimulate / randomTicks);
        long remainingTicks = ticksToSimulate;
        int hydration = state.get(Properties.HYDRATION);
        boolean changed = false;

        while (hydration < DriedGhastBlock.MAX_HYDRATION) {
            long ticksNeeded = randomTickFraction + DriedGhastBlock.HYDRATION_TICK_TIME;

            if (remainingTicks < ticksNeeded)
                break;

            remainingTicks -= ticksNeeded;
            hydration++;
            changed = true;
        }


        if (changed) {
            if (hydration >= DriedGhastBlock.MAX_HYDRATION) {
                serverWorld.setBlockState(pos, state.with(Properties.HYDRATION, hydration), 2);
                serverWorld.scheduleBlockTick(pos, driedGhast, (int) Math.max(1, remainingTicks));
            }
        }

        return changed;
    }
}
