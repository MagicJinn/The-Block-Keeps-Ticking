package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingBrewingStandBlockEntity extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingBrewingStandBlockEntity();

    @Override
    public Class<BrewingStandBlockEntity> getType() {
        return BrewingStandBlockEntity.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof BrewingStandBlockEntity brewingStand) {
            return ((TickingBlockAccessor) brewingStand).Simulate(ticksToSimulate, world, state,
                    pos);
        }
        return false;
    }
}
