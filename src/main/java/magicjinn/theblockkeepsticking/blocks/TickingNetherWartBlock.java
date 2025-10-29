package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingNetherWartBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingNetherWartBlock();

    @Override
    public Class<NetherWartBlock> getType() {
        return NetherWartBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof NetherWartBlock netherWart) {
            return ((TickingAccessor) netherWart).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
