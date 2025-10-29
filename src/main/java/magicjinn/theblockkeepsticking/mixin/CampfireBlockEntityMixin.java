package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin implements TickingAccessor {
    @Shadow @Final private DefaultedList<ItemStack> itemsBeingCooked;
    @Shadow @Final private int[] cookingTimes;
    @Shadow @Final private int[] cookingTotalTimes;

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        // Check if campfire is lit
        if (!state.get(CampfireBlock.LIT))
            return false;

        boolean changed = false;
        for (int i = 0; i < itemsBeingCooked.size(); i++) {
            ItemStack item = itemsBeingCooked.get(i);
            if (!item.isEmpty()) {
                cookingTimes[i] += (int) ticksToSimulate;
                changed = true;
            }
        }

        return changed;
    }
}
