package magicjinn.apiexample.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;

@Mixin(ChestBlockEntity.class)
public class ChestMixin implements TickingAccessor {

    private static final int TICKS_PER_DIAMOND = 120;

    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        ChestBlockEntity chestEntity = (ChestBlockEntity) (Object) this; // cast to ChestBlockEntity

        int amountToAdd = (int) (ticksToSimulate / TICKS_PER_DIAMOND);
        int inventorySlots = chestEntity.getContainerSize();

        boolean addedAny = false;

        // Get max stack size for diamonds
        int diamondMaxStackSize = new ItemStack(Items.DIAMOND).getMaxStackSize();

        for (int slot = 0; slot < inventorySlots; slot++) {
            if (amountToAdd <= 0)
                break;

            ItemStack stackInSlot = chestEntity.getItem(slot);
            if (!stackInSlot.isEmpty() && !stackInSlot.is(Items.DIAMOND)) // Don't overwrite non-diamond items
                continue;

            int currentStackSize = stackInSlot.getCount();
            int spaceInStack = diamondMaxStackSize - currentStackSize;

            if (spaceInStack > 0) {
                int toAdd = Math.min(amountToAdd, spaceInStack);

                if (stackInSlot.isEmpty())
                    chestEntity.setItem(slot, new ItemStack(Items.DIAMOND, toAdd));
                else
                    stackInSlot.grow(toAdd);

                amountToAdd -= toAdd;
                addedAny = true;
            }
        }
        return addedAny;
    }
}