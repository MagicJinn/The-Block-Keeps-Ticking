package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingPointedDripstoneBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingPointedDripstoneBlock();

    @Override
    public Class<PointedDripstoneBlock> getType() {
        return PointedDripstoneBlock.class;
    }

    @Override
    public String getName() {
        return "Pointed Dripstone";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof PointedDripstoneBlock pointedDripstone) {
            return ((TickingAccessor) pointedDripstone).Simulate(ticksToSimulate, world, state,
                    pos);
        }
        return false;
    }
}
