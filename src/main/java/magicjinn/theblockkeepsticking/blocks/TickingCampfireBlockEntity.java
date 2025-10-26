package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingCampfireBlockEntity extends TickingBlock {
    public static final TickingBlock INSTANCE = new TickingCampfireBlockEntity();

    @Override
    public Class<CampfireBlockEntity> getType() {
        return CampfireBlockEntity.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, Long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CampfireBlockEntity campfire) {
            return ((TickingBlockAccessor) campfire).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
