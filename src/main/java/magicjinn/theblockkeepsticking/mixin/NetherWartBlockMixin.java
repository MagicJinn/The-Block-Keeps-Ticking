package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(NetherWartBlock.class)
public class NetherWartBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        // NetherWartBlock netherWart = (NetherWartBlock) (Object) this;

        // Because mojang is stupid
        int age = (int) state.getValue(NetherWartBlock.AGE);
        int maxAge = (int) NetherWartBlock.MAX_AGE;

        if (age >= maxAge)
            return false;

        // 10% chance per random tick
        int growth = TickingCalculator.RandomTickAmount(ticksToSimulate, level, 10);

        if (growth > 0) {
            int newAge = Math.min(maxAge, age + growth);
            BlockState newState = (BlockState) state.setValue(NetherWartBlock.AGE, newAge);
            level.setBlock(pos, newState, 2);
            return true;
        }

        return false;
    }
}
