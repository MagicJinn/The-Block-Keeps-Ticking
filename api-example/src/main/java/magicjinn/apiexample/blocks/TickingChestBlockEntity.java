package magicjinn.apiexample.blocks;

import magicjinn.apiexample.APIExample;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// Example TickingObject that gives us access to the Simulate method of the ChestBlockEntity
public class TickingChestBlockEntity extends TickingObject {
    public static final TickingObject INSTANCE = new TickingChestBlockEntity();

    // The type of block entity the TickingObject will be simulating,
    // to be found in the world
    @SuppressWarnings("null")
    @Override
    public Class<ChestBlockEntity> getType() {
        return ChestBlockEntity.class;
    }

    // The name of the TickingObject's subject, in this case "Chest"
    @Override
    public String getName() {
        return "Chest";
    }

    // The mod ID of the mod that is registering this TickingObject
    @Override
    public String getModId() {
        return APIExample.class.getSimpleName();
    }

    // The blockInstance will be a ChestBlockEntity (as defined by getType()),
    // so we can safely cast it and call the Simulate method using the
    // TickingAccessor interface
    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof ChestBlockEntity chestEntity) {
            return ((TickingAccessor) chestEntity).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
