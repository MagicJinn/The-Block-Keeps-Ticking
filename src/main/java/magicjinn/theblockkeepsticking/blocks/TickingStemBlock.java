package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.StemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingStemBlock extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingStemBlock();

    @Override
    public Class<StemBlock> getType() {
        return StemBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof StemBlock stem) {
            return ((TickingBlockAccessor) stem).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
