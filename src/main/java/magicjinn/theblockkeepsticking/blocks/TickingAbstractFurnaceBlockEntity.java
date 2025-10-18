package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.framework.ProcessingBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;

public class TickingAbstractFurnaceBlockEntity extends ProcessingBlock {
    public static final TickingAbstractFurnaceBlockEntity INSTANCE =
            new TickingAbstractFurnaceBlockEntity();

    @Override
    public Class<AbstractFurnaceBlockEntity> getType() {
        return AbstractFurnaceBlockEntity.class;
    }

    @Override
    public void Simulate(BlockEntity blockInstance, Long ticksToSimulate) {
        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) blockInstance;
    }
}
