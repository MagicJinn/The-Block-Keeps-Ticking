package magicjinn.theblockkeepsticking.entities;

import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingAgeableMob extends TickingObject {
    public static final TickingObject INSTANCE = new TickingAgeableMob();

    @Override
    public Class<AgeableMob> getType() {
        return AgeableMob.class;
    }

    @Override
    public String getName() {
        return "Ageable Entities";
    }

    @Override
    public boolean Simulate(Object entityInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (entityInstance instanceof AgeableMob ageableMob) {
            return ((TickingAccessor) ageableMob).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
