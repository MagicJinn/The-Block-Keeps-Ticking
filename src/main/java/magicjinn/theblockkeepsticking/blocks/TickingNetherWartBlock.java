package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingNetherWartBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingNetherWartBlock();

    @Override
    public Class<NetherWartBlock> getType() {
        return NetherWartBlock.class;
    }

    @Override
    public String getName() {
        return "Nether Wart";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof NetherWartBlock netherWart) {
            return ((TickingAccessor) netherWart).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
