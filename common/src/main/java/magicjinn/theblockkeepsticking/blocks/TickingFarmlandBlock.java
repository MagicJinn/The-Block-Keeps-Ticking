package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TickingFarmlandBlock extends TickingObject {

    @Override
    public Class<?> getType() {
        return FarmlandBlock.class;
    }

    @Override
    public String getName() {
        return "Farmland Hydration";
    }

    @Override
    public boolean Simulate(Object objectInstance, long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        if (objectInstance instanceof FarmlandBlock block) {
            return ((TickingAccessor) block).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
