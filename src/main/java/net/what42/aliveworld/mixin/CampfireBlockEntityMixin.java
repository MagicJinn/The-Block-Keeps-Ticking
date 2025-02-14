package net.what42.aliveworld.mixin;

import net.what42.aliveworld.api.CampfireAccess;
import net.what42.aliveworld.simulator.CampfireSimulator;
import net.what42.aliveworld.util.BlockEntityUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin implements CampfireAccess {
    @Shadow @Final private DefaultedList<ItemStack> itemsBeingCooked;
    @Shadow private int[] cookingTimes;
    @Shadow private int[] cookingTotalTimes;

    @Override
    public CampfireSimulator createSimulator() {
        if (cookingTimes == null) cookingTimes = new int[4];
        if (cookingTotalTimes == null) cookingTotalTimes = new int[4];
        return new CampfireSimulator(itemsBeingCooked, cookingTimes, cookingTotalTimes);
    }

    @Override
    public void apply(World world, BlockPos pos, BlockState state, CampfireSimulator simulator) {
        DefaultedList<ItemStack> newItems = simulator.getItems();
        for (int i = 0; i < itemsBeingCooked.size(); i++) {
            itemsBeingCooked.set(i, newItems.get(i));
        }

        System.arraycopy(simulator.getCookingTimes(), 0, cookingTimes, 0, cookingTimes.length);

        if (simulator.isDataChanged()) {
            world.markDirty(pos);
            if (!state.isAir()) {
                world.updateComparators(pos, state.getBlock());
            }
        }
    }
}