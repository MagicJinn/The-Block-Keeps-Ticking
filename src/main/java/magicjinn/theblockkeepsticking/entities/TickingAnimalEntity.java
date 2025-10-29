package magicjinn.theblockkeepsticking.entities;


import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class TickingAnimalEntity extends TickingObject {
    public static final TickingObject INSTANCE = new TickingAnimalEntity();

    @Override
    public Class<?> getType() {
        return AnimalEntity.class;
    }

    @Override
    public boolean Simulate(Object objectInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (objectInstance instanceof AnimalEntity animal) {
            return ((TickingAccessor) animal).Simulate(ticksToSimulate, world, state, null);
        }
        return false;
    }
}
