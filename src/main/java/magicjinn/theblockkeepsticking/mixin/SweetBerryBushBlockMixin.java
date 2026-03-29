package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level, 5);
        final int age = state.getValue(SweetBerryBushBlock.AGE);

        if (randomTicks <= 0 || age >= SweetBerryBushBlock.MAX_AGE)
            return false;

        int newAge = Math.min(SweetBerryBushBlock.MAX_AGE, age + randomTicks);
        BlockState newState = state.setValue(SweetBerryBushBlock.AGE, newAge);
        level.setBlockAndUpdate(pos, newState);

        // no clue what this does
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, Context.of(newState));
        return true;
    }
}
