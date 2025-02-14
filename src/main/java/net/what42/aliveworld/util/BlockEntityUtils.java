package net.what42.aliveworld.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEntityUtils {
    public static void markChanged(World world, BlockPos pos, BlockState state) {
        world.markDirty(pos);
        if (!state.isAir()) {
            world.updateComparators(pos, state.getBlock());
        }
    }
}
