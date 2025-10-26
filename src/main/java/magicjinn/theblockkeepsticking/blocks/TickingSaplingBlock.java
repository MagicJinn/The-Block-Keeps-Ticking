package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingSaplingBlock extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingSaplingBlock();

    @Override
    public Class<SaplingBlock> getType() {
        return SaplingBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, Long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof SaplingBlock sapling) {
            return ((TickingBlockAccessor) sapling).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
