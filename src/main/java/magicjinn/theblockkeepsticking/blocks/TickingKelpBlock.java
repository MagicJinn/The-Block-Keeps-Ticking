package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.KelpBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingKelpBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingKelpBlock();

    @Override
    public Class<?> getType() {
        return KelpBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof KelpBlock kelp) {
            return ((TickingAccessor) kelp).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }

}
