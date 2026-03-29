package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TickingSugarCaneBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingSugarCaneBlock();

    @Override
    public Class<SugarCaneBlock> getType() {
        return SugarCaneBlock.class;
    }

    @Override
    public String getName() {
        return "Sugar Cane";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, Level level,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof SugarCaneBlock sugarcane) {
            return ((TickingAccessor) sugarcane).Simulate(ticksToSimulate, level, state, pos);
        }
        return false;
    }
}
