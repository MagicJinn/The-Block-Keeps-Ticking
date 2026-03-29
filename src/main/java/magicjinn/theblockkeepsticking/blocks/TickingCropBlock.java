package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingCropBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingCropBlock();

    @Override
    public Class<CropBlock> getType() {
        return CropBlock.class;
    }

    @Override
    public String getName() {
        return "Crops";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CropBlock crop) {
            return ((TickingAccessor) crop).Simulate(ticksToSimulate, level, state, pos);
        }
        return false; // If not an CropBlock, return false
    }
}
