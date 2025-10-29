package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingBambooShootBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingBambooShootBlock();

    @Override
    public Class<?> getType() {
        return BambooShootBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof BambooShootBlock shootBlock) {
            return ((TickingAccessor) shootBlock).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }

}
