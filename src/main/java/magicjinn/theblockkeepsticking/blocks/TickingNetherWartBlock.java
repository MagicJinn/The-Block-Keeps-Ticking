package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingNetherWartBlock extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingNetherWartBlock();

    @Override
    public Class<NetherWartBlock> getType() {
        return NetherWartBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, Long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof NetherWartBlock netherWart) {
            return ((TickingBlockAccessor) netherWart).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
