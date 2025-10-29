package magicjinn.theblockkeepsticking.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TickingObject {
    /**
     * Gets the type of block this TickingBlock handles. Add public static final [HANDLING_TYPE]
     * INSTANCE = new [HANDLING_TYPE](); to the subclass to create implement this properly.
     * 
     * @return
     */
    public abstract Class<?> getType();

    // Custom simulation logic
    public abstract boolean Simulate(Object objectInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos);
}
