package magicjinn.theblockkeepsticking.blocks;

import magicjinn.theblockkeepsticking.util.TickingObject;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickingAbstractCauldronBlock extends TickingObject {
    public static final TickingObject INSTANCE = new TickingAbstractCauldronBlock();

    @Override
    public Class<AbstractCauldronBlock> getType() {
        return AbstractCauldronBlock.class;
    }

    @Override
    public String getName() {
        return "Leveled Cauldron";
    }

    @Override
    public boolean Simulate(Object blockInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos) {
        if (blockInstance instanceof AbstractCauldronBlock cauldron) {
            return ((TickingAccessor) cauldron).Simulate(ticksToSimulate, world, state, pos);
        }
        return false;
    }
}
