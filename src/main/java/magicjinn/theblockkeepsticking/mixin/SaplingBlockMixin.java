package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.SaplingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(SaplingBlock.class)
public class SaplingBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        SaplingBlock sapling = (SaplingBlock) (Object) this;

        // 1/7 chance per random tick
        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world, 7);
        if (randomTicks <= 0)
            return false;

        int stage = (int) state.get(SaplingBlock.STAGE);

        // Instead of immediately generating the tree, simulate growth
        for (int i = stage; i < randomTicks && i < 2; i++) {
            ServerWorld serverWorld = (ServerWorld) world;
            BlockState currentState = serverWorld.getBlockState(pos);
            if (currentState.getBlock() instanceof SaplingBlock) {
                sapling.generate(serverWorld, pos, currentState, serverWorld.random);
            }
        }

        return true; // Always return true, since saplings are always simulated
    }
}
