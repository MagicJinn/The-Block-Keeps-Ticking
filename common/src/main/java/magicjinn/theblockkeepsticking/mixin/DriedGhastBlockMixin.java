package magicjinn.theblockkeepsticking.mixin;

import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.DriedGhastBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DriedGhastBlock.class)
public class DriedGhastBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        // This sucks

        DriedGhastBlock driedGhast = (DriedGhastBlock) (Object) this;
        if (!(level instanceof ServerLevel serverLevel))
            return false;

        boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED);
        if (!waterlogged)
            return false;

        long remainingTicks = ticksToSimulate;

        // How many ticks happen between every random tick
        int randomTickRatio = TickingCalculator.RandomTickRatio(serverLevel);
        boolean changed = false;

        int hydration = state.getValue(BlockStateProperties.DRIED_GHAST_HYDRATION_LEVELS);
        while (hydration < DriedGhastBlock.MAX_HYDRATION_LEVEL) {
            long ticksNeeded = randomTickRatio + DriedGhastBlock.HYDRATION_TICK_DELAY;

            if (remainingTicks < ticksNeeded)
                break;

            remainingTicks -= ticksNeeded;
            hydration++;
            changed = true;
        }

        if (changed) {
            serverLevel.setBlock(pos, state.setValue(BlockStateProperties.DRIED_GHAST_HYDRATION_LEVELS, hydration), 2);
            serverLevel.scheduleTick(pos, driedGhast, (int) Math.max(1,
                    DriedGhastBlock.HYDRATION_TICK_DELAY - Math.max(0, remainingTicks)));
        }

        return changed;
    }
}
