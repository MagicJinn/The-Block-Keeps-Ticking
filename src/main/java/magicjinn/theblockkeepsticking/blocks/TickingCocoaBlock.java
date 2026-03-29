package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CocoaBlock cocoaBlock) {
            return ((TickingAccessor) cocoaBlock).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }

}
