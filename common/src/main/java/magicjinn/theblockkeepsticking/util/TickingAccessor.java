package magicjinn.theblockkeepsticking.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface TickingAccessor {
    boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos);
}
