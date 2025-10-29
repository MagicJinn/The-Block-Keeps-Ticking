package magicjinn.theblockkeepsticking.mixin;

import org.joml.Math;
import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PassiveEntity.class)
public class PassiveEntityMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        PassiveEntity entity = (PassiveEntity) (Object) this;

        // Check if the mob is an adult
        if (!entity.isBaby())
            return false; // Already an adult, nothing to simulate

        // If not an adult, add ticks to make it grow up
        int currentAge = entity.getBreedingAge(); // Negative value for babies
        entity.setBreedingAge(Math.max(0, currentAge + (int) ticksToSimulate));

        return true; // Entity was aged
    }
}
