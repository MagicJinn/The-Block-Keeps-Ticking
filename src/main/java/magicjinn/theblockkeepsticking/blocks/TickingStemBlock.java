package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.StemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingStemBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingStemBlock();

    @Override
    public Class<StemBlock> getType() {
        return StemBlock.class;
    }

    @Override
    public String getName() {
        return "Crop Stems";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof StemBlock stem) {
            return ((TickingAccessor) stem).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
