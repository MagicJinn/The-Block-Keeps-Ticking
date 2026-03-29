package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingBrewingStandBlockEntity extends TickingObject {
    public static final TickingObject INSTANCE = new TickingBrewingStandBlockEntity();

    @Override
    public Class<BrewingStandBlockEntity> getType() {
        return BrewingStandBlockEntity.class;
    }

    @Override
    public String getName() {
        return "Brewing Stands";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof BrewingStandBlockEntity brewingStand) {
            return ((TickingAccessor) brewingStand).Simulate(ticksToSimulate, level, state,
                    pos);
        }
        return false;
    }
}
