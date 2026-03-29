package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingBambooStalkBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingBambooStalkBlock();

    @Override
    public Class<BambooStalkBlock> getType() {
        return BambooStalkBlock.class;
    }

    @Override
    public String getName() {
        return "Bamboo";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof BambooStalkBlock stalkBlock) {
            return ((TickingAccessor) stalkBlock).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }

}
