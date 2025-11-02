package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PassiveEntity.class)
public class PassiveEntityMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        PassiveEntity entity = (PassiveEntity) (Object) this;

        boolean changed = false;
        int currentAge = entity.getBreedingAge(); // Negative value for babies

        // This sucks
        if (entity instanceof ChickenEntity chickenEntity && currentAge >= 0) {
            // We can safely subtract, the check is <=
            chickenEntity.eggLayTime -= (int) ticksToSimulate;
            changed = true;
        } else if (currentAge == 0)
            return false;

        // Age up babies, "age" down adults to make their breeding cooldown pass
        // Move age towards zero
        entity.setBreedingAge(TickingCalculator.MoveTowardsZero(currentAge, ticksToSimulate));

        return changed; // Entity was aged
    }
}
