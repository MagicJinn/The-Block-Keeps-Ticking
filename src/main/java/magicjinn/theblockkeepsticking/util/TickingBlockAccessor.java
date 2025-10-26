package magicjinn.theblockkeepsticking.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface TickingBlockAccessor {
    boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos);
}
