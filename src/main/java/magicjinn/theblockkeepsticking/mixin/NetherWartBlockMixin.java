package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(NetherWartBlock.class)
public class NetherWartBlockMixin implements TickingBlockAccessor {
    @Override
    public boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        // NetherWartBlock netherWart = (NetherWartBlock) (Object) this;

        // Because mojang is stupid
        int age = (int) state.get(NetherWartBlock.AGE);
        int maxAge = (int) NetherWartBlock.MAX_AGE;

        if (age >= maxAge)
            return false;

        // 10% chance per random tick
        int growth = TickingCalculator.RandomTickAmount(ticksToSimulate, world, 10);

        if (growth > 0) {
            int newAge = Math.min(maxAge, age + growth);
            BlockState newState = (BlockState) state.with(NetherWartBlock.AGE, newAge);
            world.setBlockState(pos, newState, 2);
            return true;
        }

        return false;
    }
}
