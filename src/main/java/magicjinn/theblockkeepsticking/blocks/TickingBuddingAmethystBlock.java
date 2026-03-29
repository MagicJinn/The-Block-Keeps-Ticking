package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingBuddingAmethystBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingBuddingAmethystBlock();

    @Override
    public Class<BuddingAmethystBlock> getType() {
        return BuddingAmethystBlock.class;
    }

    @Override
    public String getName() {
        return "Budding Amethyst";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof BuddingAmethystBlock buddingAmethyst) {
            return ((TickingAccessor) buddingAmethyst).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
