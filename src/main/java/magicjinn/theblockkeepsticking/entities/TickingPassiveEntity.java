package magicjinn.theblockkeepsticking.entities;

import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingPassiveEntity extends TickingObject {
    public static final TickingObject INSTANCE = new TickingPassiveEntity();

    @Override
    public Class<?> getType() {
        return PassiveEntity.class;
    }

    @Override
    public String getName() {
        return "Passive Entities";
    }

    @Override
    public boolean Simulate(Object entityInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (entityInstance instanceof PassiveEntity passiveEntity) {
            return ((TickingAccessor) passiveEntity).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }

    public static void AgePassiveEntity(PassiveEntity entity, long ticksToSimulate) {

    }

}
