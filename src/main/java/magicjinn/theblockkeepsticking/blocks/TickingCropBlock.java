package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.accessors.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.TickingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingCropBlock extends TickingBlock {
    public static final TickingCropBlock INSTANCE = new TickingCropBlock();

    @Override
    public Class<CropBlock> getType() {
        return CropBlock.class;
    }

    @Override
    public boolean Simulate(Object blockInstance, Long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CropBlock crop) {
            return ((TickingBlockAccessor) crop).Simulate(ticksToSimulate, world, state, pos);
        }
        return false; // If not an CropBlock, return false
    }
}
