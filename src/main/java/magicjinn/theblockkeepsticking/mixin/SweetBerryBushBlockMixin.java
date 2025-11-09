package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;

@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world, 5);
        final int age = state.get(SweetBerryBushBlock.AGE);

        if (randomTicks <= 0 || age >= SweetBerryBushBlock.MAX_AGE)
            return false;

        int newAge = Math.min(SweetBerryBushBlock.MAX_AGE, age + randomTicks);
        BlockState newState = state.with(SweetBerryBushBlock.AGE, newAge);
        world.setBlockState(pos, newState, 2);

        // no clue what this does
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, Emitter.of(newState));
        return true;
    }
}
