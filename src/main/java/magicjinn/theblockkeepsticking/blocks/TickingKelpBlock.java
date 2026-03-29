package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingKelpBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingKelpBlock();

    @Override
    public Class<?> getType() {
        return KelpBlock.class;
    }

    @Override
    public String getName() {
        return "Kelp";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof KelpBlock kelp) {
            return ((TickingAccessor) kelp).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }

}
