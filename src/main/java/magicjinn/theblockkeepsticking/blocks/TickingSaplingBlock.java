package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof SaplingBlock sapling) {
            return ((TickingAccessor) sapling).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
