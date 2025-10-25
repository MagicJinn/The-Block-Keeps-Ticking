package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.accessors.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.TickingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingAbstractFurnaceBlockEntity extends TickingBlock {
    // Set an instance to more easily access this class
    public static final TickingBlock INSTANCE =
            new TickingAbstractFurnaceBlockEntity();

    @Override
    public Class<AbstractFurnaceBlockEntity> getType() {
        return AbstractFurnaceBlockEntity.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, Long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof AbstractFurnaceBlockEntity furnace) {
            return ((TickingBlockAccessor) furnace).Simulate(ticksToSimulate, world, state, pos);
        }
        return false; // If not an AbstractFurnaceBlockEntity, return false
    }
}
