package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.config.ModConfig;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.level.Level;

@Mixin(AgeableMob.class)
public class AgeableMobMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        AgeableMob ageableMob = (AgeableMob) (Object) this;

        boolean changed = false;
        int currentAge = ageableMob.getAge(); // Negative value for babies

        // This sucks
        if (ageableMob instanceof Chicken chickenEntity && currentAge >= 0) {
            if (ModConfig.isEnabled(ModConfig.CHICKEN_EGG_LAYING_IN_UNLOADED)) {
                // We can safely subtract, the check is <=
                // 6000 + anywhere between 0 and 6000 is default egg laying time
                // averages out to 9000
                chickenEntity.eggTime -= (int) (ticksToSimulate % 9000);
                changed = true;
            }
        } else if (currentAge == 0)
            return false;

        // Age up babies, "age" down adults to make their breeding cooldown pass
        // Move age towards zero
        ageableMob.setAge(TickingCalculator.MoveTowardsZero(currentAge, ticksToSimulate));

        return changed; // Entity was aged
    }
}
