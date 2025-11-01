package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnifferEggBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingSnifferEggBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingSnifferEggBlock();

    @Override
    public Class<SnifferEggBlock> getType() {
        return SnifferEggBlock.class;
    }

    @Override
    public String getName() {
        return "Sniffer Eggs";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof SnifferEggBlock snifferEgg) {
            return ((TickingAccessor) snifferEgg).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
