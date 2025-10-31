package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingAbstractFurnaceBlockEntity extends TickingObject {
    // Set an instance to more easily access this class
    public static final TickingObject INSTANCE =
            new TickingAbstractFurnaceBlockEntity();

    @Override
    public Class<AbstractFurnaceBlockEntity> getType() {
        return AbstractFurnaceBlockEntity.class;
    }

    @Override
    public String getName() {
        return "Furnaces";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof AbstractFurnaceBlockEntity furnace) {
            return ((TickingAccessor) furnace).Simulate(ticksToSimulate, world, state, pos);
        }
        return false; // If not an AbstractFurnaceBlockEntity, return false
    }
}
