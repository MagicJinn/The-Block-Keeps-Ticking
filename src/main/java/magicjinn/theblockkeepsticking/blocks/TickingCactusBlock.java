package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingCactusBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingCactusBlock();

    @Override
    public Class<CactusBlock> getType() {
        return CactusBlock.class;
    }

    @Override
    public String getName() {
        return "Cactus";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CactusBlock cactus) {
            return ((TickingAccessor) cactus).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
