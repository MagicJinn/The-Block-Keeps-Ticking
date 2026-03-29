package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

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
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof AbstractFurnaceBlockEntity furnace) {
            return ((TickingAccessor) furnace).Simulate(ticksToSimulate, level, state, pos);
        }
        return false; // If not an AbstractFurnaceBlockEntity, return false
    }
}
