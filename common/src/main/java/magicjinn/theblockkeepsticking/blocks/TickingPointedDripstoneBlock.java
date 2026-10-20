package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingPointedDripstoneBlock extends TickingObject {
    @Override
    public Class<PointedDripstoneBlock> getType() {
        return PointedDripstoneBlock.class;
    }

    @Override
    public String getName() {
        return "Pointed Dripstone";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof PointedDripstoneBlock pointedDripstone) {
            return ((TickingAccessor) pointedDripstone).Simulate(ticksToSimulate, level, state,
                    pos);
        }
        return false;
    }
}
