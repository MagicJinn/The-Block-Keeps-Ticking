package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof SugarCaneBlock sugarcane) {
            return ((TickingAccessor) sugarcane).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
