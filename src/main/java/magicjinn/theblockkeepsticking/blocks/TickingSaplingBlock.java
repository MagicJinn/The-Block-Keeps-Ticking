package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingSaplingBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingSaplingBlock();

    @Override
    public Class<SaplingBlock> getType() {
        return SaplingBlock.class;
    }

    @Override
    public String getName() {
        return "Saplings";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof SaplingBlock sapling) {
            return ((TickingAccessor) sapling).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
