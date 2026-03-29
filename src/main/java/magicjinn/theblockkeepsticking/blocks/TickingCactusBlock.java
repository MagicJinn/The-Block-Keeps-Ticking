package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CactusBlock cactus) {
            return ((TickingAccessor) cactus).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
