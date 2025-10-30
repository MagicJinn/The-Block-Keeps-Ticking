package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof BrewingStandBlockEntity brewingStand) {
            return ((TickingAccessor) brewingStand).Simulate(ticksToSimulate, world, state,
                    pos);
        }
        return false;
    }
}
