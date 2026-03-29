package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof StemBlock stem) {
            return ((TickingAccessor) stem).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
