package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingBambooBlock extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingBambooBlock();

    @Override
    public Class<BambooBlock> getType() {
        return BambooBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof BambooBlock bamboo) {
            return ((TickingBlockAccessor) bamboo).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
