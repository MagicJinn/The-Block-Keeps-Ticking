package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingBambooShootBlock extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingBambooShootBlock();

    @Override
    public Class<?> getType() {
        return BambooShootBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof BambooShootBlock shootBlock) {
            return ((TickingBlockAccessor) shootBlock).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }

}
