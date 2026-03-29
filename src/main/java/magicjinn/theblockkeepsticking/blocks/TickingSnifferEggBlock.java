package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof SnifferEggBlock snifferEgg) {
            return ((TickingAccessor) snifferEgg).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
