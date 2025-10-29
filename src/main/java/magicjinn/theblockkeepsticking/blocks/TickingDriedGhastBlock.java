package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.block.BlockState;
import net.minecraft.block.DriedGhastBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingDriedGhastBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingDriedGhastBlock();

    @Override
    public Class<DriedGhastBlock> getType() {
        return DriedGhastBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof DriedGhastBlock block) {
            return ((TickingAccessor) block).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}


