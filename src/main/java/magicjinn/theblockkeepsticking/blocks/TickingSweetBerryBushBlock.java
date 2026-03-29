package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingSweetBerryBushBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingSweetBerryBushBlock();

    @Override
    public Class<SweetBerryBushBlock> getType() {
        return SweetBerryBushBlock.class;
    }

    @Override
    public String getName() {
        return "Sweet Berry Bushes";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof SweetBerryBushBlock sweetBerryBush) {
            return ((TickingAccessor) sweetBerryBush).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
