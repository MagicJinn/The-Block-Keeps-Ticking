package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.accessors.AbstractFurnaceAccessor;
import magicjinn.theblockkeepsticking.util.TickingBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;

public class TickingAbstractFurnaceBlockEntity extends TickingBlock {
    // Set an instance to more easily access this class
    public static final TickingAbstractFurnaceBlockEntity INSTANCE =
            new TickingAbstractFurnaceBlockEntity();

    @Override
    public Class<AbstractFurnaceBlockEntity> getType() {
        return AbstractFurnaceBlockEntity.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, Long ticksToSimulate) {
        if (blockInstance instanceof AbstractFurnaceBlockEntity furnace) {
            return ((AbstractFurnaceAccessor) furnace).Simulate(ticksToSimulate);
        }
        return false; // If not an AbstractFurnaceBlockEntity, return false
    }
}
