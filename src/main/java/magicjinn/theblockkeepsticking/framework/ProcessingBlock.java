package magicjinn.theblockkeepsticking.framework;

import net.minecraft.block.entity.BlockEntity;
//TODO: Remove!
// Things like furnaces, that process items over time
public abstract class ProcessingBlock extends TickingBlock {
    public abstract void Simulate(BlockEntity blockInstance, Long ticksToSimulate);

}
