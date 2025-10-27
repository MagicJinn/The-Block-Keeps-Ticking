package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingCactusBlock extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingCactusBlock();

    @Override
    public Class<CactusBlock> getType() {
        return CactusBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CactusBlock cactus) {
            return ((TickingBlockAccessor) cactus).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
