package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof CropBlock crop) {
            return ((TickingAccessor) crop).Simulate(ticksToSimulate, world, state, pos);
        }
        return false; // If not an CropBlock, return false
    }
}
