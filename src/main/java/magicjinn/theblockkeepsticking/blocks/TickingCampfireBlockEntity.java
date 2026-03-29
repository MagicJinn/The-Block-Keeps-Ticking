package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingCampfireBlockEntity extends TickingObject {
    public static final TickingObject INSTANCE = new TickingCampfireBlockEntity();

    @Override
    public Class<CampfireBlockEntity> getType() {
        return CampfireBlockEntity.class;
    }

    @Override
    public String getName() {
        return "Campfires";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CampfireBlockEntity campfire) {
            return ((TickingAccessor) campfire).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
