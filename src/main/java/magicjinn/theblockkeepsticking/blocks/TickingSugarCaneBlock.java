package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingSugarCaneBlock extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingSugarCaneBlock();

    @Override
    public Class<SugarCaneBlock> getType() {
        return SugarCaneBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof SugarCaneBlock sugarcane) {
            return ((TickingBlockAccessor) sugarcane).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
