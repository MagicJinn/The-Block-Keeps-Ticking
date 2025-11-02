package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BuddingAmethystBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof BuddingAmethystBlock buddingAmethyst) {
            return ((TickingAccessor) buddingAmethyst).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
