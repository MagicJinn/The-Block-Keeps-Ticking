package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.KelpBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingKelpBlock extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingKelpBlock();

    @Override
    public Class<?> getType() {
        return KelpBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, Long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof KelpBlock kelp) {
            return ((TickingBlockAccessor) kelp).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }

}
