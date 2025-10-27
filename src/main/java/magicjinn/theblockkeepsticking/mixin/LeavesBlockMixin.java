package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin implements TickingBlockAccessor {
    @Override
    public boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        LeavesBlock leavesBlock = (LeavesBlock) (Object) this;
        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world);

        if (randomTicks <= 0)
            return false;

        // since there is no random chance, we can safely call randomTick
        leavesBlock.randomTick(state, (ServerWorld) world, pos, world.random);
        return true;
    }
}
