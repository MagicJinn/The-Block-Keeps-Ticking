package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingCocoaBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingCocoaBlock();

    @Override
    public Class<?> getType() {
        return CocoaBlock.class;
    }

    @Override
    public String getName() {
        return "Cocoa Beans";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CocoaBlock cocoaBlock) {
            return ((TickingAccessor) cocoaBlock).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }

}
