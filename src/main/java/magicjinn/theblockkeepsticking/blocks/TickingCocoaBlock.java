package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingCocoaBlock extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingCocoaBlock();

    @Override
    public Class<?> getType() {
        return CocoaBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CocoaBlock cocoaBlock) {
            return ((TickingBlockAccessor) cocoaBlock).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }

}
