package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingLeavesBlock extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingLeavesBlock();

    @Override
    public Class<LeavesBlock> getType() {
        return LeavesBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, Long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof LeavesBlock leaves) {
            return ((TickingBlockAccessor) leaves).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
