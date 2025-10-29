package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ChickenEntity.class)
public class ChickenEntityMixin implements TickingAccessor {

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        ChickenEntity chicken = (ChickenEntity) (Object) this;
        chicken.eggLayTime -= (int) ticksToSimulate; // We can safely subtract, the check is <=
        return true;
    }
}
