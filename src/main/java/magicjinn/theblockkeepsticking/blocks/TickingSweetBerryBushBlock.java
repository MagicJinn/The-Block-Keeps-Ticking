package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingSweetBerryBushBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingSweetBerryBushBlock();

    @Override
    public Class<SweetBerryBushBlock> getType() {
        return SweetBerryBushBlock.class;
    }

    @Override
    public String getName() {
        return "Sweet Berry Bushes";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof SweetBerryBushBlock sweetBerryBush) {
            return ((TickingAccessor) sweetBerryBush).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
