package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;

import magicjinn.theblockkeepsticking.accessors.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.TickingBlock;
import magicjinn.theblockkeepsticking.util.Timer;
import net.minecraft.block.SaplingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(SaplingBlock.class)
public class SaplingBlockMixin implements TickingBlockAccessor {
    @Override
    public boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        SaplingBlock sapling = (SaplingBlock) (Object) this;

        // 1/7 chance per random tick
        int randomTicks = TickingBlock.RandomTickAmount(ticksToSimulate, world) / 7;
        if (randomTicks <= 0)
            return false;

        int stage = (int) state.get(SaplingBlock.STAGE);

        // Instead of immediately generating the tree, schedule it for 20 ticks later
        for (int i = stage; i < randomTicks && i < 2; i++) {
            Timer.INSTANCE.Schedule(
                    "sapling_growth_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ(),
                    server -> {
                        ServerWorld serverWorld = server.getWorld(world.getRegistryKey());
                        if (serverWorld != null) {
                            BlockState currentState = serverWorld.getBlockState(pos);
                            if (currentState.getBlock() instanceof SaplingBlock) {
                                sapling.generate(serverWorld, pos, currentState,
                                        serverWorld.random);
                            }
                        }
                    });
        }

        return true; // Always return true, since saplings are always simulated
    }
}
