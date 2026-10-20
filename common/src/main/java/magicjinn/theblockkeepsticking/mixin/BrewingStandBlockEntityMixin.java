package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import magicjinn.theblockkeepsticking.util.TickingAccessor;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin implements TickingAccessor {
    @Shadow private int brewTime;

    private static final int MAX_BREWING_TIME = 400;

    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        // Since a brewing stand can only process 1 item at a time, and we don't support hoppers,
        // we only need to check whether there is already a recipe in progress,
        // and subtract time from it

        if (brewTime < MAX_BREWING_TIME && brewTime > 0) { // This means we're brewing
            brewTime = Math.max(1, brewTime - (int) ticksToSimulate);
            return true;
        }
        return false;
    }
}
