package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingLeavesBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingLeavesBlock();

    @Override
    public Class<LeavesBlock> getType() {
        return LeavesBlock.class;
    }

    @Override
    public String getName() {
        return "Leaves";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof LeavesBlock leaves) {
            return ((TickingAccessor) leaves).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
