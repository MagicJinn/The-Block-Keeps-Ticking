package magicjinn.theblockkeepsticking.entities;

import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingChickenEntity extends TickingObject {
    public static final TickingObject INSTANCE = new TickingChickenEntity();

    @Override
    public Class<?> getType() {
        return ChickenEntity.class;
    }

    @Override
    public boolean Simulate(Object entityInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (entityInstance instanceof ChickenEntity chickenEntity) {
            return ((TickingAccessor) chickenEntity).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}


