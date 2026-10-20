package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(SaplingBlock.class)
public class SaplingBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        SaplingBlock sapling = (SaplingBlock) (Object) this;

        // 1/7 chance per random tick
        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level, 7);
        if (randomTicks <= 0)
            return false;

        int stage = (int) state.getValue(SaplingBlock.STAGE);

        // Instead of immediately generating the tree, simulate growth
        for (int i = stage; i < randomTicks && i < 2; i++) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockState currentState = serverLevel.getBlockState(pos);
            if (currentState.getBlock() instanceof SaplingBlock) {
                sapling.advanceTree(serverLevel, pos, currentState, serverLevel.getRandom());
            }
        }

        return true; // Always return true, since saplings are always simulated
    }
}
