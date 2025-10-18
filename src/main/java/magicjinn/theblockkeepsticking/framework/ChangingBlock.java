package magicjinn.theblockkeepsticking.framework;

import net.minecraft.block.Block;

// Things like plants, that change their state over time
public abstract class ChangingBlock extends TickingBlock {
    public abstract void Simulate(Block blockInstance, Long ticksToSimulate);
}
