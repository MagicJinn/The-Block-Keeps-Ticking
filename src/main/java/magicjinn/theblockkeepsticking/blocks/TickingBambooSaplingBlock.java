package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingBambooSaplingBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingBambooSaplingBlock();

    @Override
    public Class<BambooSaplingBlock> getType() {
        return BambooSaplingBlock.class;
    }

    @Override
    public String getName() {
        return "Bamboo";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof BambooSaplingBlock saplingBlock) {
            return ((TickingAccessor) saplingBlock).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
