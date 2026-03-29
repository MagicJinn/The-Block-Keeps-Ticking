package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DriedGhastBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingDriedGhastBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingDriedGhastBlock();

    @Override
    public Class<DriedGhastBlock> getType() {
        return DriedGhastBlock.class;
    }

    @Override
    public String getName() {
        return "Dried Ghast";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof DriedGhastBlock block) {
            return ((TickingAccessor) block).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}


